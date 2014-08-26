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


import com.betfair.cougar.core.api.BindingDescriptor;
import com.betfair.cougar.core.api.transports.AbstractRegisterableTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioConfig;
import com.betfair.cougar.netutil.nio.TlsNioConfig;
import com.betfair.cougar.transport.api.protocol.socket.SocketBindingDescriptor;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;


@ManagedResource
public class ExecutionVenueNioServer extends AbstractRegisterableTransport {

    final static Logger LOGGER = LoggerFactory.getLogger(ExecutionVenueNioServer.class);

    private TlsNioConfig nioConfig;

    private SocketAcceptor socketAcceptor;

    private IoSessionManager sessionManager;

    private InetSocketAddress socketAddress;

    private int socketAcceptorProcessors;

    private ExecutionVenueServerHandler serverHandler;
    private ExecutorService serverExecutor;
    private volatile boolean running;
    private volatile boolean shutdownInProgress;

    @Override
    public void notify(BindingDescriptor bindingDescriptor) {
        if (bindingDescriptor instanceof SocketBindingDescriptor) {
            serverHandler.notify((SocketBindingDescriptor) bindingDescriptor);
        }
    }

    public void setServerHandler(ExecutionVenueServerHandler serverHandler) {
        this.serverHandler = serverHandler;//NOSONAR
    }

    public void setNioConfig(TlsNioConfig nioConfig) {
        this.nioConfig = nioConfig;
    }

    public TlsNioConfig getNioConfig() {
        return nioConfig;
    }

    public InetAddress getBoundAddress() {
        return socketAddress != null ? socketAddress.getAddress() : null;
    }

    @ManagedAttribute
    public int getBoundPort() {
        return socketAddress != null ? socketAddress.getPort() : 0;
    }

    @ManagedAttribute
    public String getHostAddress() {
        return socketAddress != null ? socketAddress.getHostName() : "Not Bound";
    }

    public synchronized void start() throws IOException {
        register();
        if (serverHandler != null) {

            if (socketAcceptor == null) {
                socketAcceptor = new SocketAcceptor(socketAcceptorProcessors, serverExecutor);
            }

            SocketAcceptorConfig config = socketAcceptor.getDefaultConfig();
            nioConfig.configureSocketAcceptorConfig(config);
            socketAcceptor.bind(nioConfig.getServerSocketAddress(), serverHandler, config);

            socketAddress = (InetSocketAddress) socketAcceptor.getManagedServiceAddresses().iterator().next();


            LOGGER.info("ExecutionVenueNioServer started on " + socketAcceptor.getManagedServiceAddresses());

            // Create a shutdown hook to close the Socket Server cleanly
            Runtime.getRuntime().addShutdownHook(new Thread("EV Socket Server Shutdown Thread") {
                @Override
                public void run() {
                    LOGGER.info("Gracefully shutting down ExecutionVenueNioServer");
                    try {
                        ExecutionVenueNioServer.this.stop();
                    } catch (Exception e) {
                        LOGGER.warn("Failed to shutdown ExecutionVenueNioServer", e);
                    }
                }
            });
            running = true;
        }
    }

    @ManagedAttribute
    public synchronized void setHealthState(boolean isHealthy) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("setting protocol to " + (isHealthy ? "enabled" : "disabled"));
        }
        if (socketAcceptor != null) {
            CougarProtocol cougarProtocol = (CougarProtocol) socketAcceptor.getDefaultConfig().getFilterChain().get("protocol");
            cougarProtocol.setEnabled(isHealthy);
            if (!isHealthy) {
                shutdownSessions(false);
            }
        }
    }

    @ManagedAttribute
    public synchronized Boolean isHealthState() {
        if (socketAcceptor != null) {
            CougarProtocol cougarProtocol = (CougarProtocol) socketAcceptor.getDefaultConfig().getFilterChain().get("protocol");
            return cougarProtocol.isEnabled();
        }
        return null;
    }

    @ManagedAttribute
    public boolean isEnabled() {
        final CougarProtocol protocol = (CougarProtocol) socketAcceptor.getDefaultConfig().getFilterChain().get("protocol");
        if (protocol != null) {
            return protocol.isEnabled();
        }

        return false;
    }

    public synchronized void stop() {
        if (running) {
            shutdownInProgress = true;
            running = false;
            if (socketAcceptor != null) {
                shutdownSessions(true);
                socketAcceptor.unbindAll();
                socketAcceptor = null;
            }
            serverExecutor.shutdown();
            shutdownInProgress = false;
        }
    }

    public void setServerExecutor(ExecutorService serverExecutor) {
        this.serverExecutor = serverExecutor;//NOSONAR
    }

    @ManagedAttribute
    public int getSocketAcceptorProcessors() {
        return socketAcceptorProcessors;
    }

    public void setSocketAcceptorProcessors(int socketAcceptorProcessors) {
        this.socketAcceptorProcessors = socketAcceptorProcessors;
    }

    public void setSessionManager(IoSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private void shutdownSessions(boolean blockUntilComplete) {
        CougarProtocol cougarProtocol = (CougarProtocol) socketAcceptor.getDefaultConfig().getFilterChain().get("protocol");
        sessionManager.shutdownSessions(socketAcceptor.getManagedSessions(socketAddress), cougarProtocol, this.serverHandler, blockUntilComplete);
    }

    @ManagedAttribute
    public boolean isRunning() {
        return running;
    }

    @ManagedAttribute
    public boolean isShutdownInProgress() {
        return shutdownInProgress;
    }
}
