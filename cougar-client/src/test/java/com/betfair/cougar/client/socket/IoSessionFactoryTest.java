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

import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import com.betfair.cougar.util.JMXReportingThreadPoolExecutor;
import org.apache.mina.common.IoSession;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Session factory tests
 */
public class IoSessionFactoryTest {

    private IoSessionFactory sessionFactory;
    private IoSession connectedSession;

    @Test
    public void testGetSessionAlwaysReturnsConnectedSessions() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertTrue(connectedSession.equals(sessionFactory.getSession()));
        }
    }

    @Test
    public void testOpenSession() throws Exception {
        final InetSocketAddress address1 = new InetSocketAddress("host1", 9003);
        final InetSocketAddress address2 = new InetSocketAddress("host2", 9003);
        sessionFactory.openSession(address1);
        sessionFactory.openSession(address2);

        Field pendingConnections = IoSessionFactory.class.getDeclaredField("pendingConnections");
        pendingConnections.setAccessible(true);
        Map<SocketAddress, Object> map = (Map<SocketAddress, Object>) pendingConnections.get(sessionFactory);
        assertEquals(2, map.size());
        assertTrue(map.keySet().contains(address1));
        assertTrue(map.keySet().contains(address2));

        sessionFactory.openSession(address1);
        sessionFactory.openSession(address2);

        map = (Map<SocketAddress, Object>) pendingConnections.get(sessionFactory);
        assertEquals(2, map.size());
        assertTrue(map.keySet().contains(address1));
        assertTrue(map.keySet().contains(address2));
    }

    @Test
    public void testCloseSession() throws Exception {
        final InetSocketAddress address1 = new InetSocketAddress("host1", 9003);
        final InetSocketAddress address2 = new InetSocketAddress("host2", 9003);
        sessionFactory.openSession(address1);
        sessionFactory.openSession(address2);

        sessionFactory.closeSession(connectedSession.getRemoteAddress(), false);
        sessionFactory.closeSession(address1, false);
        sessionFactory.closeSession(address2, false);
        verify(sessionFactory).close(connectedSession, false);
    }

    @Before
    public void setup() throws Exception {
        sessionFactory = mock(IoSessionFactory.class);

        Field sessionsField = IoSessionFactory.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        Map<SocketAddress, IoSession> sessions = new HashMap<SocketAddress, IoSession>();
        connectedSession = getConnectedSession();
        sessions.put(connectedSession.getRemoteAddress(), connectedSession);
        final IoSession notConnectedSession = getNotConnectedSession();
        sessions.put(notConnectedSession.getRemoteAddress(), notConnectedSession);
        final IoSession closingSession = getClosingSession();
        sessions.put(closingSession.getRemoteAddress(), closingSession);
        final IoSession suspendedSession = getSuspendedSession();
        sessions.put(suspendedSession.getRemoteAddress(), suspendedSession);
        final IoSession disconnectedSession = getDisconnectedSession();
        sessions.put(disconnectedSession.getRemoteAddress(), disconnectedSession);
        sessionsField.set(sessionFactory, sessions);
        when(sessionFactory.getSession()).thenCallRealMethod();
        when(sessionFactory.isAvailable(any(IoSession.class))).thenCallRealMethod();

        Field lockField = IoSessionFactory.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        lockField.set(sessionFactory, new Object());

        Field pendingConnections = IoSessionFactory.class.getDeclaredField("pendingConnections");
        pendingConnections.setAccessible(true);
        pendingConnections.set(sessionFactory, new HashMap());

        Field executor = IoSessionFactory.class.getDeclaredField("reconnectExecutor");
        executor.setAccessible(true);
        ExecutorService mockExecutor = mock(JMXReportingThreadPoolExecutor.class);
        when(mockExecutor.submit(any(Runnable.class))).thenReturn(null);
        executor.set(sessionFactory, mockExecutor);

        doCallRealMethod().when(sessionFactory).openSession(any(SocketAddress.class));
        doCallRealMethod().when(sessionFactory).closeSession(any(SocketAddress.class), anyBoolean());
    }

    private IoSession getConnectedSession() {
        return getSession(1, true, false, false, false);
    }

    private IoSession getNotConnectedSession() {
        return getSession(2, false, false, false, false);
    }

    private IoSession getClosingSession() {
        return getSession(3, true, true, false, false);
    }

    private IoSession getSuspendedSession() {
        return getSession(4, true, false, true, false);
    }

    private IoSession getDisconnectedSession() {
        return getSession(5, true, false, false, true);
    }

    private IoSession getSession(int id, boolean isConnected, boolean isClosing, boolean isSuspended, boolean isDisconnected) {
        final IoSession ioSession = mock(IoSession.class);
        when(ioSession.isConnected()).thenReturn(isConnected);
        when(ioSession.isClosing()).thenReturn(isClosing);
        when(ioSession.containsAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name())).thenReturn(isSuspended);
        when(ioSession.containsAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name())).thenReturn(isDisconnected);
        when(ioSession.getRemoteAddress()).thenReturn(new InetSocketAddress("1.1.1." + id, 9003));
        return ioSession;
    }
}
