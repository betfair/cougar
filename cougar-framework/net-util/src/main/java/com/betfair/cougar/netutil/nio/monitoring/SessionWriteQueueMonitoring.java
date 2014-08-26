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

import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.NioUtils;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.support.MBeanServerFactoryBean;

import javax.management.MBeanServer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class SessionWriteQueueMonitoring extends IoFilterAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SessionWriteQueueMonitor.class);

    private static final ConcurrentHashMap<String, SessionWriteQueueMonitor> monitors = new ConcurrentHashMap<String, SessionWriteQueueMonitor>();
    private static final Map<String, HostWriteQueueMonitor> hostMonitors = new HashMap<String, HostWriteQueueMonitor>();
    private static MBeanServer mBeanServer;

    static {
        MBeanServerFactoryBean factoryBean = new MBeanServerFactoryBean();
        factoryBean.setLocateExistingServerIfPossible(true);
        factoryBean.afterPropertiesSet();
        mBeanServer = (MBeanServer) factoryBean.getObject();
    }

    // for testing
    static void setMBeanServer(MBeanServer mBeanServer) {
        SessionWriteQueueMonitoring.mBeanServer = mBeanServer;
    }

    public static SessionWriteQueueMonitor getSessionMonitor(String sessionId) {
        return monitors.get(sessionId);
    }

    private NioLogger logger;
    private long maxWriteQueueSize;

    public SessionWriteQueueMonitoring(NioLogger logger, long maxWriteQueueSize) {
        this.logger = logger;
        this.maxWriteQueueSize = maxWriteQueueSize;
    }

    @Override
    public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
        SessionWriteQueueMonitor monitor = new SessionWriteQueueMonitor(session);
        final String sessionId = NioUtils.getSessionId(session);
        if (monitors.putIfAbsent(sessionId, monitor) == null) {
            try {
                mBeanServer.registerMBean(monitor, monitor.getObjectName());
            } catch (Exception e) {
                LOG.error("Error registering mbean", e);
            }
            synchronized (hostMonitors) {
                HostWriteQueueMonitor hostMonitor = hostMonitors.get(monitor.getRemoteHost());
                if (hostMonitor == null) {
                    hostMonitor = new HostWriteQueueMonitor(monitor.getRemoteHost());
                    hostMonitors.put(monitor.getRemoteHost(), hostMonitor);
                    try {
                        mBeanServer.registerMBean(hostMonitor, hostMonitor.getObjectName());
                    } catch (Exception e) {
                        LOG.error("Error registering mbean", e);
                    }
                }
                hostMonitor.addSessionMonitor(monitor);
            }
        }

        // continue on..
        nextFilter.sessionOpened(session);
    }

    @Override
    public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
        final String sessionId = NioUtils.getSessionId(session);
        SessionWriteQueueMonitor monitor = monitors.remove(sessionId);
        try {
            mBeanServer.unregisterMBean(monitor.getObjectName());
        } catch (Exception e) {
            LOG.error("Error unregistering mbean", e);
        }
        synchronized (hostMonitors) {
            HostWriteQueueMonitor hostMonitor = hostMonitors.get(monitor.getRemoteHost());
            if (hostMonitor != null) {
                hostMonitor.removeSessionMonitor(monitor);
                if (hostMonitor.isEmpty()) {
                    hostMonitors.remove(monitor.getRemoteHost());
                    try {
                        mBeanServer.unregisterMBean(hostMonitor.getObjectName());
                    } catch (Exception e) {
                        LOG.error("Error unregistering mbean", e);
                    }
                }
            }
        }

        nextFilter.sessionClosed(session);
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        // if we want to terminate based on queue depth then check it..
        SessionWriteQueueMonitor monitor = monitors.get(NioUtils.getSessionId(session));
        if (monitor != null) {
            long newDepth = monitor.countIn();
            if (maxWriteQueueSize > 0 && newDepth > maxWriteQueueSize) {
                logger.log(NioLogger.LoggingLevel.SESSION, session, "Session exceeded max writeQueue size of %s, closing session", maxWriteQueueSize);
                // kill
                session.close();
                return;
            }
        }

        nextFilter.filterWrite(session, writeRequest);
    }

    @Override
    public void messageSent(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        // count out
        SessionWriteQueueMonitor monitor = monitors.get(NioUtils.getSessionId(session));
        if (monitor != null) {
            monitor.countOut();
        }

        nextFilter.messageSent(session, message);
    }
}
