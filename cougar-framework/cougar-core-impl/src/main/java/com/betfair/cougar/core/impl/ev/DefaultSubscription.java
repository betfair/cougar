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

package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.core.api.ev.Subscription;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility default subscription class which handles listener interaction. Any subclass wanting to trigger specific functionality
 * around closure should override either preClose(CloseReason) or postClose(CloseReason) as appropriate.
 */
public class DefaultSubscription implements Subscription {

    private List<SubscriptionListener> listeners = new CopyOnWriteArrayList<SubscriptionListener>();
    private volatile CloseReason closeReason;

    @Override
    public void addListener(SubscriptionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(SubscriptionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void close() {
        close(CloseReason.REQUESTED_BY_SUBSCRIBER);
    }

    @Override
    public final void close(CloseReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Close reason can't be null");
        }
        preClose(reason);
        onConnectionClosed(reason);
        postClose(reason);
    }

    protected void preClose(CloseReason reason) {
    }

    protected void postClose(CloseReason reason) {
    }

    public CloseReason getCloseReason() {
        return closeReason;
    }

    protected void onConnectionClosed(CloseReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Close reason can't be null");
        }
        this.closeReason = reason;
        for (SubscriptionListener listener : listeners) {
            listener.subscriptionClosed(this, reason);
        }
    }
}
