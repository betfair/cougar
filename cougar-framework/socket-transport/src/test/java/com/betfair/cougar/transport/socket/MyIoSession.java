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

package com.betfair.cougar.transport.socket;

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

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static junit.framework.Assert.fail;

/**
*/
public class MyIoSession implements IoSession {

    private String sessionId;
    private List<Object> written = new ArrayList<Object>();
    private Lock writeLock = new ReentrantLock();
    private CountDownLatch latch;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private boolean closed;
    private boolean throwExceptionOnNextWrite;

    MyIoSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<Object> getWritten() {
        return written;
    }

    @Override
    public IoService getService() {
        return null;
    }

    @Override
    public IoServiceConfig getServiceConfig() {
        return null;
    }

    @Override
    public IoHandler getHandler() {
        return null;
    }

    @Override
    public IoSessionConfig getConfig() {
        return null;
    }

    @Override
    public IoFilterChain getFilterChain() {
        return null;
    }

    @Override
    public WriteFuture write(Object o) {
        try {
            writeLock.lock();
            if (throwExceptionOnNextWrite) {
                throw new RuntimeException();
            }
            DefaultWriteFuture f = new DefaultWriteFuture(this);
            f.setWritten(!closed);
            if (!closed) {
                written.add(o);
                if (latch != null) {
                    latch.countDown();
                }
            }
            return f;
        }
        finally {
            writeLock.unlock();
        }
    }

    @Override
    public CloseFuture close() {
        try {
            writeLock.lock();
            closed = true;
            DefaultCloseFuture f = new DefaultCloseFuture(this);
            f.setClosed();
            return f;
        }
        finally {
            writeLock.unlock();
        }
    }

    @Override
    public Object getAttachment() {
        return null;
    }

    @Override
    public Object setAttachment(Object o) {
        return null;
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
    public Object setAttribute(String s) {
        return setAttribute(s, null);
    }

    @Override
    public Object removeAttribute(String s) {
        return attributes.remove(s);
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
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public CloseFuture getCloseFuture() {
        return null;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public SocketAddress getServiceAddress() {
        return null;
    }

    @Override
    public int getIdleTime(IdleStatus idleStatus) {
        return 0;
    }

    @Override
    public long getIdleTimeInMillis(IdleStatus idleStatus) {
        return 0;
    }

    @Override
    public void setIdleTime(IdleStatus idleStatus, int i) {

    }

    @Override
    public int getWriteTimeout() {
        return 0;
    }

    @Override
    public long getWriteTimeoutInMillis() {
        return 0;
    }

    @Override
    public void setWriteTimeout(int i) {

    }

    @Override
    public TrafficMask getTrafficMask() {
        return null;
    }

    @Override
    public void setTrafficMask(TrafficMask trafficMask) {

    }

    @Override
    public void suspendRead() {

    }

    @Override
    public void suspendWrite() {

    }

    @Override
    public void resumeRead() {

    }

    @Override
    public void resumeWrite() {

    }

    @Override
    public long getReadBytes() {
        return 0;
    }

    @Override
    public long getWrittenBytes() {
        return 0;
    }

    @Override
    public long getReadMessages() {
        return 0;
    }

    @Override
    public long getWrittenMessages() {
        return 0;
    }

    @Override
    public long getWrittenWriteRequests() {
        return 0;
    }

    @Override
    public int getScheduledWriteRequests() {
        return 0;
    }

    @Override
    public int getScheduledWriteBytes() {
        return 0;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public long getLastIoTime() {
        return 0;
    }

    @Override
    public long getLastReadTime() {
        return 0;
    }

    @Override
    public long getLastWriteTime() {
        return 0;
    }

    @Override
    public boolean isIdle(IdleStatus idleStatus) {
        return false;
    }

    @Override
    public int getIdleCount(IdleStatus idleStatus) {
        return 0;
    }

    @Override
    public long getLastIdleTime(IdleStatus idleStatus) {
        return 0;
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

    public void awaitWrite(int numWrites, long timeoutMillis, Long... waitAfter) throws InterruptedException {
        try {
            writeLock.lock();
            if (written.size() >= numWrites) {
                return;
            }

            latch = new CountDownLatch(numWrites - written.size());
        }
        finally {
            writeLock.unlock();
        }
        if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            fail("Didn't attain "+numWrites+" writes in "+timeoutMillis+"ms");
        }
        if (waitAfter.length > 0) {
            for (Long l : waitAfter) {
                try {
                    Thread.sleep(l);
                }
                catch (InterruptedException ie) {
                    // ignore this one
                }
            }
        }
    }

    public void throwExceptionOnNextWrite() {
        throwExceptionOnNextWrite = true;
    }
}
