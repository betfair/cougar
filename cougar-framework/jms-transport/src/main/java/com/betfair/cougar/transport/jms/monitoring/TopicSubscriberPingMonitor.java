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

import com.betfair.tornjak.monitor.OnDemandMonitor;
import com.betfair.tornjak.monitor.Status;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 *
 */
@ManagedResource
public class TopicSubscriberPingMonitor extends OnDemandMonitor {

    private final String transportIdentifier;
    private final String destinationName;
    private final String subscriptionId;
    private final long pingWarningTimeout;
    private final long pingFailureTimeout;
    private volatile long lastPingReceivedTime;
    private volatile long lastPingTransmissionTime;
    private volatile long totalPingTransmissionTime;
    private volatile long pingReceivedCount;

    public TopicSubscriberPingMonitor(String transportIdentifier, String destinationName, String subscriptionId, long pingWarningTimeout, long pingFailureTimeout, Status maxStatus) {
        this.transportIdentifier = transportIdentifier;
        this.destinationName = destinationName;
        this.subscriptionId = subscriptionId;
        if (pingFailureTimeout < pingWarningTimeout) {
            throw new IllegalArgumentException("pingFailureTimeout cannot be less than pingWarningTimeout");
        }
        this.pingWarningTimeout = pingWarningTimeout;
        this.pingFailureTimeout = pingFailureTimeout;
        setMaxImpactToOverallStatus(maxStatus);
    }

    @Override
    @ManagedAttribute
    public String getName() {
        String ret = "TopicSubscriberPingMonitor-";
        if (transportIdentifier != null) {
            ret += transportIdentifier+"-";
        }
        ret+=destinationName;
        if (subscriptionId != null) {
            ret += "["+subscriptionId+"]";
        }
        return ret;
    }

    public synchronized void pingReceived(PingEvent ping) {
        long now = System.currentTimeMillis();
        lastPingReceivedTime = now;
        long transmissionDuration = now - ping.getEmissionTime();
        lastPingTransmissionTime = transmissionDuration;
        totalPingTransmissionTime += transmissionDuration;
        pingReceivedCount ++;
    }

    @Override
    protected synchronized Status checkStatus() {
        long timeSinceLastPing = System.currentTimeMillis() - lastPingReceivedTime;
        if (timeSinceLastPing > pingFailureTimeout) {
            return Status.FAIL;
        }
        if (timeSinceLastPing > pingWarningTimeout) {
            return Status.WARN;
        }
        return Status.OK;
    }

    @ManagedAttribute
    public String getStatusAsString() {
        return checkStatus().toString();
    }

    @ManagedAttribute
    public String getDestinationName() {
        return destinationName;
    }

    @ManagedAttribute
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @ManagedAttribute
    public long getPingWarningTimeout() {
        return pingWarningTimeout;
    }

    @ManagedAttribute
    public long getPingFailureTimeout() {
        return pingFailureTimeout;
    }

    @ManagedAttribute
    public long getLastPingReceivedTime() {
        return lastPingReceivedTime;
    }

    @ManagedAttribute
    public long getLastPingTransmissionTime() {
        return lastPingTransmissionTime;
    }

    @ManagedAttribute
    public long getTotalPingTransmissionTime() {
        return totalPingTransmissionTime;
    }

    @ManagedAttribute
    public long getPingReceivedCount() {
        return pingReceivedCount;
    }
}
