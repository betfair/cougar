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

package com.betfair.cougar.netutil.nio.monitoring;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
*/
public class HostWriteQueueMonitor implements HostWriteQueueMonitorMBean {
    private List<SessionWriteQueueMonitor> sessionWriteQueueMonitors = new CopyOnWriteArrayList<SessionWriteQueueMonitor>();

    private String hostname;

    public HostWriteQueueMonitor(String hostname) {
        this.hostname = hostname;
    }

    ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost="+hostname);
    }

    public long getTotalWriteQueueDepth() {
        long ret = 0;
        for (SessionWriteQueueMonitor sessionMonitor : sessionWriteQueueMonitors) {
            ret += sessionMonitor.getQueueDepth();
        }
        return ret;
    }

    @Override
    public int getNumSessions() {
        return sessionWriteQueueMonitors.size();
    }

    @Override
    public long getMeanWriteQueueDepth() {
        return getTotalWriteQueueDepth()/getNumSessions();
    }

    @Override
    public long getMinWriteQueueDepth() {
        long ret = Long.MAX_VALUE;
        for (SessionWriteQueueMonitor sessionMonitor : sessionWriteQueueMonitors) {
            ret = Math.min(ret, sessionMonitor.getQueueDepth());
        }
        return ret;
    }

    @Override
    public long getMaxWriteQueueDepth() {
        long ret = 0;
        for (SessionWriteQueueMonitor sessionMonitor : sessionWriteQueueMonitors) {
            ret = Math.max(ret, sessionMonitor.getQueueDepth());
        }
        return ret;
    }

    public void addSessionMonitor(SessionWriteQueueMonitor monitor) {
        sessionWriteQueueMonitors.add(monitor);
    }

    public void removeSessionMonitor(SessionWriteQueueMonitor monitor) {
        sessionWriteQueueMonitors.remove(monitor);
    }

    public boolean isEmpty() {
        return sessionWriteQueueMonitors.isEmpty();
    }

    @Override
    public String toString() {
        return "HostWriteQueueMonitor{" +
                "sessionWriteQueueMonitors=" + sessionWriteQueueMonitors.size() +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
