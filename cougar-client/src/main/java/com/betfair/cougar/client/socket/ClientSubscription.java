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

import com.betfair.cougar.core.impl.ev.DefaultSubscription;
import org.apache.mina.common.IoSession;

public class ClientSubscription extends DefaultSubscription {

    private ClientConnectedObjectManager ccom;
    private IoSession session;
    private long heapId;
    private String subscriptionId;

    public ClientSubscription(ClientConnectedObjectManager ccom, IoSession session, long heapId, String subscriptionId) {
        this.ccom = ccom;
        this.session = session;
        this.heapId = heapId;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public void preClose(CloseReason reason) {
        ccom.terminateSubscription(session, heapId, subscriptionId, reason);
    }

    // Make public visibility
    @Override
    public void onConnectionClosed(CloseReason reason) {
        super.onConnectionClosed(reason);
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
