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

import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.netutil.nio.HeapDelta;
import com.betfair.cougar.netutil.nio.connected.InitialUpdate;
import com.betfair.platform.virtualheap.Heap;
import org.apache.mina.common.IoSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HeapState {
    private final Heap heap;
    private final BlockingQueue<QueuedHeapDelta> queue = new PriorityBlockingQueue<QueuedHeapDelta>(10, HEAP_DELTA_COMPARATOR);
    private final AtomicLong lastUpdateId = new AtomicLong(-1);
    private final Lock heapUpdateLock = new ReentrantLock();
    private final ConcurrentMap<String, ClientSubscription> subscriptions = new ConcurrentHashMap<String, ClientSubscription>();

    private volatile boolean seenInitialUpdate;

    public HeapState(Heap heap) {
        this.heap = heap;
    }

    public Subscription addSubscription(ClientConnectedObjectManager ccom, IoSession session, long heapId, String subscriptionId) {
        ClientSubscription sub = new ClientSubscription(ccom, session, heapId, subscriptionId);
        ClientSubscription existing = subscriptions.putIfAbsent(subscriptionId, sub);
        if (existing != null) {
            return null;
        }
        return sub;
    }

    public Map<String, ClientSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void terminateSubscription(String subscriptionId, Subscription.CloseReason reason) {
        ClientSubscription sub = subscriptions.remove(subscriptionId);
        if (sub != null) {
            if (reason != Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER) {
                // we don't want to call close() here as that will end up calling back into CCOM and get a deadlock ;)
                sub.onConnectionClosed(reason);
            }
        }
    }

    public void terminateAllSubscriptions(Subscription.CloseReason reason) {
        List<String> keys = new ArrayList<String>(subscriptions.keySet());
        for (String s : keys) {
            terminateSubscription(s, reason);
        }
    }

    public int getSubscriptionCount() {
        return subscriptions.size();
    }

    public long getLastDeltaId() {
        return lastUpdateId.get();
    }

    public void queueUpdate(HeapDelta payload) {
        queue.add(new QueuedHeapDelta(payload));
    }

    public String getHeapUri() {
        return heap.getUri();
    }

    public HeapDelta peekNextDelta() {
        QueuedHeapDelta next = queue.peek();
        return next != null && isNextUpdate(next.delta) ? next.delta : null;
    }

    private boolean isNextUpdate(HeapDelta delta) {
        return delta.getUpdateId() == getNextUpdateId() || isInitialUpdate(delta);
    }

    private boolean isInitialUpdate(HeapDelta delta) {
        if (seenInitialUpdate) {
            return false;
        }
        if (delta.getUpdates().isEmpty()) {
            return false;
        }
        return delta.getUpdates().get(0) instanceof InitialUpdate;
    }

    public boolean haveSeenInitialUpdate() {
        return seenInitialUpdate;
    }

    public long getNextUpdateId() {
        return lastUpdateId.get() + 1;
    }

    public Lock getHeapUpdateLock() {
        return heapUpdateLock;
    }

    public Heap getHeap() {
        return heap;
    }

    public HeapDelta popNextDelta() {
        HeapDelta nextDelta = queue.remove().delta; // We only call this if we know the queue has something
        seenInitialUpdate |= isInitialUpdate(nextDelta);
        lastUpdateId.set(nextDelta.getUpdateId());
        return nextDelta;
    }

    public QueueHealth checkDeltaQueueHealth(int maxQueueSize, long maxWaitTime) {
        // if the delta queue for a heap grows too long then we've lost a message and need to abort/disconnect
        if (queue.size() > maxQueueSize) {
            return QueueHealth.QUEUE_TOO_LONG;
        }

        QueuedHeapDelta first = queue.peek();
        if (first != null
            && first.delta.getUpdateId() > getNextUpdateId()
            && first.queueTime + maxWaitTime < System.currentTimeMillis()) {
            // get the time the next update was queued, and check against that timeout..
            return QueueHealth.WAITED_TOO_LONG;
        }

        return QueueHealth.HEALTHY;
    }

    public static enum QueueHealth {
        HEALTHY,
        QUEUE_TOO_LONG,
        WAITED_TOO_LONG
    }

    private static final Comparator<QueuedHeapDelta> HEAP_DELTA_COMPARATOR = new Comparator<QueuedHeapDelta>() {
        @Override
        public int compare(QueuedHeapDelta o1, QueuedHeapDelta o2) {
            long a = o1.delta.getUpdateId();
            long b = o2.delta.getUpdateId();
            if (a > b) {
                return 1;
            } else if (b > a) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private static final class QueuedHeapDelta {
        private final long queueTime = System.currentTimeMillis();
        private final HeapDelta delta;

        private QueuedHeapDelta(HeapDelta delta) {
            this.delta = delta;
        }
    }
}
