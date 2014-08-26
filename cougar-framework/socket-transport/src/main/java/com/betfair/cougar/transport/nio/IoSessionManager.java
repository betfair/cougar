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

package com.betfair.cougar.transport.nio;

import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioLogger;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles IoSession life-cycle on server
 * Main concern addressed is to shutdown
 * active sessions gracefully
 */
public class IoSessionManager {

    private final static Logger logger = LoggerFactory.getLogger(ExecutionVenueServerHandler.class);

    private long maxTimeToWaitForRequestCompletion;
    private NioLogger nioLogger;

    public void shutdownSessions(Set<IoSession> ioSessions, CougarProtocol cougarProtocol, ExecutionVenueServerHandler handler) {
        shutdownSessions(ioSessions, cougarProtocol, handler, false);
    }

    public void shutdownSessions(Set<IoSession> ioSessions, CougarProtocol cougarProtocol, ExecutionVenueServerHandler handler, boolean blockUntilComplete) {
        if (maxTimeToWaitForRequestCompletion > 0) { // needs graceful shutdown
            for (final IoSession ioSession : ioSessions) {
                cougarProtocol.suspendSession(ioSession);
            }

            waitForOutstandingRequestsToComplete(handler);
        }

        for (final IoSession ioSession : ioSessions) {
            cougarProtocol.closeSession(ioSession, blockUntilComplete);
        }
    }

    public void setMaxTimeToWaitForRequestCompletion(long maxTimeToWaitForRequestCompletion) {
        this.maxTimeToWaitForRequestCompletion = maxTimeToWaitForRequestCompletion;
    }

    public void setNioLogger(NioLogger nioLogger) {
        this.nioLogger = nioLogger;
    }

    public void waitForOutstandingRequestsToComplete(final ExecutionVenueServerHandler handler) {

        FutureTask<Boolean> future = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                while (handler.getOutstandingRequests() > 0) {
                    Thread.sleep(25);
                }
                return true;
            }
        });

        final Thread thread = new Thread(future, "Outstanding Request Monitor");
        thread.setDaemon(true);
        thread.start();

        try {
            final Boolean result = future.get(maxTimeToWaitForRequestCompletion, TimeUnit.MILLISECONDS);
            logger.info("All outstanding requests completed : "+result);
        }
        catch (TimeoutException e) {
            logger.warn("Not all outstanding requests completed within "+maxTimeToWaitForRequestCompletion+"ms");
        }
        catch (Exception e) {
            logger.warn("Exception while waiting for outstanding requests to complete : ",e);
        }

        if (nioLogger.isLogging(NioLogger.LoggingLevel.TRANSPORT)) {
            nioLogger.log(NioLogger.LoggingLevel.TRANSPORT, "All", "IoSessionManager - " + handler.getOutstandingRequests() + " outstanding requests remaining");
        }
    }
}
