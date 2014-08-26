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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.HandlerListener;
import com.betfair.cougar.netutil.nio.NioUtils;
import com.betfair.cougar.netutil.nio.RequestResponseManager;
import org.apache.mina.common.IoSession;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 *
 */
public class RPCTimeoutChecker implements Runnable, HandlerListener {

    private static final Logger LOG = LoggerFactory.getLogger(RPCTimeoutChecker.class);

    private static final AtomicLong rpcTimeoutCheckerThreadId = new AtomicLong();
    private Thread thread;
    private final List<IoSession> sessions = new LinkedList<IoSession>();
    private volatile boolean running = true;
    private long checkGranularity;

    public RPCTimeoutChecker(long checkGranularity) {
        this.checkGranularity = checkGranularity;
        thread = new Thread(this, "SocketTransport-RPCTimeoutChecker-"+rpcTimeoutCheckerThreadId.incrementAndGet());
        thread.setDaemon(true);
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public void run() {
        while (running) {
            try {
                synchronized (sessions) {
                    for (IoSession session : sessions) {
                        RequestResponseManager requestResponseManager = (RequestResponseManager) session.getAttribute(RequestResponseManager.SESSION_KEY);
                        // can happen if we're called before the session has actual completed handshake with server..
                        if (requestResponseManager != null) {
                            requestResponseManager.checkForExpiredRequests();
                        }
                    }
                }
            }
            catch (Exception e) {
                // make sure a spurious bug doesn't wipe us out..
                LOG.warn("Exception occurred checking for expired requests", e);
            }
            try {
                Thread.sleep(checkGranularity);
            } catch (InterruptedException e) {
                // don't care
            }

        }

    }

    public void stop() {
        running = false;
    }

    @Override
    public void sessionOpened(IoSession session) {
        synchronized (sessions) {
            sessions.add(session);
        }
    }

    @Override
    public void sessionClosed(IoSession session) {
        synchronized (sessions) {
            sessions.remove(session);
        }
    }
}
