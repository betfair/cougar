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

import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.TrafficMask;
import org.apache.mina.common.TransportType;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.common.support.DefaultCloseFuture;
import org.apache.mina.common.support.DefaultWriteFuture;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;

public class MyIoSession implements IoSession {

    private String sessionId;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private CountDownLatch closureLatch = new CountDownLatch(1);
    private List<Object> allValuesWritten = new ArrayList<Object>();
    private boolean closed;

    MyIoSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public IoService getService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IoServiceConfig getServiceConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IoHandler getHandler() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IoSessionConfig getConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IoFilterChain getFilterChain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WriteFuture write(Object message) {
        allValuesWritten.add(message);
        DefaultWriteFuture future = new DefaultWriteFuture(this);
        future.setWritten(true);
        return future;
    }

    @Override
    public CloseFuture close() {
        closed = true;
        return getCloseFuture();
    }

    @Override
    public Object getAttachment() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object setAttachment(Object attachment) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getAttribute(String s) {
        return attributes.get(s);
    }

    @Override
    public Object setAttribute(String s, Object o) {
        return attributes.put(s, o);
    }

    @Override
    public Object setAttribute(String key) {
        return setAttribute(key, null);
    }

    @Override
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    @Override
    public boolean containsAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    @Override
    public TransportType getTransportType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isConnected() {
        return !closed;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public CloseFuture getCloseFuture() {
        DefaultCloseFuture future = new DefaultCloseFuture(this);
        if (closed) {
            future.setClosed();
        }
        return future;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return new InetSocketAddress(1234);
    }

    @Override
    public SocketAddress getLocalAddress() {
        return new InetSocketAddress(9001);
    }

    @Override
    public SocketAddress getServiceAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getIdleTime(IdleStatus status) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getIdleTimeInMillis(IdleStatus status) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setIdleTime(IdleStatus status, int idleTime) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getWriteTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getWriteTimeoutInMillis() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setWriteTimeout(int writeTimeout) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TrafficMask getTrafficMask() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTrafficMask(TrafficMask trafficMask) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void suspendRead() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void suspendWrite() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resumeRead() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resumeWrite() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getReadBytes() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getWrittenBytes() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getReadMessages() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getWrittenMessages() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getWrittenWriteRequests() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getScheduledWriteRequests() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getScheduledWriteBytes() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getCreationTime() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLastIoTime() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLastReadTime() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLastWriteTime() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isIdle(IdleStatus status) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getIdleCount(IdleStatus status) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLastIdleTime(IdleStatus status) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyIoSession that = (MyIoSession) o;

        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }

    public void awaitClosure(long millis) throws InterruptedException {
        if (!closureLatch.await(millis, TimeUnit.MILLISECONDS)) {
            fail("Waited "+millis+"ms for closure and it didn't come");
        }
    }

    @Override
    public String toString() {
        return "MyIoSession{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }
}
