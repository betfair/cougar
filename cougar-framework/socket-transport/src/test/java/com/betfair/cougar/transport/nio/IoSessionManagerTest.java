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
import com.betfair.cougar.netutil.nio.message.DisconnectMessage;
import com.betfair.cougar.netutil.nio.message.SuspendMessage;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.socket.SocketTransportCommand;
import com.betfair.cougar.transport.socket.SocketTransportCommandProcessor;
import org.apache.mina.common.IoSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static com.betfair.cougar.netutil.nio.CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC;
import static com.betfair.cougar.netutil.nio.CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC;
import static com.betfair.cougar.transport.nio.SessionTestUtil.newSession;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;

/**
 * Session manager tests
 */
public class IoSessionManagerTest {

    public static final byte V1 = TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC;
    public static final byte V2 = TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC;
    IoSessionManager sessionManager;
    private CougarProtocol cougarProtocol;
    private NioLogger nioLogger;
    private ExecutionVenueServerHandler serverHandler;

    @Before
    public void setup() {
        nioLogger = new NioLogger("NONE"); // Changing this might affect assertions around number of mock method invocations
        sessionManager = new IoSessionManager();
        sessionManager.setMaxTimeToWaitForRequestCompletion(5000);
        sessionManager.setNioLogger(nioLogger);
        cougarProtocol = CougarProtocol.getServerInstance( nioLogger, 5000, 10000, null, false, false);
        TransportCommandProcessor<SocketTransportCommand> processor = new SocketTransportCommandProcessor();
        serverHandler = mock(ExecutionVenueServerHandler.class);
        when(serverHandler.getOutstandingRequests()).thenReturn(0l);
    }

    @Test
    public void testSuspendMessageIsNotWrittenForV1Sessions() {
        final IoSession ioSession = newSession(V1);

        sessionManager.shutdownSessions(singleton(ioSession), cougarProtocol, serverHandler);

        verify(ioSession, never()).write(isA(SuspendMessage.class));
        verify(ioSession).write(isA(DisconnectMessage.class));
    }

    @Test
    public void testSuspendAndDisconnectMessagesAreWrittenForV2Sessions() {
        final IoSession ioSession = newSession(V2);

        sessionManager.shutdownSessions(singleton(ioSession), cougarProtocol, serverHandler);

        verify(ioSession).write(isA(SuspendMessage.class));
        verify(ioSession).write(isA(DisconnectMessage.class));
    }

    @Test
    public void testSessionsAreClosedOnlyAfterOutstandingRequestsAreServed() {
        final IoSession ioSession = newSession(V2);
        ExecutionVenueServerHandler serverHandler = mock(ExecutionVenueServerHandler.class);
        when(serverHandler.getOutstandingRequests()).thenReturn(2l, 1l, 0l); // Counting down

        sessionManager.shutdownSessions(singleton(ioSession), cougarProtocol, serverHandler);

        final InOrder inOrder = inOrder(ioSession, serverHandler);
        inOrder.verify(ioSession).write(isA(SuspendMessage.class));
        inOrder.verify(serverHandler, times(3)).getOutstandingRequests();
        inOrder.verify(ioSession).write(isA(DisconnectMessage.class));
    }
}
