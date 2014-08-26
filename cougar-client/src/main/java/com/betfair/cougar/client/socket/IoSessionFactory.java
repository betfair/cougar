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

import com.betfair.cougar.client.socket.resolver.NetworkAddressResolver;
import com.betfair.cougar.netutil.nio.ClientHandshake;
import com.betfair.cougar.netutil.nio.NioConfig;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.NioUtils;
import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import com.betfair.cougar.util.JMXReportingThreadPoolExecutor;
import org.apache.mina.common.*;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.net.SocketAddress;
import java.util.*;

/**
 *
 */
@ManagedResource
public class IoSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(IoSessionFactory.class);
    private final NioLogger logger;

    private int handshakeResponseTimeout;
    private int reconnectInterval;

    private final SocketConnector socketConnector;

    private final Object lock = new Object(); // a lock object to protect access to session list and a  counter
    private volatile int counter = 0;

    // Maintains a list of all endpoints to which connections are established
    private final Map<SocketAddress, IoSession> sessions = new TreeMap<SocketAddress, IoSession>(new AddressComparator());

    private final IoHandler ioHandler;
    private final IoFutureListener sessionClosedListener;
    private final NioConfig nioConfig;

    private volatile boolean keepRunning = false;
    private JMXReportingThreadPoolExecutor reconnectExecutor;
    private final String hosts;
    private SessionRecycler sessionRecycler;
    // Maintains a list of endpoints to which connections are being established
    private Map<SocketAddress, ReconnectTask> pendingConnections = new HashMap<SocketAddress, ReconnectTask>();

    public IoSessionFactory(NioLogger logger,
                            String hosts,
                            JMXReportingThreadPoolExecutor executorService,
                            JMXReportingThreadPoolExecutor reconnectExecutor,
                            NioConfig config,
                            IoHandler ioHandler,
                            IoFutureListener sessionClosedListener,
                            int reconnectInterval,
                            int handshakeResponseTimeout,
                            long sessionRecycleInterval,
                            NetworkAddressResolver addressResolver) {
        this.logger = logger;
        this.reconnectInterval = reconnectInterval;
        this.handshakeResponseTimeout = handshakeResponseTimeout;

        this.hosts = hosts;

        this.nioConfig = config;

        this.socketConnector = new SocketConnector(executorService.getCorePoolSize(), executorService);
        this.socketConnector.setWorkerTimeout(config.getWorkerTimeout());

        this.ioHandler = ioHandler;
        this.sessionClosedListener = sessionClosedListener;

        this.keepRunning = false;
        this.reconnectExecutor = reconnectExecutor;
        sessionRecycler = new SessionRecycler(this, addressResolver, hosts, sessionRecycleInterval);
    }

    public boolean isConnected() {
        synchronized (lock) {
            return !sessions.isEmpty();
        }
    }

    /**
     * Returns a list of all server socket addresses to which sessions are already
     * established or being established
     * @return List of socket addresses
     */
    public Set<SocketAddress> getCurrentSessionAddresses() {
        Set<SocketAddress> result = new HashSet<SocketAddress>();
        synchronized (lock) {
            result.addAll(sessions.keySet());
            result.addAll(pendingConnections.keySet());
        }
        return result;
    }

    public Map<String, String> getConnectedStatus() {
        List<IoSession> tmp;
        synchronized (lock) {
            tmp = new ArrayList<IoSession>(sessions.values());
        }
        final HashMap<String, String> result = new HashMap<String, String>();
        for (IoSession session : tmp) {
            final String sessionId = NioUtils.getSessionId(session);
            StringBuilder buffer = new StringBuilder();
            buffer.append("SessionId=").append(sessionId).append(",")
                    .append("remoteHost=").append(session.getRemoteAddress()).append(",")
                    .append("connected=").append(session.isConnected()).append(",")
                    .append("closing=").append(session.isClosing()).append(",")
                    .append('\n');
            result.put(sessionId, buffer.toString());
        }
        return result;
    }

    public void start() {
        this.keepRunning = true;
        sessionRecycler.initialise();
    }

    public void stop() {
        keepRunning = false; // stop all tasks to reconnect
        ArrayList<IoSession> sessionsSnapshot;
        synchronized (lock) {
            sessionsSnapshot = new ArrayList<IoSession>(sessions.values());
        }
        for (IoSession session : sessionsSnapshot) {  // close each open session
            close(session);
        }
    }

    /**
     * Rotates via list of currently established sessions
     *
     * @return an IO session
     */
    public IoSession getSession() {
        synchronized (lock) {
            if (sessions.isEmpty()) {
                return null;
            } else {
                final Object[] keys = sessions.keySet().toArray();
                for (int i = 0; i < sessions.size(); i++) { //
                    counter++;
                    final int pos = Math.abs(counter % sessions.size());
                    final IoSession session = sessions.get(keys[pos]);
                    if (isAvailable(session)) {
                        return session;
                    }
                }
                return null;
            }
        }
    }

    /**
     * Open a new session to the specified address. If session is being
     * opened does nothing
     *
     * @param endpoint
     */

    public void openSession(SocketAddress endpoint) {
        synchronized (lock) {
            // Submit a reconnect task for this address if one is not already present
            if (!pendingConnections.containsKey(endpoint)) {
                final ReconnectTask task = new ReconnectTask(endpoint);
                pendingConnections.put(endpoint, task);
                this.reconnectExecutor.submit(task);
            }
        }
    }

    /**
     * If there is an active session to the specified endpoint, it will be closed
     * If not the reconnection task for the endpoint will be stopped
     *
     * @param endpoint
     * @param reconnect whether to reconnect after closing the current session.
     *                  Only used if the session is active
     */
    public void closeSession(SocketAddress endpoint, boolean reconnect) {
        synchronized (lock) {
            // Submit a reconnect task for this address if one is not already present
            if (pendingConnections.containsKey(endpoint)) {
                final ReconnectTask task = pendingConnections.get(endpoint);
                if (task != null) {
                    task.stop();
                }
            } else {
                final IoSession ioSession = sessions.get(endpoint);
                if (ioSession != null) {
                    close(ioSession, reconnect);
                }
            }
        }
    }

    boolean isAvailable(IoSession session) {
        return (session.isConnected() // connected
                && !session.isClosing() // close has not been initiated
                && !session.containsAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name()) // suspend message has not been received
                && !session.containsAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name())); // disconnect message has not been received

    }

    private final IoFutureListener serverSideCloseListener =
            new IoFutureListener() {
                @Override
                public void operationComplete(IoFuture future) {
                    IoSessionFactory.this.close(future.getSession());
                }
            };

    public void close(final IoSession aSession) {
        close(aSession, true);
    }

    public void close(final IoSession aSession, boolean reconnect) {
        if (aSession == null) {
            return;
        }

        boolean sessionRemoved = false;
        final SocketAddress remoteAddress = aSession.getRemoteAddress();
        synchronized (lock) {
            final IoSession removed = sessions.remove(remoteAddress);
            sessionRemoved = (removed != null);
        }

        if (sessionRemoved) {
            try {
                if (!aSession.isClosing()) {
                    logger.log(NioLogger.LoggingLevel.SESSION, aSession, "IoSessionFactory - Closing session");
                    aSession.close();
                }
            } finally {
                if (reconnect) {
                    // Submit a reconnect task for this address if one is not already active
                    openSession(remoteAddress);
                }
            }
        }
    }

    public IoSession connect(final SocketAddress endpoint) {

        ConnectFuture cf = null;
        try {
            cf = socketConnector.connect(endpoint, this.ioHandler, this.nioConfig.configureSocketSessionConfig());
        } catch (Exception e) {
            log.info("Error connecting to " + endpoint, e);
        }

        if (cf != null) {
            cf.join();
            if (cf.isConnected()) {
                log.info("Connected to " + endpoint);
                final IoSession session = cf.getSession();

                if (handshake(session)) {
                    final CloseFuture closeFuture = session.getCloseFuture();
                    closeFuture.addListener(this.serverSideCloseListener);
                    closeFuture.addListener(this.sessionClosedListener);
                    return session;
                } else {
                    log.info("Handshake failed for " + endpoint);
                    logger.log(NioLogger.LoggingLevel.SESSION, session, "Handshake failed for %s", endpoint);
                    session.close();
                }
            } else {
                log.info("Failed to connect to " + endpoint);
            }
        }

        return null;
    }

    private boolean handshake(IoSession session) {
        ClientHandshake clientHandshake = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
        clientHandshake.await(handshakeResponseTimeout);
        session.removeAttribute(ClientHandshake.HANDSHAKE); // not needed anymore
        return clientHandshake.successful();
    }

    // ############################################
    private class ReconnectTask implements Runnable {

        private SocketAddress socketAddress;
        private boolean stop;

        private ReconnectTask(SocketAddress socketAddress) {
            this.socketAddress = socketAddress;
            this.stop = false;
        }

        public void run() {
            IoSession session = null;

            long i = 1;
            while (keepRunning && !stop) {
                session = IoSessionFactory.this.connect(socketAddress);
                if (session != null) {
                    synchronized (lock) {
                        sessions.put(socketAddress, session);
                        pendingConnections.remove(socketAddress);
                    }
                    return;
                }
                try {
                    Thread.sleep((long) (reconnectInterval * (1.0 - Math.pow(0.9, i)) / 0.1)); // based on geometric series sum to plateau 10 times initial value
                    i++;
                } catch (InterruptedException e) {/*ignored*/}
            }

            synchronized (lock) {
                pendingConnections.remove(socketAddress);
            }
        }

        // Stop attempting to connect
        public void stop() {
            this.stop = true;
        }
    }

    @ManagedAttribute
    public int getReconnectInterval() {
        return reconnectInterval;
    }

    /*package*/ void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    @ManagedAttribute
    public String getHosts() {
        return hosts;
    }

    /*package*/ SessionRecycler getSessionRecycler() {
        return this.sessionRecycler;
    }

    /**
     * Simple comparator used for sorting the list the resolved
     * server socket addresses
     */
    private class AddressComparator implements Comparator<SocketAddress> {
        @Override
        public int compare(SocketAddress o1, SocketAddress o2) {
            return o2.hashCode() - o1.hashCode();
        }
    }
}
