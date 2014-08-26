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

package com.betfair.cougar.transport.socket;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.UUIDGenerator;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.core.impl.logging.ConnectedObjectLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.*;
import com.betfair.cougar.netutil.nio.connected.InitialUpdate;
import com.betfair.cougar.netutil.nio.connected.TerminateHeap;
import com.betfair.cougar.netutil.nio.connected.Update;
import com.betfair.cougar.netutil.nio.message.EventMessage;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.api.protocol.socket.NewHeapSubscription;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.platform.virtualheap.Heap;
import org.apache.mina.common.IoSession;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.betfair.cougar.core.api.ev.Subscription.CloseReason.*;

@ManagedResource
public class PooledServerConnectedObjectManager implements ServerConnectedObjectManager {

    private static Logger LOGGER = LoggerFactory.getLogger(PooledServerConnectedObjectManager.class);

    private static final AtomicLong heapStateInstanceIdSource = new AtomicLong();

    private EventLogger eventLogger;
    private NioLogger nioLogger;

    private BlockingDeque<String> heapsWaitingForUpdate = new LinkedBlockingDeque<String>();

    // mods of these 2 for writes were handled. removals are more problematic.. so let's into a r/w lock over their interactions
    private ReentrantLock subTermLock = new ReentrantLock();
    private Map<String, HeapState> heapStates = new HashMap<String, HeapState>();
    private Map<Long, String> heapUris = new HashMap<Long, String>();
    private Map<IoSession, Multiset<String>> heapsByClient = new HashMap<IoSession, Multiset<String>>();

    private AtomicLong heapIdGenerator = new AtomicLong(0);
    private CougarObjectIOFactory objectIOFactory;

    private int numProcessingThreads;
    private List<ConnectedObjectPusher> pushers = new ArrayList<ConnectedObjectPusher>();

    private int maxUpdateActionsPerMessage;

    private UUIDGenerator uuidGenerator = new UUIDGeneratorImpl();

    private Thread shutdownHook = new Thread(new Runnable() {
        @Override
        public void run() {
            LOGGER.info("Terminating all push subscriptions due to application shutdown.");
            terminateAllSubscriptions(NODE_SHUTDOWN);
        }
    }, "PooledServerConnectedObjectManager-ShutdownHook");

    // used for testing
    Map<String, HeapState> getHeapStates() {
        return heapStates;
    }

    public Map<Long, String> getHeapUris() {
        return heapUris;
    }

    // used for testing
    Map<IoSession, Multiset<String>> getHeapsByClient() {
        return heapsByClient;
    }

    BlockingDeque<String> getHeapsWaitingForUpdate() {
        return heapsWaitingForUpdate;
    }

    // used for monitoring
    public List<String> getHeapsForSession(IoSession session) {
        List<String> ret = new ArrayList<String>();
        try {
            subTermLock.lock();
            Multiset<String> s = heapsByClient.get(session);
            if (s != null) {
                ret.addAll(s.keySet());
            }
        } finally {
            subTermLock.unlock();
        }
        return ret;
    }

    public HeapStateMonitoring getHeapStateForMonitoring(String uri) {
        return heapStates.get(uri);
    }

    @Override
    public void setNioLogger(NioLogger nioLogger) {
        this.nioLogger = nioLogger;
    }

    public void setObjectIOFactory(CougarObjectIOFactory objectIOFactory) {
        this.objectIOFactory = objectIOFactory;
    }

    public void setNumProcessingThreads(int i) {
        this.numProcessingThreads = i;
    }

