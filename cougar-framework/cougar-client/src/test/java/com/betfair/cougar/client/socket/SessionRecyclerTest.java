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
import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import org.apache.mina.common.IoSession;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 *
 */
public class SessionRecyclerTest {

    public static final String HOST1 = "HOST1";
    public static final String HOST2 = "HOST2";
    public static final String HOST3 = "HOST3";
    public static final String HOST1_IP1 = "1.1.1.1";
    public static final String HOST1_IP2 = "1.1.1.2";
    public static final String HOST1_IP3 = "1.1.1.3";
    public static final String HOST2_IP1 = "2.2.2.1";
    public static final String HOST2_IP2 = "2.2.2.2";
    public static final String HOST2_IP3 = "2.2.2.3";
    public static final String HOST3_IP1 = "3.3.3.1";
    public static final String HOST3_IP2 = "3.3.3.2";
    public static final String HOST3_IP3 = "3.3.3.3";
    private IoSessionFactory sessionFactory;
    private SessionRecycler recycler;
    private String hosts;

    @Before
    public void setup() throws Exception {
        sessionFactory = mock(IoSessionFactory.class);

        Field sessionsField = IoSessionFactory.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        Map<SocketAddress, IoSession> sessions = new HashMap<SocketAddress, IoSession>();
        final IoSession session1 = getSession(1);
        final IoSession session2 = getSession(2);
        final IoSession session3 = getSession(3);
        sessions.put(session1.getRemoteAddress(), session1);
        sessions.put(session2.getRemoteAddress(), session2);
        sessions.put(session3.getRemoteAddress(), session3);

        sessionsField.set(sessionFactory, sessions);
        when(sessionFactory.getSession()).thenCallRealMethod();
        when(sessionFactory.isAvailable(any(IoSession.class))).thenCallRealMethod();

        Field lockField = IoSessionFactory.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        lockField.set(sessionFactory, new Object());

        NetworkAddressResolver resolver = mock(NetworkAddressResolver.class);
        when(resolver.resolve(HOST1)).thenReturn(asSet(HOST1_IP1, HOST1_IP2));
        when(resolver.resolve(HOST2)).thenReturn(asSet(HOST2_IP1, HOST2_IP2));
        when(resolver.resolve(HOST3)).thenReturn(asSet(HOST3_IP1, HOST3_IP2));
        hosts = "HOST1:9003,HOST2:9003,HOST3:9003";
        recycler = new SessionRecycler(sessionFactory, resolver, hosts, 5000);
    }

    @Test
    public void testSessionsAreEstablishedToAllResolvedEndpoints() {
        recycler.run();
        verify(sessionFactory, times(6)).openSession(any(InetSocketAddress.class));
        verify(sessionFactory).openSession(new InetSocketAddress(HOST1_IP1, 9003));
        verify(sessionFactory).openSession(new InetSocketAddress(HOST2_IP1, 9003));
        verify(sessionFactory).openSession(new InetSocketAddress(HOST3_IP1, 9003));
        verify(sessionFactory).openSession(new InetSocketAddress(HOST1_IP2, 9003));
        verify(sessionFactory).openSession(new InetSocketAddress(HOST2_IP2, 9003));
        verify(sessionFactory).openSession(new InetSocketAddress(HOST3_IP2, 9003));
    }

    @Test
    public void testSessionsAreRecycledWhenEndpointsChange() throws Exception {
        final InetSocketAddress host11 = new InetSocketAddress(HOST1_IP1, 9003);
        final InetSocketAddress host21 = new InetSocketAddress(HOST2_IP1, 9003);
        final InetSocketAddress host31 = new InetSocketAddress(HOST3_IP1, 9003);
        final InetSocketAddress host12 = new InetSocketAddress(HOST1_IP2, 9003);
        final InetSocketAddress host22 = new InetSocketAddress(HOST2_IP2, 9003);
        final InetSocketAddress host32 = new InetSocketAddress(HOST3_IP2, 9003);

        final InetSocketAddress host13 = new InetSocketAddress(HOST1_IP3, 9003);
        final InetSocketAddress host23 = new InetSocketAddress(HOST2_IP3, 9003);
        final InetSocketAddress host33 = new InetSocketAddress(HOST3_IP3, 9003);

        NetworkAddressResolver resolver = mock(NetworkAddressResolver.class);
        when(resolver.resolve(HOST1)).thenReturn(asSet(HOST1_IP1, HOST1_IP2)).thenReturn(asSet(HOST1_IP1, HOST1_IP2, HOST1_IP3));
        when(resolver.resolve(HOST2)).thenReturn(asSet(HOST2_IP1, HOST2_IP2)).thenReturn(asSet(HOST2_IP3));
        when(resolver.resolve(HOST3)).thenReturn(asSet(HOST3_IP1, HOST3_IP2)).thenReturn(asSet(HOST3_IP3, HOST3_IP2, HOST3_IP1));
        final SessionRecycler recycler = new SessionRecycler(sessionFactory, resolver, hosts, 5000);

        // spin
        recycler.run();

        // verify new sessions opened
        verify(sessionFactory, times(6)).openSession(any(InetSocketAddress.class));
        verify(sessionFactory).openSession(host11);
        verify(sessionFactory).openSession(host21);
        verify(sessionFactory).openSession(host31);
        verify(sessionFactory).openSession(host12);
        verify(sessionFactory).openSession(host22);
        verify(sessionFactory).openSession(host32);

        Set<SocketAddress> sessionAddresses = new HashSet<SocketAddress>(asList(host11, host21, host31, host12, host22, host32));

        when(sessionFactory.getCurrentSessionAddresses()).thenReturn(sessionAddresses);

        // spin
        recycler.run();

        // verify new sessions opened
        verify(sessionFactory, times(9)).openSession(any(InetSocketAddress.class));
        verify(sessionFactory).openSession(host13);
        verify(sessionFactory).openSession(host23);
        verify(sessionFactory).openSession(host33);

        // verify sessions closed
        verify(sessionFactory, times(2)).closeSession(any(SocketAddress.class), eq(false));
        verify(sessionFactory).closeSession(host21, false);
        verify(sessionFactory).closeSession(host22, false);
    }

