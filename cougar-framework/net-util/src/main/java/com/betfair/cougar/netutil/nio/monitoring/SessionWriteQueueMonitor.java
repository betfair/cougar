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

import org.apache.mina.common.IoSession;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class SessionWriteQueueMonitor implements SessionWriteQueueMonitorMBean {

    private AtomicLong queueDepth = new AtomicLong();
    private IoSession session;

    public SessionWriteQueueMonitor(IoSession session) {
        this.session = session;
    }

    @Override
    public long getQueueDepth() {
        return queueDepth.get();
    }

    String getRemoteHost() {
        InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
        // todo: replace with socketAddress.getHostAddress() when we move to java 1.7
        String host = socketAddress.getHostName();
        if (host == null) {
            host = socketAddress.getAddress().getHostAddress();
        }
        return host;
    }

    ObjectName getObjectName() throws MalformedObjectNameException {
        InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
        String host = getRemoteHost();
        return new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress="+host+"_"+socketAddress.getPort());
    }

    public long countIn() {
        return queueDepth.incrementAndGet();
    }

    public long countOut() {
        return queueDepth.decrementAndGet();
    }
}