    public void setEventLogger(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void setMaxUpdateActionsPerMessage(int maxUpdateActionsPerMessage) {
        this.maxUpdateActionsPerMessage = maxUpdateActionsPerMessage;
    }

    public void start() {
        for (int i = 0; i < numProcessingThreads; i++) {
            ConnectedObjectPusher pusher = new ConnectedObjectPusher();
            pushers.add(pusher);
            new Thread(pusher, "ConnectedObjectPusher-" + (i + 1)).start();
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void stop() {
        for (ConnectedObjectPusher pusher : pushers) {
            pusher.stop();
        }

        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        shutdownHook.run();
    }

    private void terminateAllSubscriptions(Subscription.CloseReason reason) {
        if (heapsByClient != null) {
            List<IoSession> sessions;
            try {
                subTermLock.lock();
                // take a copy in case it's being modified as we shutdown
                sessions = new ArrayList<IoSession>(heapsByClient.keySet());
            } finally {
                subTermLock.unlock();
            }

            for (IoSession session : sessions) {
                terminateSubscriptions(session, reason);
            }
        }
    }

    // note, you must have the subterm lock before calling this method
    private HeapState processHeapStateCreation(final ConnectedResponse result, final String heapUri) {
        if (!subTermLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("You must have the subTermLock before calling this method");
        }
        final HeapState newState = new HeapState(result.getHeap());
        // we're safe to lock this out of normal order as the HeapState isn't visible to other threads until the
        // end of this block. We have to have the lock before we make it visible..
        newState.getUpdateLock().lock();
        // new heap for this transport
        UpdateProducingHeapListener listener = new UpdateProducingHeapListener() {

            @Override
            protected void doUpdate(Update u) {
                if (u.getActions().size() > 0) {
                    newState.getQueuedChanges().add(new QueuedHeapChange(u));
                    // bad luck, we just added the heap and it's just about to get terminated...
                    if (u.getActions().contains(TerminateHeap.INSTANCE)) {
                        newState.getQueuedChanges().add(new HeapTermination());
                    }
                    heapsWaitingForUpdate.add(heapUri);
                }
            }
        };
        newState.setHeapListener(listener);
        result.getHeap().addListener(listener, false);
        heapStates.put(heapUri, newState);
        heapUris.put(newState.getHeapId(), heapUri);
        return newState;
    }

    @Override
    public void addSubscription(final SocketTransportCommandProcessor commandProcessor, final SocketTransportRPCCommand command, final ConnectedResponse result, final OperationDefinition operationDefinition, final DehydratedExecutionContext context, final LogExtension connectedObjectLogExtension) {
        final String heapUri = result.getHeap().getUri();
        HeapState heapState = null;
        try {
            boolean readyToContinue = false;

            while (!readyToContinue) {
                // only need this lock to modify the heapStates map, we need the state lock to modify the contained state later..
                subTermLock.lock();
                heapState = heapStates.get(heapUri);
                boolean wasNewHeapState = heapState == null;
                if (wasNewHeapState) {
                    heapState = processHeapStateCreation(result, heapUri);
                    readyToContinue = true;
                }
                // for existing heaps we lock in the same way as usual
                else {
                    // we have to release this lock now so we can get them in the right order, otherwise we could deadlock
                    // this is safe since we know that at this moment in time there is a live heap state for this heapuri
                    subTermLock.unlock();

                    // between these 2 calls one of 3 things can happen:
                    // 1: nothing
                    // 2: the last subscriber to the heap state unsubscribes and the heap state is removed
                    // 3: the last subscriber goes away and someone else comes through at the right moment and recreates it (less likely)

                    // in the last case we need to just keep looping until we know for certain it hasn't happened...
                    // what we need is a unique heapstate instance id which we can compare to... (AtomicLong should be sufficient)

                    // now get them in the right order
                    heapState.getUpdateLock().lock();
                    subTermLock.lock();


                    // so, in case 2 above the heap is now not in the map.. in which case we're going to subscribe to something just as it goes..
                    // so, lets do that new state check once more
                    if (!heapStates.containsKey(heapUri)) {
                        heapState = processHeapStateCreation(result, heapUri);
                        readyToContinue = true;
                    }
                    // ok, so it's still there, now we need to check if it's the same one..
                    else {
                        HeapState latestState = heapStates.get(heapUri);
                        // case 1 above
                        if (latestState.getInstanceId() == heapState.getInstanceId()) {
                            readyToContinue = true;
                        }
                        // case 3 above
                        else {
                            // this shouldn't matter anymore as we've got a lock on a dead heap
                            heapState.getUpdateLock().unlock();
                            // reset our check state and go back round until we're happy
                            heapState = latestState;
                        }
                    }
                }
            }

            // right, now we've got both locks, in the right order and we've definitely got a heap state which everyone else can also get/has got

            final HeapState finalHeapState = heapState;
            // hmm,
            final Subscription subscription = result.getSubscription();
            result.getHeap().traverse(new UpdateProducingHeapListener() {
                @Override
                protected void doUpdate(Update u) {
                    boolean updateContainsTermination = u.getActions().contains(TerminateHeap.INSTANCE);
                    if (updateContainsTermination) {
                        // note this won't notify this sub, which never got started. the publisher code won't expect a call back for this since
                        // it's just terminated the heap, which implies it wants to disconnect all clients anyway
                        terminateSubscriptions(command.getSession(), heapUri, REQUESTED_BY_PUBLISHER);
                        commandProcessor.writeErrorResponse(command, context, new CougarFrameworkException("Subscription requested for terminated heap: " + heapUri), true);
                        return;
                    }

                    Multiset<String> heapsForThisClient = heapsByClient.get(command.getSession());
                    if (heapsForThisClient == null) {
                        heapsForThisClient = new Multiset<String>();
                        heapsByClient.put(command.getSession(), heapsForThisClient);
                    }

                    long heapId = finalHeapState.getHeapId();

                    final String subscriptionId = finalHeapState.addSubscription(connectedObjectLogExtension, subscription, command.getSession());
                    subscription.addListener(new Subscription.SubscriptionListener() {
                        @Override
                        public void subscriptionClosed(Subscription subscription, Subscription.CloseReason reason) {
                            if (reason == REQUESTED_BY_PUBLISHER) {
                                PooledServerConnectedObjectManager.this.terminateSubscription(command.getSession(), heapUri, subscriptionId, reason);
                            }
                            // log end regardless of the reason
                            finalHeapState.logSubscriptionEnd(subscriptionId, connectedObjectLogExtension, reason);
                        }
                    });
                    boolean newHeapDefinition = heapsForThisClient.count(heapUri) == 0;
                    heapsForThisClient.add(heapUri);

                    NewHeapSubscription response;
                    if (newHeapDefinition) {
                        response = new NewHeapSubscription(heapId, subscriptionId, heapUri);
                    } else {
                        response = new NewHeapSubscription(heapId, subscriptionId);
                    }

                    // first tell the client about the heap
                    ExecutionResult executionResult = new ExecutionResult(response);
                    boolean successfulResponse = commandProcessor.writeSuccessResponse(command, executionResult, context);
                    // so if we couldn't send the response it means we know the client isn't going to have a sub response, which means we need to clean up on our
                    // end so we don't get warnings on the client about receiving updates for something it knows nothing about..
                    if (!successfulResponse) {
                        terminateSubscriptions(command.getSession(), heapUri, INTERNAL_ERROR);
                    }

                    if (newHeapDefinition) {
                        // then add the sub initialisation to the update queue
                        finalHeapState.getQueuedChanges().add(new QueuedHeapChange(new QueuedSubscription(command.getSession(), new InitialUpdate(u))));
                        heapsWaitingForUpdate.add(heapUri);
                    }
                }
            });
        } finally {
            subTermLock.unlock();
            assert heapState != null;
            heapState.getUpdateLock().unlock();
        }
    }

    @Override
    public void terminateSubscription(IoSession session, TerminateSubscription payload) {
        Subscription.CloseReason reason = Subscription.CloseReason.REQUESTED_BY_PUBLISHER;
        try {
            reason = Subscription.CloseReason.valueOf(payload.getCloseReason());
        } catch (IllegalArgumentException iae) {
            // unrecognised reason
        }
        terminateSubscription(session, heapUris.get(payload.getHeapId()), payload.getSubscriptionId(), reason);
    }

    /**
     * Terminates a single subscription to a single heap
     */
    public void terminateSubscription(IoSession session, String heapUri, String subscriptionId, Subscription.CloseReason reason) {
        Lock heapUpdateLock = null;
        try {
            HeapState state = heapStates.get(heapUri);
            if (state != null) {
                heapUpdateLock = state.getUpdateLock();
                heapUpdateLock.lock();
            }
            subTermLock.lock();

            if (state != null) {
                if (!state.isTerminated()) {
                    state.terminateSubscription(session, subscriptionId, reason);
                    // notify client
                    if (reason == REQUESTED_BY_PUBLISHER || reason == Subscription.CloseReason.REQUESTED_BY_PUBLISHER_ADMINISTRATOR) {
                        try {
                            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Notifying client that publisher has terminated subscription %s", subscriptionId);
                            NioUtils.writeEventMessageToSession(session, new TerminateSubscription(state.getHeapId(), subscriptionId, reason.name()), objectIOFactory);
                        } catch (Exception e) {
                            // if we can't tell them about it then something more serious has just happened.
                            // the client will likely find out anyway since this will likely mean a dead session
                            // we'll just log some info to the log to aid any debugging. We won't change the closure reason.
                            LOGGER.info("Error occurred whilst trying to inform client of subscription termination", e);
                            nioLogger.log(NioLogger.LoggingLevel.SESSION, session, "Error occurred whilst trying to inform client of subscription termination, closing session");
                            // we'll request a closure of the session too to make sure everything gets cleaned up, although chances are it's already closed
                            session.close();
                        }
                    }

                    if (state.hasSubscriptions()) {
                        terminateSubscriptions(heapUri, reason);
                    } else if (state.getSubscriptions(session).isEmpty()) {
                        terminateSubscriptions(session, heapUri, reason);
                    }
                }
            }

            Multiset<String> heapsForSession = heapsByClient.get(session);
            if (heapsForSession != null) {
                heapsForSession.remove(heapUri);
                if (heapsForSession.isEmpty()) {
                    terminateSubscriptions(session, reason);
                }
            }
        } finally {
            subTermLock.unlock();
            if (heapUpdateLock != null) {
                heapUpdateLock.unlock();
            }
        }
    }

    /**
     * Terminates all subscriptions to a given heap from a single session
     */
    private void terminateSubscriptions(IoSession session, String heapUri, Subscription.CloseReason reason) {
        terminateSubscriptions(session, heapStates.get(heapUri), heapUri, reason);
    }

    /**
     * Terminates all subscriptions to a given heap from a single session
     */
    private void terminateSubscriptions(IoSession session, HeapState state, String heapUri, Subscription.CloseReason reason) {
        Lock heapUpdateLock = null;
        try {
            if (state != null) {
                heapUpdateLock = state.getUpdateLock();
                heapUpdateLock.lock();
            }
            subTermLock.lock();

            if (state != null) {
                if (!state.isTerminated()) {
                    state.terminateSubscriptions(session, reason);
                    if (state.getSessions().isEmpty()) {
                        terminateSubscriptions(heapUri, reason);
                    }
                }
            }

            Multiset<String> heapsForSession = heapsByClient.get(session);
            if (heapsForSession != null) {
                nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Terminating subscription on %s heaps", heapsForSession.keySet().size());
                heapsForSession.removeAll(heapUri);
                if (heapsForSession.isEmpty()) {
                    terminateSubscriptions(session, reason);
                }
            }
        } finally {
            subTermLock.unlock();
            if (heapUpdateLock != null) {
                heapUpdateLock.unlock();
            }
        }
    }

    /**
     * Terminates all subscriptions for a given client
     */
    private void terminateSubscriptions(IoSession session, Subscription.CloseReason reason) {
        Multiset<String> heapsForThisClient;
        try {
            subTermLock.lock();
            heapsForThisClient = heapsByClient.remove(session);
        } finally {
            subTermLock.unlock();
        }

        if (heapsForThisClient != null) {
            for (String s : heapsForThisClient.keySet()) {
                terminateSubscriptions(session, s, reason);
            }
        }
    }

    /**
     * Terminates all subscriptions for a given heap
     */
    private void terminateSubscriptions(String heapUri, Subscription.CloseReason reason) {
        HeapState state = heapStates.get(heapUri);
        if (state != null) {
            try {
                state.getUpdateLock().lock();
                subTermLock.lock();
                // if someone got here first, don't bother doing the work
                if (!state.isTerminated()) {
                    heapStates.remove(heapUri);
                    heapUris.remove(state.getHeapId());
                    List<IoSession> sessions = state.getSessions();
                    for (IoSession session : sessions) {
                        terminateSubscriptions(session, state, heapUri, reason);
                    }
                    LOGGER.error("Terminating heap state '{}'", heapUri);
                    state.terminate();
                    state.removeListener();
                }
            } finally {
                subTermLock.unlock();
                state.getUpdateLock().unlock();
            }
        }
    }

    private class ConnectedObjectPusher implements Runnable {
        private volatile boolean running = true;

        public void run() {
            try {
                while (running) {
                    try {
                        String uri = heapsWaitingForUpdate.pollFirst(1000, TimeUnit.MILLISECONDS);
                        if (uri == null) {
                            continue;
                        }
                        HeapState heapState = heapStates.get(uri);
                        if (heapState == null) {
                            continue;
                        }
                        Lock lock = heapState.getUpdateLock();
                        // make sure noone else tries to send later updates while we're preparing this one..
                        lock.lock();
                        try {
                            if (heapState.isTerminated()) {
                                LOGGER.error("heapState.isTerminated()");
                                continue;
                            }
                            // cleanly dequeue everything waiting in the queue
                            List<QueuedHeapChange> changes = new LinkedList<QueuedHeapChange>();
                            Iterator<QueuedHeapChange> changeIterator = heapState.getQueuedChanges().iterator();
                            while (changeIterator.hasNext()) {
                                changes.add(changeIterator.next());
                                changeIterator.remove();
                            }
                            // if someone already took all the updates (in a batch), then this could easily happen
                            while (!changes.isEmpty()) {
                                // any subs that occurred mid update stream need to be added to the list of sessions at the right place so they get updates from that point on.
                                Iterator<QueuedHeapChange> it = changes.iterator();
                                while (it.hasNext()) {
                                    QueuedHeapChange next = it.next();
                                    if (next.isSub()) {
                                        QueuedSubscription sub = next.getSub();
                                        IoSession session = sub.getSession();
                                        // send the initial update to this session only
                                        // because we write it with the last update id (because this session has never seen this
                                        //   heap before) the new client will just continue after with everyone else.

                                        long updateId = heapState.getLastUpdateId();
                                        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "ConnectedObjectPusher: Sending initial heap state with updateId = %s for heapId = %s", updateId, heapState.getHeapId());
                                        NioUtils.writeEventMessageToSession(session, new HeapDelta(heapState.getHeapId(), updateId, Collections.<Update>singletonList(sub.getInitialState())), objectIOFactory);

                                        heapState.addSession(session);
                                        it.remove();
                                    } else {
                                        break;
                                    }
                                }

                                // loop through the changes, looking to see if we've got a heap sub in there, if so, send everything before in a batch
                                List<Update> updatesThisCycle = new ArrayList<Update>();
                                it = changes.iterator();
                                while (it.hasNext()) {
                                    QueuedHeapChange next = it.next();
                                    if (next.isUpdate()) {
                                        updatesThisCycle.add(next.getUpdate());
                                        it.remove();
                                    } else {
                                        break;
                                    }
                                }

                                if (!updatesThisCycle.isEmpty()) {
                                    // send all the updates in a set of batch messages to each session that is listening to this heap

                                    // so now we need to split this into the batches that will be sent and send each in turn, for this we need to split based on max batch size
                                    // each message must contain atomic QueuedHeapChanges, but may not contain more than maxUpdateActionsPerMessage, except where required to send an atomic QueuedHeapChange
                                    final int updatesToSendThisCycle = updatesThisCycle.size();
                                    int numQueuedHeapChangesSent = 0;
                                    while (numQueuedHeapChangesSent < updatesToSendThisCycle) {
                                        int numQueuedHeapChangesThisMessage = 0;
                                        int numActionsThisMessage = 0;
                                        List<Update> updatesThisBatch = new ArrayList<Update>();
                                        Iterator<Update> queuedIt = updatesThisCycle.iterator();
                                        while (queuedIt.hasNext()) {
                                            Update u = queuedIt.next();
                                            int actionsThisUpdate = u.getActions().size();
                                            if (numQueuedHeapChangesThisMessage > 0 && numActionsThisMessage + actionsThisUpdate > maxUpdateActionsPerMessage) {
                                                break;
                                            }
                                            updatesThisBatch.add(u);
                                            queuedIt.remove();
                                            numQueuedHeapChangesThisMessage++;
                                            numActionsThisMessage += actionsThisUpdate;
                                        }

                                        // we really only want to serialise this once per protocol version (given that serialisation can change by protocol version)
                                        Set<Byte> protocolVersions = new HashSet<Byte>();
                                        for (IoSession session : heapState.getSessions()) {
                                            protocolVersions.add(CougarProtocol.getProtocolVersion(session));
                                        }
                                        Map<Byte, EventMessage> serialisedUpdatesByProtocolVersion = new HashMap<Byte, EventMessage>();
                                        long updateId = heapState.getNextUpdateId();
                                        for (Byte version : protocolVersions) {
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            CougarObjectOutput out = objectIOFactory.newCougarObjectOutput(baos, version);
                                            out.writeObject(new HeapDelta(heapState.getHeapId(), updateId, updatesThisBatch));
                                            out.flush();
                                            serialisedUpdatesByProtocolVersion.put(version, new EventMessage(baos.toByteArray()));
                                        }
                                        // now write these out for each session
                                        for (IoSession session : heapState.getSessions()) {
                                            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Sending heap delta of size %s and with updateId = %s for heapId = %s", updatesThisBatch.size(), updateId, heapState.getHeapId());
                                            session.write(serialisedUpdatesByProtocolVersion.get(CougarProtocol.getProtocolVersion(session)));
                                        }

                                        numQueuedHeapChangesSent += updatesThisBatch.size();
                                    }

                                }

                                // time to kill the heap..
                                if (!changes.isEmpty() && changes.get(0).isTermination()) {
                                    changes.remove(0);
                                    terminateSubscriptions(uri, REQUESTED_BY_PUBLISHER);
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("error sending updates", e);
                            terminateSubscriptions(uri, INTERNAL_ERROR);
                        } finally {
                            lock.unlock();
                        }
                    } catch (InterruptedException e) {
                        // oh well, prob just an opportunity to break out
                    }
                }
            } catch (Exception e) {
                LOGGER.error("thread died", e);
            } catch (Error e) {
                LOGGER.error("thread died", e);
                throw e;
            }
        }

        public void stop() {
            running = false;
        }
    }

    @Override
    public void sessionOpened(IoSession session) {
    }

    @Override
    public void sessionClosed(IoSession session) {
        nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, session, "Session closed, terminating live subscriptions");
        terminateSubscriptions(session, CONNECTION_CLOSED);
    }

    private class QueuedHeapChange {
        private Update update;
        private QueuedSubscription sub;

        private QueuedHeapChange(Update update) {
            this.update = update;
        }

        private QueuedHeapChange(QueuedSubscription sub) {
            this.sub = sub;
        }

        protected QueuedHeapChange() {
        }

        public Update getUpdate() {
            return update;
        }

        public QueuedSubscription getSub() {
            return sub;
        }

        public boolean isUpdate() {
            return update != null;
        }

        public boolean isSub() {
            return sub != null;
        }

        public boolean isTermination() {
            return false;
        }
    }

    private class HeapTermination extends QueuedHeapChange {
        @Override
        public boolean isTermination() {
            return true;
        }
    }

    private class QueuedSubscription {
        private IoSession session;
        private InitialUpdate initialState;

        private QueuedSubscription(IoSession session, InitialUpdate initialState) {
            this.session = session;
            this.initialState = initialState;
        }

        public IoSession getSession() {
            return session;
        }

        public InitialUpdate getInitialState() {
            return initialState;
        }
    }

    public interface HeapStateMonitoring {

        SortedMap<String, List<String>> getSubscriptionIdsBySessionId();

        long getLastUpdateId();

        int getSubscriptionCount();

        int getSessionCount();
    }

    class HeapState implements HeapStateMonitoring {
        private Lock updateLock = new ReentrantLock();
        private final long heapId;
        private final List<IoSession> sessions = new CopyOnWriteArrayList<IoSession>();
        private final AtomicLong updateIdGenerator = new AtomicLong();
        private UpdateProducingHeapListener listener;
        private final Heap heap;
        private final Queue<QueuedHeapChange> queuedChanges = new ConcurrentLinkedQueue<QueuedHeapChange>();
        private volatile boolean terminated;
        private final Map<String, SubscriptionDetails> subscriptions = new HashMap<String, SubscriptionDetails>();
        private final Map<IoSession, List<String>> sessionSubscriptions = new HashMap<IoSession, List<String>>();
        private final long instanceId = heapStateInstanceIdSource.incrementAndGet();

        @Override
        public SortedMap<String, List<String>> getSubscriptionIdsBySessionId() {
            SortedMap<String, List<String>> ret = new TreeMap<String, List<String>>();
            try {
                updateLock.lock();
                subTermLock.lock();

                for (IoSession key : sessionSubscriptions.keySet()) {
                    String sessionId = NioUtils.getSessionId(key);
                    ret.put(sessionId, sessionSubscriptions.get(key));
                }
            } finally {
                // make damn certain both locks are unlocked
                try {
                    updateLock.unlock();
                }
                finally {
                    subTermLock.unlock();
                }
            }

            return ret;
        }

        @Override
        public long getLastUpdateId() {
            return updateIdGenerator.get();
        }

        @Override
        public int getSubscriptionCount() {
            return subscriptions.size();
        }

        @Override
        public int getSessionCount() {
            return sessions.size();
        }

        public HeapState(Heap heap) {
            this.heap = heap;
            heapId = heapIdGenerator.incrementAndGet();
        }

        public Queue<QueuedHeapChange> getQueuedChanges() {
            return queuedChanges;
        }

        public Lock getUpdateLock() {
            return updateLock;
        }

        public long getHeapId() {
            return heapId;
        }

        public List<IoSession> getSessions() {
            return sessions;
        }

        public long getNextUpdateId() {
            return updateIdGenerator.incrementAndGet();
        }

        public void addSession(IoSession session) {
            if (!sessions.contains(session)) {
                sessions.add(session);
            }
        }

        public void removeSession(IoSession session) {
            sessions.remove(session);
        }

        public void setHeapListener(UpdateProducingHeapListener listener) {
            this.listener = listener;
        }

        public void removeListener() {
            heap.removeListener(listener);
        }

        public void terminate() {
            terminated = true;
        }

        public boolean isTerminated() {
            return terminated;
        }

        public String addSubscription(LogExtension logExtension, Subscription subscription, IoSession session) {
            SubscriptionDetails details = new SubscriptionDetails();
            details.logExtension = logExtension;
            details.subscription = subscription;

            String id = uuidGenerator.getNextUUID();
            subscriptions.put(id, details);
            List<String> subscriptionIds = sessionSubscriptions.get(session);
            if (subscriptionIds == null) {
                subscriptionIds = new ArrayList<String>();
                sessionSubscriptions.put(session, subscriptionIds);
            }
            subscriptionIds.add(id);

            logSubscriptionStart(id, logExtension);

            return id;
        }

        private void logSubscriptionStart(String subscriptionId, LogExtension extension) {
            log(subscriptionId, heap.getUri(), "SUBSCRIPTION_START", extension);
        }

        public void logSubscriptionEnd(String subscriptionId, LogExtension extension, Subscription.CloseReason reason) {
            if (reason == null) {
                String message = "Close reason not provided for subscription " + subscriptionId + " to heap " + heap.getUri() + " defaulting to INTERNAL_ERROR";
                LOGGER.warn(message, new IllegalStateException()); // so we can trace the source later..
                reason = INTERNAL_ERROR;
            }
            log(subscriptionId, heap.getUri(), reason.name(), extension);
        }

        private void log(String subscriptionId, String heapUri, String closeReason, LogExtension logExtension) {
            Object[] fieldsToLog = logExtension != null ? logExtension.getFieldsToLog() : null;
            ConnectedObjectLogEvent connectedObjectLogEvent = new ConnectedObjectLogEvent(
                "PUSH_SUBSCRIPTION-LOG",
                new Date(),
                subscriptionId,
                heapUri,
                closeReason
            );
            eventLogger.logEvent(connectedObjectLogEvent, fieldsToLog);
        }

        public void terminateSubscriptions(IoSession session, Subscription.CloseReason reason) {
            sessions.remove(session);
            // find each Subscription object for this session and delete all the subs
            List<String> ids = sessionSubscriptions.remove(session);
            if (ids != null) {
                for (String id : ids) {
                    SubscriptionDetails sub = subscriptions.remove(id);
                    try {
                        sub.subscription.close(reason);
                    } catch (Exception e) {
                        LOGGER.warn("Error trying to close subscription");
                    }
                }
            }
        }

        public boolean hasSubscriptions() {
            return subscriptions.isEmpty();
        }

        public List<String> getSubscriptions(IoSession session) {
            return sessionSubscriptions.get(session);
        }

        public void terminateSubscription(IoSession session, String subscriptionId, Subscription.CloseReason reason) {
            sessionSubscriptions.get(session).remove(subscriptionId);
            try {
                SubscriptionDetails sub = subscriptions.remove(subscriptionId);
                if (reason != REQUESTED_BY_PUBLISHER) {
                    sub.subscription.close(reason);
                }
            } catch (Exception e) {
                LOGGER.warn("Error trying to close subscription");
            }
        }

        // for testing
        Map<String, SubscriptionDetails> getSubscriptions() {
            return subscriptions;
        }

        public long getInstanceId() {
            return instanceId;
        }

        public Heap getHeap() {
            return heap;
        }

        // package private for testing
        class SubscriptionDetails {
            Subscription subscription;
            LogExtension logExtension;
        }
    }

    @ManagedAttribute(description = "Number of active heaps")
    public int getNumberOfHeaps() {
        if (heapUris != null) {
            return heapUris.size();
        }
        return 0;
    }

    @ManagedOperation(description = "Number of subscriptions for the given heap URI")
    public int getHeapSubscriptionCount(String heapUri) {
        if (heapStates != null) {
            final HeapState heapState = heapStates.get(heapUri);
            if (heapState != null) {
                return heapState.getSubscriptionCount();
            }
        }
        return -1;
    }

    @ManagedOperation(description = "Number of sessions subscribed for the given heap URI")
    public int getHeapSessionCount(String heapUri) {
        if (heapStates != null) {
            final HeapState heapState = heapStates.get(heapUri);
            if (heapState != null) {
                return heapState.getSessionCount();
            }
        }
        return -1;
    }

    @ManagedOperation(description = "Has the specified heap terminated")
    public boolean hasHeapTerminated(String heapUri) {
        if (heapStates != null) {
            final HeapState heapState = heapStates.get(heapUri);
            if (heapState != null) {
                return heapState.isTerminated();
            }
        }
        return true;
    }

    @ManagedOperation(description = "Last received update Id")
    public long getLastUpdateId(String heapUri) {
        if (heapStates != null) {
            final HeapState heapState = heapStates.get(heapUri);
            if (heapState != null) {
                return heapState.getLastUpdateId();
            }
        }
        return -1;
    }

    @ManagedOperation(description = "Number of updates queued for the specified heap")
    public long showNumOfQueuedChanges(String heapUri) {
        if (heapStates != null) {
            final HeapState heapState = heapStates.get(heapUri);
            if (heapState != null) {
                return heapState.getQueuedChanges().size();
            }
        }
        return -1;
    }

    @ManagedAttribute(description = "Number of pusher threads")
    public int getNumProcessingThreads() {
        return numProcessingThreads;
    }

    static class Multiset<T> {

        private final Map<T, Integer> map = new HashMap<T, Integer>();

        public boolean add(T val) {
            Integer count = nullToZero(map.get(val));
            map.put(val, count + 1);
            return true;
        }

        public boolean remove(T val) {
            Integer count = map.get(val);
            if (count == null) {
                return false;
            } else {
                if (count == 1) {
                    map.remove(val);
                } else {
                    map.put(val, count - 1);
                }
                return true;
            }
        }

        public int removeAll(T val) {
            return nullToZero(map.remove(val));
        }

        public int count(T val) {
            return nullToZero(map.get(val));
        }

        public Set<T> keySet() {
            return map.keySet();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        private int nullToZero(Integer i) {
            return i == null ? 0 : i;
        }
    }
}