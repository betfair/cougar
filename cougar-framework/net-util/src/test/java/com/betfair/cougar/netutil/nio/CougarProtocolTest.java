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

import com.betfair.cougar.netutil.nio.message.TLSResult;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilter;
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
import org.apache.mina.filter.SSLFilter;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class CougarProtocolTest {

    private NioLogger logger;
    private ICougarProtocol client;
    private ICougarProtocol server;
    private IoFilter.NextFilter nextFilter;
    private PseudoIoSessionMock clientSession;
    private PseudoIoSessionMock serverSession;
    private SSLFilter sslFilter;

    @Before
    public void before() {
        sslFilter = mock(SSLFilter.class);
        nextFilter = mock(IoFilter.NextFilter.class);
    }

    private void setupProtocol(byte clientVersion, byte serverVersion) {
        setupProtocol(clientVersion, null, false, false, serverVersion, null, false, false);
    }

    private void setupProtocol(byte clientVersion, SSLFilter clientSslFilter, boolean clientRequiresTls,
                               byte serverVersion, SSLFilter serverSslFilter, boolean serverRequiresTls) {
        setupProtocol(clientVersion, clientSslFilter, clientSslFilter!=null, clientRequiresTls,
                      serverVersion, serverSslFilter, serverSslFilter!=null, serverRequiresTls);
    }

    private void setupProtocol(byte clientVersion, SSLFilter clientSslFilter, boolean clientSupportsTls, boolean clientRequiresTls,
                               byte serverVersion, SSLFilter serverSslFilter, boolean serverSupportsTls, boolean serverRequiresTls) {
        logger = new NioLogger("ALL");
        if (clientVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC) {
            if (clientSupportsTls) {
                throw new IllegalArgumentException("Server version doesn't support TLS");
            }
            client = new CougarProtocol1(false, logger, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, 2000, 5000);
        } else if (clientVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC) {
            if (clientSupportsTls) {
                throw new IllegalArgumentException("Server version doesn't support TLS");
            }
            client = new CougarProtocol2(false, logger, 2000, 5000);
        } else if (clientVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            client = new CougarProtocol3(false, logger, 2000, 5000, clientSslFilter, clientSupportsTls, clientRequiresTls, 0);
        } else if (clientVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS) {
            client = new CougarProtocol4(false, logger, 2000, 5000, clientSslFilter, clientSupportsTls, clientRequiresTls, 0);
        } else if (clientVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_COMPOUND_REQUEST_UUID) {
            client = new CougarProtocol5(false, logger, 2000, 5000, clientSslFilter, clientSupportsTls, clientRequiresTls, 0);
        } else {
            throw new IllegalArgumentException("Unsupported client version: " + clientVersion);
        }
        if (serverVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC) {
            if (serverSupportsTls) {
                throw new IllegalArgumentException("Server version doesn't support TLS");
            }
            server = new CougarProtocol1(true, logger, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, 2000, 5000);
        } else if (serverVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC) {
            if (serverSupportsTls) {
                throw new IllegalArgumentException("Server version doesn't support TLS");
            }
            server = new CougarProtocol2(true, logger, 2000, 5000);
        } else if (serverVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            server = new CougarProtocol3(true, logger, 2000, 5000, serverSslFilter, serverSupportsTls, serverRequiresTls, 0);
        } else if (serverVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS) {
            server = new CougarProtocol4(true, logger, 2000, 5000, serverSslFilter, serverSupportsTls, serverRequiresTls, 0);
        } else if (serverVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_COMPOUND_REQUEST_UUID) {
            server = new CougarProtocol5(true, logger, 2000, 5000, serverSslFilter, serverSupportsTls, serverRequiresTls, 0);
        } else {
            throw new IllegalArgumentException("Unsupported client version: " + clientVersion);
        }

        client.setEnabled(true);
        server.setEnabled(true);

        clientSession = createSession(server);
        serverSession = createSession(client);
        clientSession.setOtherSession(serverSession);
        serverSession.setOtherSession(clientSession);
    }

    // =================== Version mismatching ========================

    @Test
    public void versionMismatchVNPlusOneOnly_VN() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);

        try {
            CougarProtocol.setMinClientProtocolVersion((byte)(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED+1));
            CougarProtocol.setMaxClientProtocolVersion((byte)(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED+1));

            client.sessionOpened(nextFilter, clientSession);

            ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
            assertTrue(handshake.await(5000));
            assertFalse(handshake.successful());
        }
        finally {
            CougarProtocol.setMinClientProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED);
            CougarProtocol.setMaxClientProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        }
    }

    // =================== Successful Handshakes ========================

    @Test
    public void successfulHandshakePlaintextV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakePlaintextV3_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakePlaintextV3_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakeV2_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakeV2_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakeV2_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakeV1_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakeV1_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    @Test
    public void successfulHandshakeV1_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
    }

    // =================== Server Disabled - Rejected ========================

    @Test
    public void serverDisabledV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV3_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV3_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV2_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV2_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV2_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV1_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV1_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    @Test
    public void serverDisabledV1_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);
        server.setEnabled(false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    // ============= TLS - Client Requires, Server too old =============

    @Test
    public void clientRequiresTlsServerTooOldV3_V2() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true,
                      CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, null, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }
    @Test
    public void clientRequiresTlsServerTooOldV3_V1() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, null, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    // ============= TLS - Client too old, Server requires =============

    @Test
    public void clientTooOldServerRequiresTlsV2_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC, null, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }
    @Test
    public void clientTooOldServerRequiresTlsV1_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC, null, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }

    // ============= TLS - Client & server versions sufficient, client requires, server doesn't support =====
    @Test
    public void clientRequiresTlsServerDoesntSupportTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, null, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }
    // ============= TLS - Client & server versions sufficient, client requires, server supports =====
    @Test
    public void clientRequiresTlsServerSupportsTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain()).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain()).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client requires, server requires =====
    @Test
    public void clientRequiresTlsServerRequiresTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain()).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain()).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client supports, server doesn't =====
    @Test
    public void clientSupportsTlsServerDoesntSupportTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, null, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain(), times(0)).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain(), times(0)).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.PLAINTEXT, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.PLAINTEXT, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client supports, server supports =====
    @Test
    public void clientSupportsTlsServerSupportsTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain()).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain()).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client supports, server requires =====
    @Test
    public void clientSupportsTlsServerRequiresTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain()).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain()).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.SSL, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client doesn't support, server doesn't support =====
    @Test
    public void clientDoesntSupportTlsServerDoesntSupportTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, null, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, null, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain(), times(0)).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain(), times(0)).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.PLAINTEXT, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.PLAINTEXT, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client doesn't support, server supports =====
    @Test
    public void clientDoesntSupportTlsServerSupportsTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, null, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, false);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertTrue(handshake.successful());

        verify(clientSession.getFilterChain(), times(0)).addFirst("ssl", sslFilter);
        verify(serverSession.getFilterChain(), times(0)).addFirst("ssl", sslFilter);

        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, clientSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.PLAINTEXT, clientSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
        assertEquals(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, serverSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME));
        assertEquals(TLSResult.PLAINTEXT, serverSession.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME));
    }
    // ============= TLS - Client & server versions sufficient, client doesn't support, server requires =====
    @Test
    public void clientDoesntSupportTlsServerRequiresTlsV3_V3() throws Exception {
        setupProtocol(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, null, false,
                CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS, sslFilter, true);

        client.sessionOpened(nextFilter, clientSession);

        ClientHandshake handshake = (ClientHandshake) clientSession.getAttribute(ClientHandshake.HANDSHAKE);
        assertTrue(handshake.await(5000));
        assertFalse(handshake.successful());
    }


    private PseudoIoSessionMock createSession(ICougarProtocol otherEnd) {
//        when(session.write())
        return new PseudoIoSessionMock(otherEnd);
    }

    private class PseudoIoSessionMock implements IoSession {
        private IoSession mock = mock(IoSession.class);
        private IoFilterChain mockFilterChain = mock(IoFilterChain.class);
        private IoFilter.NextFilter nextFilter = mock(IoFilter.NextFilter.class);
        private ICougarProtocol otherEnd;
        private Map<String, Object> attributes = new HashMap<String, Object>();
        private PseudoIoSessionMock otherSession;

        private PseudoIoSessionMock(ICougarProtocol otherEnd) {
            this.otherEnd = otherEnd;
        }

        public IoSession getMock() {
            return mock;
        }

        public IoFilter.NextFilter getNextFilter() {
            return nextFilter;
        }

        @Override
        public IoService getService() {
            return mock.getService();
        }

        @Override
        public IoServiceConfig getServiceConfig() {
            return mock.getServiceConfig();
        }

        @Override
        public IoHandler getHandler() {
            return mock.getHandler();
        }

        @Override
        public IoSessionConfig getConfig() {
            return mock.getConfig();
        }

        @Override
        public IoFilterChain getFilterChain() {
            return mockFilterChain;
        }

        @Override
        public WriteFuture write(Object message) {
            DefaultWriteFuture ret = new DefaultWriteFuture(this);
            try {
                otherEnd.messageReceived(nextFilter, otherSession, message);
                ret.setWritten(true);
            }
            catch (Exception e) {
                e.printStackTrace();
                ret.setWritten(false);
            }
            // behave like the real thing..
            if (attributes.get(SSLFilter.DISABLE_ENCRYPTION_ONCE) != null) {
                attributes.remove(SSLFilter.DISABLE_ENCRYPTION_ONCE);
            }
            return ret;
        }

        @Override
        public CloseFuture close() {
            DefaultCloseFuture ret = new DefaultCloseFuture(this);
            try {
                otherEnd.sessionClosed(nextFilter, otherSession);
                ret.setClosed();
            } catch (Exception e) {
                e.printStackTrace();
                ret.setClosed();
            }
            return ret;
        }

        @Override
        public Object getAttachment() {
            return mock.getAttachment();
        }

        @Override
        public Object setAttachment(Object attachment) {
            return mock.setAttachment(attachment);
        }

        @Override
        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        @Override
        public Object setAttribute(String key, Object value) {
            return attributes.put(key, value);
        }

        @Override
        public Object setAttribute(String key) {
            return attributes.put(key, Boolean.TRUE);
        }

        @Override
        public Object removeAttribute(String key) {
            return attributes.remove(key);
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
            return mock.getTransportType();
        }

        @Override
        public boolean isConnected() {
            return mock.isConnected();
        }

        @Override
        public boolean isClosing() {
            return mock.isClosing();
        }

        @Override
        public CloseFuture getCloseFuture() {
            return mock.getCloseFuture();
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return mock.getRemoteAddress();
        }

        @Override
        public SocketAddress getLocalAddress() {
            return mock.getLocalAddress();
        }

        @Override
        public SocketAddress getServiceAddress() {
            return mock.getServiceAddress();
        }

        @Override
        public int getIdleTime(IdleStatus status) {
            return mock.getIdleTime(status);
        }

        @Override
        public long getIdleTimeInMillis(IdleStatus status) {
            return mock.getIdleTimeInMillis(status);
        }

        @Override
        public void setIdleTime(IdleStatus status, int idleTime) {
            mock.setIdleTime(status, idleTime);
        }

        @Override
        public int getWriteTimeout() {
            return mock.getWriteTimeout();
        }

        @Override
        public long getWriteTimeoutInMillis() {
            return mock.getWriteTimeoutInMillis();
        }

        @Override
        public void setWriteTimeout(int writeTimeout) {
            mock.setWriteTimeout(writeTimeout);
        }

        @Override
        public TrafficMask getTrafficMask() {
            return mock.getTrafficMask();
        }

        @Override
        public void setTrafficMask(TrafficMask trafficMask) {
            mock.setTrafficMask(trafficMask);
        }

        @Override
        public void suspendRead() {
            mock.suspendRead();
        }

        @Override
        public void suspendWrite() {
            mock.suspendWrite();
        }

        @Override
        public void resumeRead() {
            mock.resumeRead();
        }

        @Override
        public void resumeWrite() {
            mock.resumeWrite();
        }

        @Override
        public long getReadBytes() {
            return mock.getReadBytes();
        }

        @Override
        public long getWrittenBytes() {
            return mock.getWrittenBytes();
        }

        @Override
        public long getReadMessages() {
            return mock.getReadMessages();
        }

        @Override
        public long getWrittenMessages() {
            return mock.getWrittenMessages();
        }

        @Override
        public long getWrittenWriteRequests() {
            return mock.getWrittenWriteRequests();
        }

        @Override
        public int getScheduledWriteRequests() {
            return mock.getScheduledWriteRequests();
        }

        @Override
        public int getScheduledWriteBytes() {
            return mock.getScheduledWriteBytes();
        }

        @Override
        public long getCreationTime() {
            return mock.getCreationTime();
        }

        @Override
        public long getLastIoTime() {
            return mock.getLastIoTime();
        }

        @Override
        public long getLastReadTime() {
            return mock.getLastReadTime();
        }

        @Override
        public long getLastWriteTime() {
            return mock.getLastWriteTime();
        }

        @Override
        public boolean isIdle(IdleStatus status) {
            return mock.isIdle(status);
        }

        @Override
        public int getIdleCount(IdleStatus status) {
            return mock.getIdleCount(status);
        }

        @Override
        public long getLastIdleTime(IdleStatus status) {
            return mock.getLastIdleTime(status);
        }

        public void setOtherSession(PseudoIoSessionMock otherSession) {
            this.otherSession = otherSession;
        }
    }
}
