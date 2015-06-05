/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.client.socket;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.ev.ConnectedResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.HeapDelta;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.NioUtils;
import com.betfair.cougar.netutil.nio.TerminateSubscription;
import com.betfair.cougar.netutil.nio.connected.InitialUpdate;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;
import com.betfair.cougar.transport.api.protocol.socket.NewHeapSubscription;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.ImmutableHeap;
import com.betfair.platform.virtualheap.conflate.Conflater;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Manages connected objects, and subscriptions thereof.
 */
public class ClientConnectedObjectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectedObjectManager.class);

    private ConcurrentHashMap<String, ConnectedHeaps> heapsByServer = new ConcurrentHashMap<String, ConnectedHeaps>();
    private NioLogger nioLogger;
    private BlockingDeque<String> sessionsWithUpdates = new LinkedBlockingDeque<String>();

    private final List<ConnectedObjectPuller> pullers = new ArrayList<ConnectedObjectPuller>();
    private int numProcessingThreads;
    private long maxInitialPopulationWait;

    private final Lock heapSubMutationLock = new ReentrantLock();
    private long pullerAwaitTimeout;
    private long missingDeltaTimeout;
    private int maxDeltaQueue;
    private final Lock queueHealthCheckLock = new ReentrantLock();

    private Conflater newListenerConflater;

    private CougarObjectIOFactory objectIOFactory;

    private static final AtomicLong initialPopulationThreadIdSource = new AtomicLong();

    // exposed for testing
    ConcurrentHashMap<String, ConnectedHeaps> getHeapsByServer() {
        return heapsByServer;
    }

    // exposed for testing
    BlockingDeque<String> getSessionsWithUpdates() {
        return sessionsWithUpdates;
    }

    public Lock getHeapSubMutationLock() {
        return heapSubMutationLock;
    }

    public void setNumProcessingThreads(int numProcessingThreads) {
        this.numProcessingThreads = numProcessingThreads;
    }

    public void setMaxInitialPopulationWait(long maxInitialPopulationWait) {
        this.maxInitialPopulationWait = maxInitialPopulationWait;
    }

    public void setPullerAwaitTimeout(long pullerAwaitTimeout) {
        this.pullerAwaitTimeout = pullerAwaitTimeout;
    }

    public void setMissingDeltaTimeout(long missingDeltaTimeout) {
        this.missingDeltaTimeout = missingDeltaTimeout;
    }

    public void setMaxDeltaQueue(int maxDeltaQueue) {
        this.maxDeltaQueue = maxDeltaQueue;
    }

    public void setObjectIOFactory(CougarObjectIOFactory objectIOFactory) {
        this.objectIOFactory = objectIOFactory;
    }

    public void setNewListenerConflater(Conflater newListenerConflater) {
        // spring 2.5 has issues with null beans floating around, so we have a marker implementation to connote null
        if (newListenerConflater != ConflaterFactory.NULL_CONFLATER) {
            this.newListenerConflater = newListenerConflater;
        } else {
            this.newListenerConflater = null;
        }
    }

    public void start() {
        for (int i = 0; i < numProcessingThreads; i++) {
            ConnectedObjectPuller puller = new ConnectedObjectPuller();
            pullers.add(puller);
            new Thread(puller, "ConnectedObjectPuller-" + (i + 1)).start();
        }
    }

    public void stop() {
        for (ConnectedObjectPuller puller : pullers) {
            puller.stop();
        }
    }

    public void setNioLogger(NioLogger nioLogger) {
        this.nioLogger = nioLogger;
    }

    // monitoring methods
    public ConnectedHeaps getHeapsForSession(IoSession session) {
        return heapsByServer.get(NioUtils.getSessionId(session));
    }

    public void handleSubscriptionResponse(final IoSession currentSession, InvocationResponse in, final ExecutionObserver observer) {
        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Received a subscription response");
        final NewHeapSubscription newHeapSubscription;
        try {
            newHeapSubscription = (NewHeapSubscription) in.getResult();
        } catch (Exception e) {
            LOGGER.warn("Error unpacking subscription result", e);
            observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.FrameworkError, "Error unpacking subscription result", e)));
            return;
        }
        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Received a subscription response for heapId %s with subscriptionId %s", newHeapSubscription.getHeapId(), newHeapSubscription.getSubscriptionId());

        final String sessionId = NioUtils.getSessionId(currentSession);
        ConnectedHeaps heaps;
        heapSubMutationLock.lock();
        try {
            heaps = heapsByServer.get(sessionId);
            if (heaps == null) {
                heaps = new ConnectedHeaps();
                heapsByServer.put(sessionId, heaps);
            }
        } finally {
            heapSubMutationLock.unlock();
        }

        // new heap
        boolean newHeap = false;
        if (newHeapSubscription.getUri() != null) {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Received a new heap definition, heapId = %s, heapUrl = %s", newHeapSubscription.getHeapId(), newHeapSubscription.getUri());
            newHeap = heaps.addHeap(newHeapSubscription.getHeapId(), newHeapSubscription.getUri());
            if (!newHeap) {
                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Received a new heap definition, heapId = %s, even though we know about the heap already!", newHeapSubscription.getHeapId());
            }
        }
        final boolean preExistingHeap = !newHeap;

        // find heap uri
        final HeapState heapState = heaps.getHeapState(newHeapSubscription.getHeapId());
        if (heapState == null) {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Couldn't find heap definition, heapId = %s", newHeapSubscription.getHeapId());
            LOGGER.warn("Can't find the heap for this subscription result. Heap id = " + newHeapSubscription.getHeapId());
            observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.FrameworkError, "Can't find the heap for this subscription result. Heap id = " + newHeapSubscription.getHeapId())));
        } else {
            if (preExistingHeap && heapState.haveSeenInitialUpdate()) {
                Subscription sub = heapState.addSubscription(this, currentSession, newHeapSubscription.getHeapId(), newHeapSubscription.getSubscriptionId());
                if (sub != null) {
                    observer.onResult(new ExecutionResult(new ConnectedResponseImpl(heapState.getHeap(), sub)));
                } else {
                    // null sub means we already had a subscription with that id, something's not in a good state in the server, so kill this connection as we don't know what's going on
                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Duplicate subscription returned by the server, id = %s - closing session", newHeapSubscription.getSubscriptionId());
                    LOGGER.warn("Duplicate subscription returned by the server, id = " + newHeapSubscription.getSubscriptionId() + " - closing session");
                    observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.FrameworkError, "Duplicate subscription returned by the server, id = " + newHeapSubscription.getSubscriptionId())));
                    currentSession.close();
                }
            } else {
                // split this off into it's own thread since the mina docs lie and we only have one ioprocessor thread and if we don't fork we'd block forever
                final ConnectedHeaps finalHeaps = heaps;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean resultSent = false;
                        // now we've got the heap
                        CountDownLatch initialPopulationLatch = finalHeaps.getInitialPopulationLatch(newHeapSubscription.getHeapId());
                        try {
                            boolean populated = false;
                            if (initialPopulationLatch != null) {
                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Waiting for initial heap population, heapUrl = %s", newHeapSubscription.getUri());
                                populated = initialPopulationLatch.await(maxInitialPopulationWait, TimeUnit.MILLISECONDS);
                                finalHeaps.removeInitialPopulationLatch(newHeapSubscription.getHeapId());
                            } else {
                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Initial heap population, heapUrl = %s", newHeapSubscription.getUri());

                            }
                            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Returning heap to client, heapUrl = %s", newHeapSubscription.getUri());
                            if (populated) {
                                observer.onResult(new ExecutionResult(new ConnectedResponseImpl(heapState.getHeap(), heapState.addSubscription(ClientConnectedObjectManager.this, currentSession, newHeapSubscription.getHeapId(), newHeapSubscription.getSubscriptionId()))));
                                resultSent = true;
                            }
                        } catch (InterruptedException e) {
                            // we got interrupted waiting for the response, oh well..
                        } catch (RuntimeException e) {
                            LOGGER.warn("Error processing initial heap population, treating as a failure", e);
                        } finally {
                            if (!resultSent) {
                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, currentSession, "Didn't get initial population message for heap, heapUrl = %s", newHeapSubscription.getUri());
                                // we don't worry about the case where it was a preExisting heap since the thread where it wasn't received will deal with it
                                if (!preExistingHeap) {
                                    terminateSubscriptions(currentSession, newHeapSubscription.getHeapId(), Subscription.CloseReason.INTERNAL_ERROR);
                                }
                                LOGGER.warn("Didn't get initial population message for heap id = " + newHeapSubscription.getHeapId());
                                observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.FrameworkError, "Didn't get initial population message for heap id = " + newHeapSubscription.getHeapId())));
                            }
                        }
                    }
                }, "SubscriptionResponseHandler-InitialPopulation-" + initialPopulationThreadIdSource.incrementAndGet() + "-" + heapState.getHeapUri()).start();
            }
        }
    }

    public void sessionTerminated(IoSession session) {
        terminateAllSubscriptions(session, Subscription.CloseReason.CONNECTION_CLOSED);
    }

    public void applyDelta(IoSession session, HeapDelta payload) {
        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Applying update for heap, heapId = %s, updateId = %s", payload.getHeapId(), payload.getUpdateId());
        ConnectedHeaps heaps = heapsByServer.get(NioUtils.getSessionId(session));
        // if we've got no record then we can't continue, and we can't really throw an exception, so just warn and ignore..
        if (heaps == null) {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Have no heaps registered for this client, address = %s", session.getRemoteAddress().toString());
            LOGGER.warn("Received a connected object update, yet have no record of any subscriptions. {address={},heapId={},updateId={}}", session.getRemoteAddress().toString(), payload.getHeapId(), payload.getUpdateId());
            return;
        }

        HeapState heapState = heaps.getHeapState(payload.getHeapId());
        if (heapState == null) {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Can't find this heap for this client, address = %s, heapId = %s", session.getRemoteAddress().toString(), payload.getHeapId());
            LOGGER.warn("Received a connected object update, yet have no record of a subscription for this heap. {address={},heapId={},updateId={}}", session.getRemoteAddress().toString(), payload.getHeapId(), payload.getUpdateId());
            return;
        }

        boolean containsInitialUpdate = (!payload.getUpdates().isEmpty() && (payload.getUpdates().get(0) instanceof InitialUpdate));
        if (containsInitialUpdate) {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Queueing initial update to local heap, heapUri = %s", heapState.getHeapUri());
        } else {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Queueing patch to local heap, heapUri = %s", heapState.getHeapUri());
        }

        heapState.queueUpdate(payload);
        heaps.queueUpdatedHeap(payload.getHeapId());
        sessionsWithUpdates.add(NioUtils.getSessionId(session));
    }

    private class ConnectedObjectPuller implements Runnable {
        private volatile boolean running = true;

        public void run() {
            while (running) {
                try {
                    String sessionId = sessionsWithUpdates.pollFirst(pullerAwaitTimeout, TimeUnit.MILLISECONDS);
                    if (sessionId != null) {
                        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Found session with queued heap update");
                        ConnectedHeaps heaps = heapsByServer.get(sessionId);
                        if (heaps != null) { // session could have died..
                            Long heapId = heaps.pollNextHeapId();
                            if (heapId != null) {
                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Queued heap update found for heapId = %s", heapId);
                                HeapState state = heaps.getHeapState(heapId);
                                if (state != null) {
                                    Lock lock = state.getHeapUpdateLock();
                                    lock.lock();
                                    try {
                                        // right, now apply all updates in sequential order, until we hit a gap
                                        HeapDelta delta = state.peekNextDelta();
                                        if (delta == null) {
                                            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "All contiguous deltas already processed for heapId = %s", heapId);
                                        }
                                        while (delta != null) {
                                            // take a copy now, so we can use it in the initial update processing later...
                                            HeapDelta currentDelta = delta;
                                            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Applying delta %s for heapId = %s", currentDelta.getUpdateId(), heapId);
                                            if (currentDelta.containsHeapTermination()) {
                                                heapSubMutationLock.lock();
                                                try {
                                                    currentDelta.applyTo(state.getHeap().asListener());
                                                    state.popNextDelta();

                                                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Found heap termination in delta %s for heapId = %s", currentDelta.getUpdateId(), heapId);
                                                    terminateSubscriptions(sessionId, heapId, Subscription.CloseReason.REQUESTED_BY_PUBLISHER);

                                                    delta = null;
                                                } finally {
                                                    heapSubMutationLock.unlock();
                                                }
                                            } else {
                                                currentDelta.applyTo(state.getHeap().asListener());
                                                state.popNextDelta();
                                                delta = state.peekNextDelta();
                                            }
                                            if (currentDelta.containsFirstUpdate()) {
                                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Found initial update in delta for heapId = %s", heapId);
                                                // basically we got the first update
                                                CountDownLatch latch = heaps.getInitialPopulationLatch(heapId);
                                                if (latch != null) {
                                                    latch.countDown();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        // something's gone a bit wrong. abort this client now..
                                        LOGGER.warn("Error processing update", e);
                                        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Error occurred processing update for heapId = %s, terminating heap", heapId);
                                        terminateSubscriptions(sessionId, heapId, Subscription.CloseReason.INTERNAL_ERROR);
                                    } finally {
                                        lock.unlock();
                                    }
                                } else {
                                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Received updated for unknown heap, id = %s, assuming it's already been processed by another thread",  heapId);
                                }
                            } else {
                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Queued heap update already processed by another thread");
                            }
                        } else {

                            if (nioLogger.isLogging(NioLogger.LoggingLevel.TRANSPORT)) {
                                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "No heaps found for session, they must have been terminated");
                            }
                        }
                    }
                    // now just have a quick peek at each heap state and check it's queue health
                    // This could be a nasty bottleneck, so we'll only allow one thread to do this at a time, the others can get on with processing work..
                    if (queueHealthCheckLock.tryLock()) {
                        try {
                            for (String sessId : new ArrayList<String>(heapsByServer.keySet())) {
                                ConnectedHeaps heaps = heapsByServer.get(sessId);
                                for (Long heapId : heaps.getAllHeapIds()) {
                                    HeapState state = heaps.getHeapState(heapId);
                                    if (state != null) {
                                        HeapState.QueueHealth health = state.checkDeltaQueueHealth(maxDeltaQueue, missingDeltaTimeout);
                                        if (health != HeapState.QueueHealth.HEALTHY) {
                                            switch (health) {
                                                case QUEUE_TOO_LONG:
                                                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessId, "Queued up too many changes looking for next update for heapId = %s, terminating heap", heapId);
                                                    break;
                                                case WAITED_TOO_LONG:
                                                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessId, "Waited too long for next update for heapId = %s, terminating heap", heapId);
                                                    break;
                                                default:
                                                    LOGGER.warn("Unrecognized health for queue: " + health);
                                            }
                                            terminateSubscriptions(sessId, heapId, Subscription.CloseReason.INTERNAL_ERROR);
                                        }
                                    } else {
                                        LOGGER.warn("Couldn't find heap state for heapId: " + heapId);
                                    }
                                }
                            }
                        } finally {
                            queueHealthCheckLock.unlock();
                        }
                    }
                } catch (InterruptedException ie) {
                    // ignore, we'll go around the loop again and wait on the poll again
                } catch (Exception e) {
                    LOGGER.warn("Error processing update", e);
                }
            }
        }

        public void stop() {
            running = false;
        }
    }

    public void terminateSubscription(IoSession session, TerminateSubscription payload) {
        Subscription.CloseReason reason = Subscription.CloseReason.REQUESTED_BY_PUBLISHER;
        try {
            reason = Subscription.CloseReason.valueOf(payload.getCloseReason());
        } catch (IllegalArgumentException iae) {
            // unrecognised reason
        }
        terminateSubscription(session, payload.getHeapId(), payload.getSubscriptionId(), reason);
    }

    public void terminateSubscription(IoSession session, long heapId, String subscriptionId, Subscription.CloseReason reason) {
        heapSubMutationLock.lock();
        try {
            String sessionId = NioUtils.getSessionId(session);
            ConnectedHeaps heaps = heapsByServer.get(NioUtils.getSessionId(session));
            if (heaps != null) {
                HeapState heapState = heaps.getHeapState(heapId);
                if (heapState != null) {
                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Subscription termination received for subscription %s with reason %s", subscriptionId, reason);
                    if (reason == Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER || reason == Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER_ADMINISTRATOR) {
                        try {
                            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Notifying server that client wants to terminate subscription %s", subscriptionId);
                            NioUtils.writeEventMessageToSession(session, new TerminateSubscription(heapId, subscriptionId, reason.name()), objectIOFactory);
                        } catch (Exception ioe) {
                            // if we can't write to the stream to tell the server that the client wants to unsub, then it's likely the session is already
                            // gone. however, we'll log a message to let people know and then request a close of the session to make sure.
                            nioLogger.log(NioLogger.LoggingLevel.SESSION, session, "Error occurred whilst trying to inform server of subscription termination, closing session");
                            LOGGER.info("Error occurred whilst trying to inform server of subscription termination, closing session", ioe);
                            session.close();
                        }
                    }

                    heapState.terminateSubscription(subscriptionId, reason);
                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Subscription terminated for heapId = %s and subscriptionId = %s", heapId, subscriptionId);

                    if (heapState.getSubscriptions().isEmpty()) {
                        terminateSubscriptions(sessionId, heapId, Subscription.CloseReason.INTERNAL_ERROR); // if there's something found by this then it's an internal error.
                    }
                }
            }
        } finally {
            heapSubMutationLock.unlock();
        }
    }

    public void terminateSubscriptions(IoSession session, long heapId, Subscription.CloseReason reason) {
        terminateSubscriptions(NioUtils.getSessionId(session), heapId, reason);
    }

    public void terminateSubscriptions(String sessionId, long heapId, Subscription.CloseReason reason) {
        heapSubMutationLock.lock();
        try {
            ConnectedHeaps heaps = heapsByServer.get(sessionId);
            if (heaps != null) {
                heaps.terminateHeap(heapId, reason);
                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "Subscriptions terminated for heapId = %s", heapId);
                if (heaps.isEmpty()) {
                    heapsByServer.remove(sessionId);
                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "All subscriptions terminated");
                }
            }
        } finally {
            heapSubMutationLock.unlock();
        }
    }

    public void terminateAllSubscriptions(IoSession session, Subscription.CloseReason reason) {
        heapSubMutationLock.lock();
        try {
            String sessionId = NioUtils.getSessionId(session);
            ConnectedHeaps heaps = heapsByServer.get(sessionId);
            // it's possible that this session hasn't been used for push at all..
            if (heaps != null) {
                heaps.terminateAllHeaps(reason);
                heapsByServer.remove(sessionId);
                if (nioLogger.isLogging(NioLogger.LoggingLevel.TRANSPORT)) {
                    nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, sessionId, "All subscriptions terminated");
                }
            }
        } finally {
            heapSubMutationLock.unlock();
        }
    }

    /**
     * Represents a set of heaps that are shared with a single server.
     */
    public class ConnectedHeaps {
        private Map<Long, HeapState> heapStates = new HashMap<Long, HeapState>();
        private Map<Long, CountDownLatch> initialLatches = new HashMap<Long, CountDownLatch>();
        private BlockingDeque<Long> heapsWithUpdates = new LinkedBlockingDeque<Long>();
        private AtomicLong queueLength = new AtomicLong();

        // returns true if a new heap was added
        public boolean addHeap(long heapId, String uri) {
            heapSubMutationLock.lock();
            try {
                if (!heapStates.containsKey(heapId)) {
                    Heap heap = new ImmutableHeap(uri, newListenerConflater);
                    initialLatches.put(heapId, new CountDownLatch(1));
                    heapStates.put(heapId, new HeapState(heap));
                    return true;
                }
                return false;
            } finally {
                heapSubMutationLock.unlock();
            }
        }

        public CountDownLatch getInitialPopulationLatch(long heapId) {
            return initialLatches.get(heapId);
        }

        public void removeInitialPopulationLatch(long heapId) {
            heapSubMutationLock.lock();
            try {
                initialLatches.remove(heapId);
            } finally {
                heapSubMutationLock.unlock();
            }
        }

        public HeapState getHeapState(long heapId) {
            return heapStates.get(heapId);
        }

        public Long pollNextHeapId() {
            Long ret = heapsWithUpdates.pollFirst();
            if (ret != null) {
                queueLength.decrementAndGet();
            }
            return ret;
        }

        public void queueUpdatedHeap(long heapId) {
            heapsWithUpdates.add(heapId);
            queueLength.incrementAndGet();
        }

        public void terminateHeap(long heapId, Subscription.CloseReason reason) {
            heapSubMutationLock.lock();
            try {
                HeapState state = heapStates.remove(heapId);
                if (state != null) {
                    state.terminateAllSubscriptions(reason);
                }
                initialLatches.remove(heapId);
            } finally {
                heapSubMutationLock.unlock();
            }
        }

        public void terminateAllHeaps(Subscription.CloseReason reason) {
            heapSubMutationLock.lock();
            try {
                List<Long> keys = new ArrayList<Long>(heapStates.keySet());
                for (long heapId : keys) {
                    terminateHeap(heapId, reason);
                }
            } finally {
                heapSubMutationLock.unlock();
            }
        }

        public boolean isEmpty() {
            return heapStates.isEmpty();
        }

        public List<Long> getAllHeapIds() {
            heapSubMutationLock.lock();
            try {
                return new ArrayList<Long>(heapStates.keySet());
            } finally {
                heapSubMutationLock.unlock();
            }
        }

        public int getHeapCount() {
            return heapStates.size();
        }

        public long getQueueLength() {
            return queueLength.get();
        }
    }
}
