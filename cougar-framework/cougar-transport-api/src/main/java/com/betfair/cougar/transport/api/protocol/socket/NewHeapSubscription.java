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

package com.betfair.cougar.transport.api.protocol.socket;

public class NewHeapSubscription {
    private long heapId;
    private String subscriptionId;
    private String uri;

    public NewHeapSubscription() {
    }

    public NewHeapSubscription(long heapId, String subscriptionId) {
        this.heapId = heapId;
        this.subscriptionId = subscriptionId;
    }

    public NewHeapSubscription(long heapId, String subscriptionId, String uri) {
        this.heapId = heapId;
        this.subscriptionId = subscriptionId;
        this.uri = uri;
    }

    public long getHeapId() {
        return heapId;
    }

    public String getUri() {
        return uri;
    }

    public void setHeapId(long heapId) {
        this.heapId = heapId;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
