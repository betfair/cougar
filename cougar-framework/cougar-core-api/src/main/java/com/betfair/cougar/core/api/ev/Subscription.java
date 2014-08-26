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

package com.betfair.cougar.core.api.ev;

/**
 * Represents a subscription, either to a push stream (connected object/operation) or a blind subscription to an event destination
 */
public interface Subscription {

    /**
     * Called by a consumer to unsubscribe from the stream of events this subscription is for.
     */
    void close();

    /**
     * Generic close mechanism (new)
     */
    void close(CloseReason reason);

    void addListener(SubscriptionListener listener);
    void removeListener(SubscriptionListener listener);

    /**
     * Get the reason this subcription was close. Returns null if the subscription is still active
     */
    CloseReason getCloseReason();

    // todo: part of US37900
    /**
     * Time the current subscription lease expires, Returns null if there is no expiry.
     */
//    Date getLeaseExpiry();

    public static interface SubscriptionListener {

        /**
         * Called when a subscription is closed
         * @param subscription The closed subscription
         */
        void subscriptionClosed(Subscription subscription, CloseReason reason);
    }

    enum CloseReason { REQUESTED_BY_SUBSCRIBER, REQUESTED_BY_PUBLISHER, INTERNAL_ERROR, NODE_SHUTDOWN, REQUESTED_BY_PUBLISHER_ADMINISTRATOR, REQUESTED_BY_SUBSCRIBER_ADMINISTRATOR, CONNECTION_CLOSED/*, LEASE_EXPIRED*/}
}