    @Test
    public void testSessionsAreNotRecycledWhenEndpointsDoNotChange() throws Exception {
        final InetSocketAddress host11 = new InetSocketAddress(HOST1_IP1, 9003);
        final InetSocketAddress host21 = new InetSocketAddress(HOST2_IP1, 9003);
        final InetSocketAddress host31 = new InetSocketAddress(HOST3_IP1, 9003);
        final InetSocketAddress host12 = new InetSocketAddress(HOST1_IP2, 9003);
        final InetSocketAddress host22 = new InetSocketAddress(HOST2_IP2, 9003);
        final InetSocketAddress host32 = new InetSocketAddress(HOST3_IP2, 9003);

        NetworkAddressResolver resolver = mock(NetworkAddressResolver.class);
        when(resolver.resolve(HOST1)).thenReturn(asSet(HOST1_IP1, HOST1_IP2));
        when(resolver.resolve(HOST2)).thenReturn(asSet(HOST2_IP1, HOST2_IP2));
        when(resolver.resolve(HOST3)).thenReturn(asSet(HOST3_IP1, HOST3_IP2));
        final SessionRecycler recycler = new SessionRecycler(sessionFactory, resolver, hosts, 5000);

        // spin
        recycler.run();

        // verify new sessions opened
        verify(sessionFactory, times(6)).openSession(any(InetSocketAddress.class));
        verify(sessionFactory).openSession(host11);
        verify(sessionFactory).openSession(host21);
        verify(sessionFactory).openSession(host31);
        verify(sessionFactory).openSession(host12);
        verify(sessionFactory).openSession(host22);
        verify(sessionFactory).openSession(host32);


        final Set<SocketAddress> sessions = new HashSet(asList(host11, host21, host31, host12, host22, host32));
        when(sessionFactory.getCurrentSessionAddresses()).thenReturn(sessions);

        // spin
        recycler.run();

        // verify no new sessions opened
        verify(sessionFactory, times(6)).openSession(any(InetSocketAddress.class));

        // verify no sessions closed
        verify(sessionFactory, never()).closeSession(any(SocketAddress.class), eq(false));
    }

    @Test
    public void testPortNumberResolution() throws Exception {
        final InetSocketAddress host11 = new InetSocketAddress(HOST1_IP1, 9003);
        final InetSocketAddress host12 = new InetSocketAddress(HOST1_IP2, 9003);
        final InetSocketAddress host21 = new InetSocketAddress(HOST2_IP1, 9004);
        final InetSocketAddress host22 = new InetSocketAddress(HOST2_IP2, 9004);
        final InetSocketAddress host31 = new InetSocketAddress(HOST3_IP1, 9005);
        final InetSocketAddress host32 = new InetSocketAddress(HOST3_IP2, 9005);

        NetworkAddressResolver resolver = mock(NetworkAddressResolver.class);
        when(resolver.resolve(HOST1)).thenReturn(asSet(HOST1_IP1, HOST1_IP2));
        when(resolver.resolve(HOST2)).thenReturn(asSet(HOST2_IP1, HOST2_IP2));
        when(resolver.resolve(HOST3)).thenReturn(asSet(HOST3_IP1 + ":9005", HOST3_IP2 + ":9005"));

        String hostsString = "HOST1,HOST2:9004,HOST3:9001";

        final SessionRecycler recycler = new SessionRecycler(sessionFactory, resolver, hostsString, 5000);

        // spin
        recycler.run();

        // verify new sessions opened
        verify(sessionFactory, times(6)).openSession(any(InetSocketAddress.class));
        verify(sessionFactory).openSession(host11);
        verify(sessionFactory).openSession(host21);
        verify(sessionFactory).openSession(host31);
        verify(sessionFactory).openSession(host12);
        verify(sessionFactory).openSession(host22);
        verify(sessionFactory).openSession(host32);
    }

    private Set<String> asSet(String... values) {
        if (values == null || values.length == 0) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet(values.length);
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    private IoSession getSession(InetSocketAddress address) {
        IoSession ioSession = getSession(1);
        when(ioSession.getRemoteAddress()).thenReturn(address);
        return ioSession;
    }

    private IoSession getSession(int id) {
        final IoSession ioSession = mock(IoSession.class);
        when(ioSession.isConnected()).thenReturn(true);
        when(ioSession.isClosing()).thenReturn(false);
        when(ioSession.containsAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name())).thenReturn(false);
        when(ioSession.containsAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name())).thenReturn(false);
        return ioSession;
    }
}
