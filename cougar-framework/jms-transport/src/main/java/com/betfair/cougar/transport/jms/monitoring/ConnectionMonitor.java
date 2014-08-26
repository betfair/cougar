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

package com.betfair.cougar.transport.jms.monitoring;


import com.betfair.cougar.transport.jms.JmsEventTransportImpl;
import com.betfair.tornjak.monitor.OnDemandMonitor;
import com.betfair.tornjak.monitor.Status;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 *
 */
@ManagedResource
public class ConnectionMonitor extends OnDemandMonitor {

    private volatile Status status;
    private final Status statusIfNotConnected;
    private final boolean permanentConnectionExpected;
    private JmsEventTransportImpl transport;

    public ConnectionMonitor(boolean permanentConnectionExpected) {
        this.permanentConnectionExpected = permanentConnectionExpected;
        if (permanentConnectionExpected) {
            statusIfNotConnected = Status.FAIL;
        }
        else {
            statusIfNotConnected = Status.WARN;
        }
        status = statusIfNotConnected;
    }

    public void connectionStarted(javax.jms.Connection c) {
        status = Status.OK;
    }

    public void connectionClosed(javax.jms.Connection c) {
        status = statusIfNotConnected;
    }

    @Override
    protected Status checkStatus() {
        if (status == statusIfNotConnected && permanentConnectionExpected) {
            transport.requestConnectionToBroker();
        }
        return status;
    }

    @ManagedAttribute
    public String getStatusIfNotConnected() {
        return statusIfNotConnected.toString();
    }

    @ManagedAttribute
    public String getStatusAsString() {
        return checkStatus().toString();
    }

    @Override
    @ManagedAttribute
    public String getName() {
        if (transport!=null && transport.getTransportIdentifier()!=null) {
            return "JmsConnectionMonitor-"+transport.getTransportIdentifier();
        }
        else {
            return "JmsConnectionMonitor";
        }
    }

    @ManagedAttribute
    public boolean isPermanentConnectionExpected() {
        return permanentConnectionExpected;
    }

    public void setTransport(JmsEventTransportImpl transport) {
        this.transport = transport;
    }
}
