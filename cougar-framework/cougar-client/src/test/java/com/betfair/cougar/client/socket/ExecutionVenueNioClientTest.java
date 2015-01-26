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

import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.transports.TransportRegistryImpl;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TlsNioConfig;
import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectIOFactory;
import com.betfair.cougar.transport.nio.ExecutionVenueNioServer;
import com.betfair.cougar.transport.nio.IoSessionManager;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.apache.mina.common.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

import static junit.framework.Assert.*;

/**
 * Unit test for the nio client
 */
@RunWith(Parameterized.class)
public class ExecutionVenueNioClientTest extends AbstractClientTest {

    private byte serverVersion;
    private NioLogger nioLogger;

    public ExecutionVenueNioClientTest(byte serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return AbstractClientTest.protocolVersionParams();
    }

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

    @Before
    public void before() throws Exception {
        HessianObjectIOFactory objectIOFactory = new HessianObjectIOFactory(true);
        this.nioLogger = new NioLogger("ALL");
        super.before(serverVersion);
    }


    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testClient() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testClient()");
    	ClientTestExecutionObserver noFaultObserver = new ClientTestExecutionObserver();
    	ClientTestExecutionObserver invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);

        performRequestAsync(noFaultObserver, new Object[] { true, ServerClientFactory.COMMAND_SLEEP_60S, "60s sleep" } );

        performRequest(invocationObserver, new Object[] { true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING } );

        invocationObserver.assertResult();
        noFaultObserver.assertResult();

        // still there after second request
        performRequest(invocationObserver, new Object[] { true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING } );

        invocationObserver.assertResult();
        noFaultObserver.assertResult();

        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testClient()");
    }

    @Test
    public void testRPCTimeout() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testRPCTimeout()");
        TlsNioConfig config = ServerClientFactory.getDefaultConfig();
        config.setRpcTimeoutMillis(1000);
        ExecutionVenueNioClient timeoutClient = ServerClientFactory.createClient(connectionString, config);
        // wait for the client to complete the start
        timeoutClient.start().get(100, TimeUnit.SECONDS);


        ClientTestExecutionObserver timeoutObserver = new ClientTestExecutionObserver(new CougarClientException(ServerFaultCode.Timeout, "Exception occurred in Client: Read timed out: tcp://"+InetAddress.getByName("localhost").getCanonicalHostName()+":"+server.getBoundPort()));

        performRequestAsync(timeoutClient, timeoutObserver, new Object[]{true, ServerClientFactory.COMMAND_SLEEP_60S, "60s sleep"});
        assertNotNull(timeoutObserver.getExecutionResultFuture().get(2000, TimeUnit.MILLISECONDS));
        timeoutObserver.assertResult();
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testRPCTimeout()");
    }

    @Test
    public void testMultipleServerConnections() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testMultipleServerConnections()");

        final ExecutionVenueNioServer nioServer2 = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion);
        final ExecutionVenueNioServer nioServer3 = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion);
        nioServer2.setServerExecutor(Executors.newCachedThreadPool());
        nioServer2.setSocketAcceptorProcessors(1);
        nioServer2.setTransportRegistry(new TransportRegistryImpl());
        nioServer2.start();
        nioServer2.setHealthState(true);
        nioServer3.setServerExecutor(Executors.newCachedThreadPool());
        nioServer3.setSocketAcceptorProcessors(1);
        nioServer3.setTransportRegistry(new TransportRegistryImpl());
        nioServer3.start();
        nioServer3.setHealthState(true);

        // list of two instances
        final ExecutionVenueNioClient client2 =
                ServerClientFactory.createClient("127.0.0.1:" + nioServer2.getBoundPort() + ",127.0.0.1:" + nioServer3.getBoundPort());
        client2.start().get(10, TimeUnit.SECONDS);

        assertTrue("Could not establish connections to both servers after 30s", awaitOutcome(new Outcome<Boolean>() {
            @Override
            public Boolean outcome() {
                return ((getNumOfConnectedSessions(client2.getSessionFactory().getConnectedStatus())) == 2);
            }
        }, true, 30000));


        // fire one request and verify that we are ok
        ClientTestExecutionObserver invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);
        performRequest(client2, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
        invocationObserver.assertResult();

        // fire the second request which will cause the server to stop
        ExceptionCapturingObserver errorObserver = new ExceptionCapturingObserver();
        performRequest(client2, errorObserver, new Object[]{true, ServerClientFactory.COMMAND_STOP_SERVER, "whatever"});
        errorObserver.assertResult();

        final FutureTask target = new FutureTask(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                while (nioServer2.isRunning() && nioServer3.isRunning()) {
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
                if (!nioServer2.isRunning()) {
                    while (nioServer2.isShutdownInProgress()) {
                        try { Thread.sleep(500); } catch (InterruptedException e) {}
                    }
                }
                if (!nioServer3.isRunning()) {
                    while (nioServer3.isShutdownInProgress()) {
                        try { Thread.sleep(500); } catch (InterruptedException e) {}
                    }
                }
                return true;
            }
        });
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();

        try {
            target.get(15, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            fail("Server took too long to stop");
        }

        // verify that we are connected to the second server
        assertTrue(client2.getSessionFactory().isConnected());
        assertEquals(1, getNumOfConnectedSessions(client2.getSessionFactory().getConnectedStatus()));

        // fire one more request and verify that we are ok
        invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);
        performRequest(client2, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
        invocationObserver.assertResult();


        client2.stop().get(30, TimeUnit.SECONDS);
        nioServer2.stop();
        nioServer3.stop();
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testMultipleServerConnections()");
    }


    @Test
    public void testKeepAlive() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testKeepAlive()");

        ExecutionVenueNioServer s = null;
        ExecutionVenueNioClient c = null;
        try {
            TlsNioConfig cfg = ServerClientFactory.getDefaultConfig();
            cfg.setKeepAliveInterval(1);
            cfg.setKeepAliveTimeout(4);

            s = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion, cfg);
            s.setServerExecutor(Executors.newCachedThreadPool());
            s.setSocketAcceptorProcessors(1);
            s.setTransportRegistry(new TransportRegistryImpl());
            s.start();
            s.setHealthState(true);

            c = ServerClientFactory.createClient("127.0.0.1:" + s.getBoundPort(), cfg);
            c.start().get(10, TimeUnit.SECONDS);

            assertTrue(c.getSessionFactory().isConnected());

            Thread.sleep(6);

            assertTrue(c.getSessionFactory().isConnected());

            // stop server
            ExceptionCapturingObserver errorObserver = new ExceptionCapturingObserver();
            performRequest(c, errorObserver, new Object[]{true, ServerClientFactory.COMMAND_STOP_SERVER, "whatever"});
            errorObserver.assertResult();

            Thread.sleep(6);

            final ExecutionVenueNioClient finalC = c;
            assertTrue(awaitOutcome(new Outcome<Boolean>() {
                @Override
                public Boolean outcome() {
                    return finalC.getSessionFactory().isConnected();
                }
            }, false, 10000));
        } finally {
            if (s != null) s.stop();
            if (c != null) c.stop();
        }
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testKeepAlive()");
    }

    private static interface Outcome<T> {
        T outcome();
    }

    private static <T> boolean awaitOutcome(Outcome<T> source, T desired, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        boolean success;
        while (!(success = desired.equals(source.outcome())) && System.currentTimeMillis()<endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
        return success;
    }

    @Test
    public void testClientWithException() throws IOException, InterruptedException {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testClientWithException()");
        //Tests an exceptional call, the observer asserts we received the anticipated exception
        Object[] args = new Object[]{false, 999, ECHO_STRING};
        ClientTestExecutionObserver exceptionThrowingObserver =
                new ClientTestExecutionObserver(
                        new CougarFrameworkException(BANG));
        performRequest(exceptionThrowingObserver, args);
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testClientWithException()");
    }

    @Test
    public void testStopUnconnected() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testStopUnconnected()");
        ExecutionVenueNioClient client = ServerClientFactory.createClient("this.is.a.bad.url:999", getConfig());
        client.stop().get(30, TimeUnit.SECONDS);
        final boolean connected = client.getSessionFactory().isConnected();
        assertFalse(connected);
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testStopUnconnected()");
    }

    @Test
    public void testUnconnected() throws IOException, InterruptedException {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testUnconnected()");
        ExecutionVenueNioClient client = ServerClientFactory.createClient("this.is.a.bad.url:999", getConfig());
        client.start();
        assertFalse(client.getSessionFactory().isConnected());
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testUnconnected()");
    }

    @Test
    public void testUnconnected2() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testUnconnected2()");
        ExecutionVenueNioClient client = ServerClientFactory.createClient("this.is.a.bad.url:999", getConfig());
        try {
            client.start().get(2, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException e) {
        }
        assertFalse(client.getSessionFactory().isConnected());
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testUnconnected2()");
    }

    @Test
    public void testStopConnected() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testStopConnected()");
        ExecutionVenueNioClient anotherClient = ServerClientFactory.createClient(connectionString, getConfig());

        anotherClient.start().get(30, TimeUnit.SECONDS);
        assertTrue("Client failed to connect", anotherClient.getSessionFactory().isConnected());

        anotherClient.stop().get(30, TimeUnit.SECONDS);
        assertFalse("Client should now be disconnected", anotherClient.getSessionFactory().isConnected());
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testStopConnected()");
    }

    @Test
    public void testAddressFailover() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testAddressFailover()");

        String addressList = "rubbish.betfair.com:999,yetmorerubbish.betfair.com:999," + connectionString + ",one.more:123";
        ExecutionVenueNioClient anotherClient = ServerClientFactory.createClient(addressList, getConfig());

        anotherClient.start().get(30, TimeUnit.SECONDS);

        assertTrue("Client couldn't connect", anotherClient.getSessionFactory().isConnected());
        assertEquals(1, getNumOfConnectedSessions(anotherClient.getSessionFactory().getConnectedStatus()));

        anotherClient.stop().get(30, TimeUnit.SECONDS); //.get(10, TimeUnit.SECONDS);

        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testAddressFailover()");
    }


    @Test
    public void testExecutionBeforeConnection() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testExecutionBeforeConnection()");
        ExecutionVenueNioClient anotherClient = ServerClientFactory.createClient("rubbish.betfair.com:0", getConfig());

        assertFalse("Client should not be connected", anotherClient.getSessionFactory().isConnected());

        ExceptionCapturingObserver observer = new ExceptionCapturingObserver();
        performRequest(anotherClient, observer, new Object[]{});
        observer.assertResult();


        anotherClient.stop().get(30, TimeUnit.SECONDS);
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testExecutionBeforeConnection()");
    }

    @Test
    public void testNoConnectionToUnhealthyServer() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testNoConnectionToUnhealthyServer()");

        ExecutionVenueNioServer nioServer2 = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion);
        nioServer2.setServerExecutor(Executors.newCachedThreadPool());
        nioServer2.setSocketAcceptorProcessors(1);
        nioServer2.setTransportRegistry(new TransportRegistryImpl());
        nioServer2.start();
        nioServer2.setHealthState(false);

        ExecutionVenueNioClient client2 = ServerClientFactory.createClient("127.0.0.1:" + nioServer2.getBoundPort());
        client2.getSessionFactory().setReconnectInterval(1000);

        try {
            client2.start().get(2, TimeUnit.SECONDS);
            fail("expected a timeout");
        } catch (TimeoutException e) {
            //success
        }

        //just to prove we could have connected if the server was healthy

        nioServer2.setHealthState(true);
        Thread.sleep(5000);

        assertTrue(client2.getSessionFactory().isConnected());

        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testNoConnectionToUnhealthyServer()");

    }

    @Test
    public void testServerDisconnects() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testServerDisconnects()");

        ExecutionVenueNioServer nioServer2 = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion);
        nioServer2.setServerExecutor(Executors.newCachedThreadPool());
        nioServer2.setSocketAcceptorProcessors(1);
        nioServer2.setTransportRegistry(new TransportRegistryImpl());
        nioServer2.start();
        nioServer2.setHealthState(true);

        ExecutionVenueNioClient client2 = ServerClientFactory.createClient("127.0.0.1:" + nioServer2.getBoundPort());
        client2.getSessionFactory().setReconnectInterval(1000);

        client2.start().get(2, TimeUnit.SECONDS);
        assertTrue(client2.getSessionFactory().isConnected());

        //confirm we're connected
        ClientTestExecutionObserver invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);
        performRequest(client2, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
        invocationObserver.assertResult();

        nioServer2.setHealthState(false);

        Thread.sleep(50);

        assertFalse(client2.getSessionFactory().isConnected());

        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testServerDisconnects()");
    }

    @Test
    public void testClientReconnection() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testClientReconnection()");

        ExecutionVenueNioServer server = null;
        ExecutionVenueNioClient client = null;
        try {
            TlsNioConfig cfg = ServerClientFactory.getDefaultConfig();
            cfg.setKeepAliveInterval(1);
            cfg.setKeepAliveTimeout(4);

            // Start Server
            server = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion, cfg);
            server.setServerExecutor(Executors.newCachedThreadPool());
            server.setSocketAcceptorProcessors(1);
            server.setTransportRegistry(new TransportRegistryImpl());
            server.start();
            server.setHealthState(true);

            // Start Client
            client = ServerClientFactory.createClient("127.0.0.1:" + server.getBoundPort(), cfg);
            client.start().get(10, TimeUnit.SECONDS);

            // Check if client is connected
            assertTrue(client.getSessionFactory().isConnected());

            Thread.sleep(6);

            assertTrue(client.getSessionFactory().isConnected());

            // Stop client
            client.stop().get(30, TimeUnit.SECONDS);
            assertFalse(client.getSessionFactory().isConnected());

            // Start Client
            client.start().get(10, TimeUnit.SECONDS);

            // Check if connection is alive
            assertTrue("Client is not connected", client.getSessionFactory().isConnected());
        } finally {
            if (server != null) server.stop();
            if (client != null) client.stop().get(30, TimeUnit.SECONDS);
        }
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testClientReconnection()");
    }

    @Test
    public void testClientObserverReceivesNotificationsAfterReconnection() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testClientObserverReceivesNotificationsAfterReconnection()");

        ExecutionVenueNioServer server = null;
        ExecutionVenueNioClient client = null;
        try {
            TlsNioConfig cfg = ServerClientFactory.getDefaultConfig();
            cfg.setKeepAliveInterval(1);
            cfg.setKeepAliveTimeout(4);

            // Start Server
            server = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion, cfg);
            server.setServerExecutor(Executors.newCachedThreadPool());
            server.setSocketAcceptorProcessors(1);
            server.setTransportRegistry(new TransportRegistryImpl());
            server.start();
            server.setHealthState(true);

            // Start Client
            client = ServerClientFactory.createClient("127.0.0.1:" + server.getBoundPort(), cfg);
            client.start().get(10, TimeUnit.SECONDS);

            // Check if client is connected
            assertTrue(client.getSessionFactory().isConnected());

            Thread.sleep(6);

            assertTrue(client.getSessionFactory().isConnected());

            // Check if an observer is able to receive notifications
            ClientTestExecutionObserver invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);
            performRequest(client, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
            invocationObserver.assertResult();

            // make sure there's nothing in-flight
            assertEquals(0, client.getOutstandingRequestCount(client.getSessionFactory().getSession()));

            // Stop client
            client.stop().get(30, TimeUnit.SECONDS);
            assertFalse(client.getSessionFactory().isConnected());

            // Start client
            client.start().get(30, TimeUnit.SECONDS);

            // Check if connection is alive
            assertTrue("Client is not connected", client.getSessionFactory().isConnected());

            // Check if the observer is still able to recieve notifications
            performRequest(client, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
            invocationObserver.assertResult();
        } finally {
            if (server != null) server.stop();
            if (client != null) client.stop().get(30, TimeUnit.SECONDS);
        }
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testClientObserverReceivesNotificationsAfterReconnection()");
    }

    @Test
    public void testObserversCanNotBeAdded() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testObserversCanNotBeAdded()");

        ClientTestExecutionObserver observer1 = new ClientTestExecutionObserver("1");
        ClientTestExecutionObserver observer2 = new ClientTestExecutionObserver("2");

        // make the server close the connection
        performRequest(observer1, new Object[]{true, 1, "whatever"});

