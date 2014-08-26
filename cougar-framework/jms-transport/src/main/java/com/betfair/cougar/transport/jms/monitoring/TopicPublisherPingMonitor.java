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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
@ManagedResource
public class TopicPublisherPingMonitor extends OnDemandMonitor {

    private final JmsEventTransportImpl transport;
    private final long pingPeriod;
    private final String destinationName;
    private static final PingEventServiceBindingDescriptor pingEventServiceBindingDescriptor = new PingEventServiceBindingDescriptor();
    private volatile Status status = Status.FAIL;
    private volatile String lastErrorMessage;
    private volatile String lastErrorTrace;
    private volatile long lastSuccessfulEmissionTime;
    private volatile long lastFailedEmissionTime;
    private volatile long pingSuccessCount;
    private volatile long pingFailCount;
    private Timer timer;

    public TopicPublisherPingMonitor(JmsEventTransportImpl transport, long pingPeriod, String destinationName, Status maxImpact) {
        this.transport = transport;
        this.pingPeriod = pingPeriod;
        this.destinationName = destinationName;
        setMaxImpactToOverallStatus(maxImpact);
    }

    @Override
    protected Status checkStatus() throws Exception {
        return status;
    }

    @Override
    @ManagedAttribute
    public String getName() {
        if (transport.getTransportIdentifier() != null) {
            return "TopicPublisherPingMonitor-"+transport.getTransportIdentifier()+"-"+destinationName;
        }
        else {
            return "TopicPublisherPingMonitor-"+destinationName;
        }
    }

    @ManagedAttribute
    public long getPingPeriod() {
        return pingPeriod;
    }

    @ManagedAttribute
    public String getDestinationName() {
        return destinationName;
    }

    @ManagedAttribute
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @ManagedAttribute
    public String getLastErrorTrace() {
        return lastErrorTrace;
    }

    @ManagedAttribute
    public String getStatusAsString() {
        return status.toString();
    }

    @ManagedAttribute
    public long getLastSuccessfulEmissionTime() {
        return lastSuccessfulEmissionTime;
    }

    @ManagedAttribute
    public long getLastFailedEmissionTime() {
        return lastFailedEmissionTime;
    }

    @ManagedAttribute
    public long getPingSuccessCount() {
        return pingSuccessCount;
    }

    @ManagedAttribute
    public long getPingFailCount() {
        return pingFailCount;
    }

    public void connectionOpened() {
        timer = new Timer(getName(), true);
        timer.scheduleAtFixedRate(new PingTask(), 0L, pingPeriod);
    }

    public void connectionClosed() {
        timer.cancel();
    }

    private class PingTask extends TimerTask {
        @Override
        public void run() {
            long emissionTime = System.currentTimeMillis();
            try {
                PingEvent pe = new PingEvent();
                pe.setEmissionTime(emissionTime);
                transport.publish(pe, destinationName, pingEventServiceBindingDescriptor);
                lastSuccessfulEmissionTime = emissionTime;
                pingSuccessCount++;
                status = Status.OK;
            }
            catch (Exception e) {
                status = Status.FAIL;
                lastErrorMessage = e.getMessage();
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                lastErrorTrace = sw.toString();
                lastFailedEmissionTime = emissionTime;
                pingFailCount++;
            }
        }
    }
}
