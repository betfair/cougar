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

import com.betfair.cougar.core.impl.transports.TransportRegistryImpl;
import com.betfair.cougar.netutil.nio.ClientHandshake;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioConfig;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TlsNioConfig;
import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import com.betfair.cougar.netutil.nio.message.RequestMessage;
import com.betfair.cougar.netutil.nio.message.SuspendMessage;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectIOFactory;
import com.betfair.cougar.transport.socket.SocketTransportCommand;
import com.betfair.cougar.transport.socket.SocketTransportCommandProcessor;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.betfair.cougar.transport.nio.SessionTestUtil.newV1Session;
import static com.betfair.cougar.transport.nio.SessionTestUtil.newV2Session;
import static com.betfair.cougar.transport.nio.SessionTestUtil.newV3Session;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**

 */
public class CougarProtocolTest {

    private TlsNioConfig defaultServerConfig;
    private TlsNioConfig defaultClientConfig;

    @Before
    public void setup() {
        defaultServerConfig = new TlsNioConfig();
        defaultServerConfig.setNioLogger(new NioLogger("ALL"));

        defaultServerConfig.setListenAddress("127.0.0.1");
        defaultServerConfig.setListenPort(2227);
        defaultServerConfig.setReuseAddress(true);
        defaultServerConfig.setTcpNoDelay(true);
        defaultServerConfig.setKeepAliveInterval(Integer.MAX_VALUE);
        defaultServerConfig.setKeepAliveTimeout(Integer.MAX_VALUE);

        defaultClientConfig = new TlsNioConfig();
        defaultClientConfig.setNioLogger(new NioLogger("ALL"));

        defaultClientConfig.setReuseAddress(true);
        defaultClientConfig.setTcpNoDelay(true);
        defaultClientConfig.setKeepAliveInterval(Integer.MAX_VALUE);
        defaultClientConfig.setKeepAliveTimeout(Integer.MAX_VALUE);


    }


    public ExecutionVenueNioServer createServer(TlsNioConfig cfg) {
        ExecutionVenueNioServer server = new ExecutionVenueNioServer();
        server.setNioConfig(cfg);
        NioLogger sessionLogger = new NioLogger("ALL");
        TransportCommandProcessor<SocketTransportCommand> processor = new SocketTransportCommandProcessor();
        CougarObjectIOFactory objectIOFactory = new HessianObjectIOFactory(false);
        ExecutionVenueServerHandler serverHandler = new ExecutionVenueServerHandler(sessionLogger, processor, objectIOFactory) {
            @Override
            public void messageReceived(IoSession session, Object message) throws Exception {
                session.write(message);
            }
        };
        server.setServerHandler(serverHandler);
        server.setServerExecutor(Executors.newCachedThreadPool());
        server.setSocketAcceptorProcessors(1);
        server.setTransportRegistry(new TransportRegistryImpl());
        final IoSessionManager sessionManager = new IoSessionManager();
        server.setSessionManager(sessionManager);
        sessionManager.setMaxTimeToWaitForRequestCompletion(5000);
        sessionManager.setNioLogger(sessionLogger);
        return server;
    }

    public IoSession createClient(NioConfig cfg, IoHandler handler) throws IOException {
        SocketConnector sc = new SocketConnector();
        ConnectFuture cf = sc.connect(new InetSocketAddress("127.0.0.1", 2227), handler, cfg.configureSocketSessionConfig());
        cf.join();
        return cf.getSession();
    }

    @Test
    public void testConnect() throws IOException {
        boolean success = false;
        ExecutionVenueNioServer server = createServer(defaultServerConfig);
        try {
            server.start();
            server.setHealthState(true);

            IoSession session = createClient(defaultClientConfig, new IoHandlerAdapter() {
            });
            ClientHandshake handshake = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
            handshake.await(10000);

            success = handshake.successful();

            session.close();
        }
        finally {
            server.stop();
        }

        assertEquals("connection was not successful", true, success);

    }

    @Test
    public void testRejectionServerUnhealthy() throws IOException {
        ExecutionVenueNioServer server = createServer(defaultServerConfig);
        server.start();
        server.setHealthState(false);

        IoSession session = createClient(defaultClientConfig, new IoHandlerAdapter());

        ClientHandshake handshake = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
        handshake.await(10000);

        boolean success = handshake.successful();

        session.close();
        server.stop();

        assertEquals("connection shouln't have been successful", false, success);
    }

    @Test
    public void testKeepAliveNotRecieved() throws InterruptedException, IOException {
        defaultServerConfig.setKeepAliveInterval(2);
        defaultServerConfig.setKeepAliveTimeout(1);
        ExecutionVenueNioServer server = createServer(defaultServerConfig);
        server.start();
        server.setHealthState(true);

        defaultClientConfig.setKeepAliveInterval(2);
        defaultClientConfig.setKeepAliveTimeout(1);

        final CountDownLatch cdl = new CountDownLatch(1);
        IoSession ioSession = createClient(defaultClientConfig, new IoHandlerAdapter() {

            @Override
            public void sessionClosed(IoSession session) throws Exception {
                cdl.countDown();
            }
        });

        boolean closed = cdl.await(20000, TimeUnit.MILLISECONDS);

        ioSession.close();
        server.stop();

        assertEquals("Expecting session to be closed", true, closed);

    }

