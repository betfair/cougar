/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

import com.betfair.cougar.client.socket.resolver.NetworkAddressResolver;
import com.betfair.cougar.netutil.InetSocketAddressUtils;
import com.betfair.cougar.util.NetworkAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.betfair.cougar.netutil.InetSocketAddressUtils.createInetSocketAddress;

/**
 * Recycles socket sessions periodically
 * - Opens new sessions to any new endpoints
 * - Closes sessions to inactive endpoints
 */
@ManagedResource
public class SessionRecycler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SessionRecycler.class);

    private IoSessionFactory sessionFactory;
    private NetworkAddressResolver resolver;
    private String hosts;
    private String lastResolvedHosts;
    private String lastOpenedSessions;
    private String lastClosedSessions;
    private Date lastRecycleTimestamp;
    private Date lastCheckTimestamp;
    private Date lastErrorTimestamp;
    private String lastErrorMessage;
    private long sessionRecycleInterval;
    private static final String DEFAULT_PORT = "9003";

    public SessionRecycler(IoSessionFactory sessionFactory, NetworkAddressResolver resolver, String hosts, long sessionRecycleInterval) {
        this.sessionFactory = sessionFactory;
        this.resolver = resolver;
        this.hosts = hosts;
        this.sessionRecycleInterval = sessionRecycleInterval;
    }

    public void initialise() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Session Recycler for " + hosts);
                t.setDaemon(true);
                return t;
            }
        }).scheduleAtFixedRate(this, 0, sessionRecycleInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        recycleSessions();
    }

    @ManagedOperation
    public void recycleSessions() {
        try {
            logger.debug("Sessions recycle started");
            List<String> resolvedEndpoints = getResolvedEndpoints();
            lastResolvedHosts = resolvedEndpoints.toString();

            final Set<String> currentEndpoints = getCurrentEndpoints();

            if (logger.isDebugEnabled()) {
                logger.debug("Configured endpoints are " + hosts);
                logger.debug("Resolved endpoints are " + resolvedEndpoints);
                logger.debug("Current endpoints are " + currentEndpoints);
            }

            List<String> sessionsToOpen = diff(resolvedEndpoints, currentEndpoints);
            List<String> sessionsToClose = diff(currentEndpoints, resolvedEndpoints);

            if (!sessionsToOpen.isEmpty() || !sessionsToClose.isEmpty()) {
                logger.info("Sessions to Open : " + sessionsToOpen);
                logger.info("Sessions to Close : " + sessionsToClose);

                lastOpenedSessions = sessionsToOpen.toString();
                lastClosedSessions = sessionsToClose.toString();

                for (String endPoint : sessionsToOpen) {
                    sessionFactory.openSession(createInetSocketAddress(endPoint));
                }

                for (String endPoint : sessionsToClose) {
                    sessionFactory.closeSession(createInetSocketAddress(endPoint), false);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Sessions recycle completed");
                }
                lastRecycleTimestamp = new Date();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No change to any resolved endpoints detected");
                }
            }
            lastCheckTimestamp = new Date();
        } catch (Exception ex) {
            lastErrorTimestamp = new Date();
            lastErrorMessage = ex.getMessage();
            logger.error("Error while recycling sessions ", ex);
        }
    }

    private Set<String> getCurrentEndpoints() {
        Set<String> result = new HashSet();
        for (SocketAddress socketAddress : sessionFactory.getCurrentSessionAddresses()) {
            result.add(InetSocketAddressUtils.asString(socketAddress));
        }
        return result;
    }

    /**
     * Returns a list of elements in the first collection which are not present in the second collection
     *
     * @param first  First collection
     * @param second Second collection
     * @return Difference between the two collections
     */
    private List<String> diff(Collection<String> first, Collection<String> second) {
        final ArrayList<String> list = new ArrayList<String>(first);
        list.removeAll(second);
        return list;
    }

    private List<String> getResolvedEndpoints() {
        List<String> endpoints = new ArrayList<String>();
        for (String url : hosts.split(",")) {
            String host = "";
            String defaultPort = DEFAULT_PORT;
            try {
                if (url.startsWith("http")) {
                    host = url.trim();
                } else {
                    String[] parts = url.trim().split(":");
                    host = parts[0];
                    if (parts.length > 1) {
                        defaultPort = parts[1];
                    }
                }

                Set<String> resolvedAddresses = resolver.resolve(host);

                for (String resolvedAddress : resolvedAddresses) {
                    String[] split = resolvedAddress.trim().split(":");
                    String serverIPAddress = split[0];
                    String serverPort = (split.length > 1 ? split[1] : defaultPort);
                    if (NetworkAddress.isValidIPAddress(serverIPAddress)) {
                        endpoints.add(serverIPAddress + ":" + serverPort);
                    }
                }
            } catch (Exception ex) {
                logger.error("Unable to resolve host : " + host, ex);
            }
        }
        return endpoints;
    }

    @ManagedAttribute
    public String getHosts() {
        return this.hosts;
    }

    @ManagedAttribute
    public String getLastResolvedHosts() {
        return lastResolvedHosts;
    }

    @ManagedAttribute
    public String getLastOpenedSessions() {
        return lastOpenedSessions;
    }

    @ManagedAttribute
    public String getLastClosedSessions() {
        return lastClosedSessions;
    }

    @ManagedAttribute
    public Date getLastErrorTimestamp() {
        return lastErrorTimestamp;
    }

    @ManagedAttribute
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @ManagedAttribute
    public long getSessionRecycleInterval() {
        return sessionRecycleInterval;
    }

    @ManagedAttribute
    public Date getLastRecycleTimestamp() {
        return lastRecycleTimestamp;
    }

    @ManagedAttribute
    public Date getLastCheckTimestamp() {
        return lastCheckTimestamp;
    }
}