//        assertTrue(!client.isConnected());

        performRequest(observer2, new Object[]{true, 1, "whatever"});

        assertEquals("DSC-0026", observer1.getExecutionResult().getFault().getFault().getErrorCode());
        assertEquals("DSC-0002", observer2.getExecutionResult().getFault().getFault().getErrorCode());
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testObserversCanNotBeAdded()");
    }

    @Test
    public void testGracefulDisconnectionEnabled() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testGracefulDisconnectionEnabled()");
        ExecutionVenueNioServer testServer = null;
        ExecutionVenueNioClient testClient = null;
        try {
            testServer = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion);
            testServer.setServerExecutor(Executors.newCachedThreadPool());
            testServer.setSocketAcceptorProcessors(1);
            testServer.setTransportRegistry(new TransportRegistryImpl());
            testServer.start();
            testServer.setHealthState(true);

            testClient = ServerClientFactory.createClient("127.0.0.1:" + testServer.getBoundPort());
            testClient.getSessionFactory().setReconnectInterval(1000);

            testClient.start().get(2, TimeUnit.SECONDS);
            assertTrue(testClient.getSessionFactory().isConnected());

            //confirm we're connected
            ClientTestExecutionObserver invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);
            performRequest(testClient, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
            invocationObserver.assertResult();

            final IoSession session = testClient.getSessionFactory().getSession();
            testServer.setHealthState(false);
            Thread.sleep(5000);


            assertFalse("There should be no connected sessions", testClient.getSessionFactory().isConnected());
            assertEquals("SUSPEND message check", (serverVersion != CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC), session.containsAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name()));
            assertTrue("DISCONNECT message should have been received", session.containsAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name()));
        } finally {
            if (testServer != null) {
                testServer.stop();
            }

            if (testClient != null) {
                testClient.stop().get(30, TimeUnit.SECONDS);
            }
        }
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testGracefulDisconnectionEnabled()");
    }

    @Test
    public void testGracefulDisconnectionDisabled() throws Exception {
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Starting testGracefulDisconnectionDisabled()");

        ExecutionVenueNioServer testServer = null;
        ExecutionVenueNioClient testClient = null;
        try {

            testServer = ServerClientFactory.createServer("127.0.0.1", 0, serverVersion);
            IoSessionManager sessionManager = new IoSessionManager();
            sessionManager.setNioLogger(testServer.getNioConfig().getNioLogger());
            sessionManager.setMaxTimeToWaitForRequestCompletion(0); // Disabled
            testServer.setSessionManager(sessionManager);

            testServer.setServerExecutor(Executors.newCachedThreadPool());
            testServer.setSocketAcceptorProcessors(1);
            testServer.setTransportRegistry(new TransportRegistryImpl());
            testServer.start();
            testServer.setHealthState(true);

            testClient = ServerClientFactory.createClient("127.0.0.1:" + testServer.getBoundPort());
            testClient.getSessionFactory().setReconnectInterval(1000);

            testClient.start().get(2, TimeUnit.SECONDS);
            assertTrue(testClient.getSessionFactory().isConnected());

            //confirm we're connected
            ClientTestExecutionObserver invocationObserver = new ClientTestExecutionObserver(ECHO_STRING);
            performRequest(testClient, invocationObserver, new Object[]{true, ServerClientFactory.COMMAND_ECHO_ARG2, ECHO_STRING});
            invocationObserver.assertResult();

            final IoSession session = testClient.getSessionFactory().getSession();
            testServer.setHealthState(false);
            Thread.sleep(5000);


            assertFalse("There should be no connected sessions", testClient.getSessionFactory().isConnected());
            assertFalse("SUSPEND message should not have been received", session.containsAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name()));
            assertTrue("DISCONNECT message should have been received", session.containsAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name()));
        } finally {
            if (testServer != null) {
                testServer.stop();
            }

            if (testClient != null) {
                testClient.stop().get(30, TimeUnit.SECONDS);
            }
        }
        nioLogger.log(NioLogger.LoggingLevel.SESSION, (String)null, "Stopping testGracefulDisconnectionDisabled()");
    }

    private int getNumOfConnectedSessions(Map<String, String> connectedStatus) {
        int conn = 0;
        for (String sessionStatus : connectedStatus.values()) {
            if (sessionStatus.contains("connected=true,closing=false")) {
                conn++;
            }
        }
        return conn;
    }
}