    @Test
    public void testKeepAlive() throws IOException, InterruptedException {
        defaultServerConfig.setKeepAliveInterval(1);
        defaultServerConfig.setKeepAliveTimeout(2);
        ExecutionVenueNioServer server = createServer(defaultServerConfig);
        server.start();
        server.setHealthState(true);

        defaultClientConfig.setKeepAliveInterval(1);
        defaultServerConfig.setKeepAliveTimeout(2);

        final CountDownLatch cdl = new CountDownLatch(1);
        IoSession ioSession = createClient(defaultClientConfig, new IoHandlerAdapter() {

            @Override
            public void sessionClosed(IoSession session) throws Exception {
                cdl.countDown();
            }
        });

        boolean closed = cdl.await(3, TimeUnit.SECONDS);
        ioSession.close();
        server.stop();


        assertEquals("session closed unexpected", false, closed);

    }

    @Test
    public void testReject() throws IOException {
        // force version to an unsupported one (the next one)
        CougarProtocol.setMinClientProtocolVersion((byte) (CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED + 1));
        CougarProtocol.setMaxClientProtocolVersion((byte) (CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED + 1));
        try {
            TlsNioConfig nioConfig = new TlsNioConfig();
            nioConfig.setNioLogger(new NioLogger("ALL"));

            nioConfig.setListenAddress("127.0.0.1");
            nioConfig.setListenPort(2227);
            nioConfig.setReuseAddress(true);
            nioConfig.setTcpNoDelay(true);
            nioConfig.setKeepAliveInterval(Integer.MAX_VALUE);
            nioConfig.setKeepAliveTimeout(Integer.MAX_VALUE);
            ExecutionVenueNioServer server = createServer(nioConfig);
            server.start();
            server.setHealthState(true);

            IoSession ioSession = createClient(defaultClientConfig, new IoHandlerAdapter());

            ClientHandshake handshake = (ClientHandshake) ioSession.getAttribute(ClientHandshake.HANDSHAKE);
            handshake.await(10000);

            boolean success = handshake.successful();

            ioSession.close();
            server.stop();

            assertEquals("connection shouldn't have been successful", false, success);
        } finally {
            CougarProtocol.setMinClientProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
            CougarProtocol.setMaxClientProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        }

    }


    @Test
    public void testDisconnect() throws IOException, InterruptedException {
        ExecutionVenueNioServer server = createServer(defaultServerConfig);
        boolean closed = false;
        try {
            server.start();
            server.setHealthState(true);

            final CountDownLatch cdl = new CountDownLatch(1);
            IoSession ioSession = createClient(defaultClientConfig, new IoHandlerAdapter() {
                @Override
                public void sessionClosed(IoSession session) throws Exception {
                    cdl.countDown();
                }
            });

            ClientHandshake handshake = (ClientHandshake) ioSession.getAttribute(ClientHandshake.HANDSHAKE);
            handshake.await(1000);

            boolean success = handshake.successful();

            assertEquals("connection should have been successful", true, success);

            server.setHealthState(false);

            closed = cdl.await(50, TimeUnit.SECONDS);

            ioSession.close();
        }
        finally {
            server.stop();
        }


        assertEquals("expected session to close due to disconnection", true, closed);

    }

    @Test
    public void testGracefulDisconnect() throws IOException, InterruptedException {
        ExecutionVenueNioServer server = createServer(defaultServerConfig);
        server.start();
        server.setHealthState(true);

        final CountDownLatch cdl = new CountDownLatch(1);
        IoSession ioSession = createClient(defaultClientConfig, new IoHandlerAdapter() {
            @Override
            public void sessionClosed(IoSession session) throws Exception {
                cdl.countDown();
            }
        });

        ClientHandshake handshake = (ClientHandshake) ioSession.getAttribute(ClientHandshake.HANDSHAKE);
        handshake.await(1000);

        boolean success = handshake.successful();

        assertEquals("connection should have been successful", true, success);

        // write some dummy request
        ioSession.write(new RequestMessage(1, "request".getBytes()));

        server.setHealthState(false);

        boolean closed = cdl.await(50, TimeUnit.SECONDS);

        // Suspend message should have been recieved
        assertTrue(ioSession.containsAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name()));
        // Disconnect message should have been recieved
        assertTrue(ioSession.containsAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name()));
        // Session should have been disconnected
        assertFalse(ioSession.isConnected());

        // teardown
        ioSession.close();
        server.stop();

        assertEquals("expected session to close due to disconnection", true, closed);

    }

    @Test
    public void testSuspendMessagesAreSkippedForV1Sessions() {
        CougarProtocol protocol = CougarProtocol.getServerInstance(new NioLogger("NONE"), 5000, 5000, null, false, false);
        IoSession ioSession = newV1Session();
        protocol.suspendSession(ioSession);
        verify(ioSession, never()).write(isA(SuspendMessage.class));
    }

    @Test
    public void testSuspendMessagesAreWrittenForV2Sessions() {
        CougarProtocol protocol = CougarProtocol.getServerInstance(new NioLogger("NONE"), 5000, 5000, null, false, false);
        IoSession ioSession = newV2Session();
        protocol.suspendSession(ioSession);
        verify(ioSession).write(isA(SuspendMessage.class));
    }

    @Test
    public void testSuspendMessagesAreWrittenForV3Sessions() {
        CougarProtocol protocol = CougarProtocol.getServerInstance(new NioLogger("NONE"), 5000, 5000, null, false, false);
        IoSession ioSession = newV3Session();
        protocol.suspendSession(ioSession);
        verify(ioSession).write(isA(SuspendMessage.class));
    }
}
