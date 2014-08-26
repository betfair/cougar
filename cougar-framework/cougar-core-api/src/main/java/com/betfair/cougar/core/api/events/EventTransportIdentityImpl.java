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

package com.betfair.cougar.core.api.events;

import com.betfair.cougar.api.security.Credential;
import com.betfair.cougar.core.api.transports.EventTransport;

import java.security.Principal;

/**
 */
public class EventTransportIdentityImpl implements EventTransportIdentity {

    //This is used to identify the transport class
    private String eventTransportName;

    //This is used to uniquely identify the instance of the transport (could be used for multiple instances
    //of the same transport
    private String transportIdentifier;

    public EventTransportIdentityImpl(EventTransport transport) {
        this(transport.getClass().getSimpleName(), "" + transport.hashCode());
    }

    public EventTransportIdentityImpl(EventTransport transport, String transportIdentifier) {
        this(transport.getClass().getSimpleName(), transportIdentifier);
    }

    public EventTransportIdentityImpl(String transportName, String transportIdentifier) {
        this.eventTransportName = transportName;
        this.transportIdentifier = transportIdentifier;
    }

    @Override
    public String getEventTransportName() {
        return eventTransportName;
    }

    @Override
    public String getTransportIdentifier() {
        return transportIdentifier;
    }

    public String getName() {
        return eventTransportName + ":" + transportIdentifier;
    }

    @Override
    public Principal getPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return EventTransportIdentityImpl.this.getName();
            }
        };
    }

    @Override
    public Credential getCredential() {
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("EventTransportIdentityImpl_");

        sb.append("transportIdentifier=").append(transportIdentifier).append("|");
        sb.append("eventTransportName=").append(eventTransportName);

        return sb.toString();
    }
}


