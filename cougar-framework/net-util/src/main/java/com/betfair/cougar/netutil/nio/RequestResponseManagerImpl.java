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

package com.betfair.cougar.netutil.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.message.RequestMessage;
import com.betfair.cougar.netutil.nio.message.ResponseMessage;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 *
 */
public class RequestResponseManagerImpl extends IoHandlerAdapter implements RequestResponseManager {

	private static final Logger LOG = LoggerFactory.getLogger(RequestResponseManagerImpl.class);

    private final IoSession session;
    private AtomicLong correlationIdGenerator = new AtomicLong();
    private Map<Long, WaitingResponseHandler> callbacks = new ConcurrentHashMap<Long, WaitingResponseHandler>();
    private volatile boolean broken = false;
    private NioLogger nioLogger;
    private long rpcTimeoutMillis; // 0 = disabled (by default)

    public RequestResponseManagerImpl(IoSession session, NioLogger nioLogger, long rpcTimeoutMillis) {
        this.session = session;
        this.nioLogger = nioLogger;
        this.rpcTimeoutMillis = rpcTimeoutMillis;
    }

    public void checkForExpiredRequests() {
        // do this in 2 steps so we don't need a lock around the map access
        Set<Long> expiredCorrelationIds = new HashSet<Long>();
        long now = System.currentTimeMillis();
        for (Long key : callbacks.keySet()) {
            WaitingResponseHandler handler = callbacks.get(key);
            if (handler != null && handler.expiryTime < now) {
                expiredCorrelationIds.add(key);
            }
        }

        for (Long key : expiredCorrelationIds) {
            WaitingResponseHandler handler = callbacks.remove(key);
            // response might have come back in between
            if (handler != null) {
                handler.handler.timedOut();
            }
        }
    }

    @Override
    public int getOutstandingRequestCount() {
        return callbacks.size();
    }

    @Override
    public long sendRequest(byte[] message, ResponseHandler handler) throws IOException {
        if (!broken) {
            long correlationId = correlationIdGenerator.incrementAndGet();
            RequestMessage req = new RequestMessage(correlationId, message);
            callbacks.put(correlationId, new WaitingResponseHandler(getExpiryTime(), handler));

            session.write(req);
            return correlationId;
        }
        else {
            throw new IOException("This RequestResponseManager is broken, most likely cause is the session has been terminated");
        }
    }

    private long getExpiryTime() {
        if (rpcTimeoutMillis == 0) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() + rpcTimeoutMillis;
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        ResponseMessage resp = (ResponseMessage) message;
        WaitingResponseHandler handler = callbacks.remove(resp.getCorrelationId());
        // could be null if it already timed out
        if (handler != null) {
            handler.handler.responseReceived(resp);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        if (cause instanceof IOException) {
            LOG.debug("IO exception from session "+NioUtils.getSessionId(session), cause);
        } else {
            LOG.warn("Unexpected exception from session "+NioUtils.getSessionId(session), cause);
        }
        nioLogger.log(NioLogger.LoggingLevel.SESSION, session, "RequestResponseManager - %s received: %s - closing session", cause.getClass().getSimpleName(), cause.getMessage());
        session.close();
    }

    @Override
    public void sessionClosed(IoSession session) {
        broken = true;
        final LinkedList<WaitingResponseHandler> callbackList = new LinkedList<WaitingResponseHandler>(callbacks.values());
        callbacks.clear();

        for (WaitingResponseHandler handler : callbackList) {
            handler.handler.sessionClosed();
        }
        LOG.info("Notified "+callbackList.size() +" outstanding requests for session "+NioUtils.getSessionId(session));
    }

    private class WaitingResponseHandler {
        long expiryTime;
        ResponseHandler handler;

        private WaitingResponseHandler(long expiryTime, ResponseHandler handler) {
            this.expiryTime = expiryTime;
            this.handler = handler;
        }
    }
}
