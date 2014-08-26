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
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 */
public class SessionWriteQueueMonitoringTest {

    private SessionWriteQueueMonitoring subject;
    private IoSession session1;
    private IoSession session2;
    private IoFilter.NextFilter nextFilter;
    private IoFilter.WriteRequest writeRequest;
    private List<IoSession> sessionsOpened;
    private MBeanServer mBeanServer;

    @Before
    public void before() {
        subject = new SessionWriteQueueMonitoring(new NioLogger("NONE"), 5);
        session1 = mock(IoSession.class);
        session2 = mock(IoSession.class);
        nextFilter = mock(IoFilter.NextFilter.class);
        writeRequest = mock(IoFilter.WriteRequest.class);
        sessionsOpened = new ArrayList<IoSession>();
        mBeanServer = mock(MBeanServer.class);
        SessionWriteQueueMonitoring.setMBeanServer(mBeanServer);
    }

    @After
    public void after() throws Exception {
        for (IoSession session : sessionsOpened) {
            subject.sessionClosed(nextFilter, session);
        }
    }

    @Test
    public void canGetUpToLimit() throws Exception {
        when(session1.getAttribute("COUGAR_SESSION_ID")).thenReturn("00001");
        when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));

        subject.sessionOpened(nextFilter, session1);
        sessionsOpened.add(session1);

        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);

        verify(nextFilter, times(1)).sessionOpened(session1);
        verify(nextFilter, times(5)).filterWrite(session1, writeRequest);
        verify(session1, never()).close();
    }

    @Test
    public void overLimitTerminates() throws Exception {
        when(session1.getAttribute("COUGAR_SESSION_ID")).thenReturn("00001");
        when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));

        subject.sessionOpened(nextFilter, session1);
        sessionsOpened.add(session1);

        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);

        verify(nextFilter, times(1)).sessionOpened(session1);
        verify(nextFilter, times(5)).filterWrite(session1, writeRequest); // 6th attempt should not call this
        verify(session1, times(1)).close();
    }

    @Test
    public void countBackDown() throws Exception {
        when(session1.getAttribute("COUGAR_SESSION_ID")).thenReturn("00001");
        when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));

        ArgumentCaptor<SessionWriteQueueMonitor> monitorCaptor = ArgumentCaptor.forClass(SessionWriteQueueMonitor.class);
        ArgumentCaptor<ObjectName> objectNameCaptor = ArgumentCaptor.forClass(ObjectName.class);

        subject.sessionOpened(nextFilter, session1);
        sessionsOpened.add(session1);

        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.messageSent(nextFilter, session1, null);
        subject.messageSent(nextFilter, session1, null);
        subject.messageSent(nextFilter, session1, null);

        verify(mBeanServer, atLeastOnce()).registerMBean(monitorCaptor.capture(), objectNameCaptor.capture());
        assertEquals(0, monitorCaptor.getAllValues().get(0).getQueueDepth());
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1234"), objectNameCaptor.getAllValues().get(0));

        verify(nextFilter, times(1)).sessionOpened(session1);
        verify(nextFilter, times(3)).filterWrite(session1, writeRequest);
        verify(nextFilter, times(3)).messageSent(session1, null);
        verify(session1, never()).close();
    }

    @Test
    public void twoSessionsIndependent() throws Exception {
        when(session1.getAttribute("COUGAR_SESSION_ID")).thenReturn("00001");
        when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));

        when(session2.getAttribute("COUGAR_SESSION_ID")).thenReturn("00002");
        when(session2.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1235));

        subject.sessionOpened(nextFilter, session1);
        sessionsOpened.add(session1);

        subject.sessionOpened(nextFilter, session2);
        sessionsOpened.add(session2);

        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);

        verify(nextFilter, times(1)).sessionOpened(session1);
        verify(nextFilter, times(3)).filterWrite(session1, writeRequest);
        verify(nextFilter, times(1)).sessionOpened(session2);
        verify(nextFilter, times(3)).filterWrite(session2, writeRequest);
        verify(session1, never()).close();
    }

    @Test
    public void twoSessionsSameHostMonitoring() throws Exception {
        when(session1.getAttribute("COUGAR_SESSION_ID")).thenReturn("00001");
        when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));

        when(session2.getAttribute("COUGAR_SESSION_ID")).thenReturn("00002");
        when(session2.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1235));

        ArgumentCaptor monitorCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<ObjectName> objectNameCaptor = ArgumentCaptor.forClass(ObjectName.class);

        subject.sessionOpened(nextFilter, session1);
        sessionsOpened.add(session1);

        subject.sessionOpened(nextFilter, session2);
        sessionsOpened.add(session2);

        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);


        verify(mBeanServer, times(3)).registerMBean(monitorCaptor.capture(), objectNameCaptor.capture());

        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1234"), objectNameCaptor.getAllValues().get(0));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1235"), objectNameCaptor.getAllValues().get(2));
        assertEquals(4, ((SessionWriteQueueMonitor)monitorCaptor.getAllValues().get(0)).getQueueDepth());
        assertEquals(2, ((SessionWriteQueueMonitor)monitorCaptor.getAllValues().get(2)).getQueueDepth());

        assertEquals(new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost=localhost"), objectNameCaptor.getAllValues().get(1));
        assertEquals(6, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getTotalWriteQueueDepth());
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getNumSessions());
        assertEquals(4, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getMaxWriteQueueDepth());
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getMinWriteQueueDepth());
        assertEquals(3, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getMeanWriteQueueDepth());
        assertEquals(6, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getTotalWriteQueueDepth());

        verify(nextFilter, times(1)).sessionOpened(session1);
        verify(nextFilter, times(4)).filterWrite(session1, writeRequest);
        verify(nextFilter, times(1)).sessionOpened(session2);
        verify(nextFilter, times(2)).filterWrite(session2, writeRequest);
        verify(session1, never()).close();

        subject.sessionClosed(nextFilter, session1);
        sessionsOpened.remove(session1);

        subject.sessionClosed(nextFilter, session2);
        sessionsOpened.remove(session2);

        verify(nextFilter, times(1)).sessionClosed(session1);
        verify(nextFilter, times(1)).sessionClosed(session2);

        verify(mBeanServer, times(3)).unregisterMBean(objectNameCaptor.capture());

        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1234"), objectNameCaptor.getAllValues().get(3));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1235"), objectNameCaptor.getAllValues().get(4));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost=localhost"), objectNameCaptor.getAllValues().get(5));
    }

    @Test
    public void twoSessionsDifferentHostMonitoring() throws Exception {
        when(session1.getAttribute("COUGAR_SESSION_ID")).thenReturn("00001");
        when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));

        when(session2.getAttribute("COUGAR_SESSION_ID")).thenReturn("00002");
        when(session2.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost2", 1235));

        ArgumentCaptor monitorCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<ObjectName> objectNameCaptor = ArgumentCaptor.forClass(ObjectName.class);

        subject.sessionOpened(nextFilter, session1);
        sessionsOpened.add(session1);

        subject.sessionOpened(nextFilter, session2);
        sessionsOpened.add(session2);

        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session1, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);
        subject.filterWrite(nextFilter, session2, writeRequest);


        verify(mBeanServer, times(4)).registerMBean(monitorCaptor.capture(), objectNameCaptor.capture());

        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1234"), objectNameCaptor.getAllValues().get(0));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost2_1235"), objectNameCaptor.getAllValues().get(2));
        assertEquals(4, ((SessionWriteQueueMonitor)monitorCaptor.getAllValues().get(0)).getQueueDepth());
        assertEquals(2, ((SessionWriteQueueMonitor)monitorCaptor.getAllValues().get(2)).getQueueDepth());

        assertEquals(new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost=localhost"), objectNameCaptor.getAllValues().get(1));
        assertEquals(4, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getTotalWriteQueueDepth());
        assertEquals(1, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getNumSessions());
        assertEquals(4, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getMaxWriteQueueDepth());
        assertEquals(4, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getMinWriteQueueDepth());
        assertEquals(4, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getMeanWriteQueueDepth());
        assertEquals(4, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(1)).getTotalWriteQueueDepth());
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost=localhost2"), objectNameCaptor.getAllValues().get(3));
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(3)).getTotalWriteQueueDepth());
        assertEquals(1, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(3)).getNumSessions());
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(3)).getMaxWriteQueueDepth());
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(3)).getMinWriteQueueDepth());
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(3)).getMeanWriteQueueDepth());
        assertEquals(2, ((HostWriteQueueMonitor)monitorCaptor.getAllValues().get(3)).getTotalWriteQueueDepth());

        verify(nextFilter, times(1)).sessionOpened(session1);
        verify(nextFilter, times(4)).filterWrite(session1, writeRequest);
        verify(nextFilter, times(1)).sessionOpened(session2);
        verify(nextFilter, times(2)).filterWrite(session2, writeRequest);
        verify(session1, never()).close();

        subject.sessionClosed(nextFilter, session1);
        sessionsOpened.remove(session1);

        subject.sessionClosed(nextFilter, session2);
        sessionsOpened.remove(session2);

        verify(nextFilter, times(1)).sessionClosed(session1);
        verify(nextFilter, times(1)).sessionClosed(session2);

        verify(mBeanServer, times(4)).unregisterMBean(objectNameCaptor.capture());

        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost_1234"), objectNameCaptor.getAllValues().get(4));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost=localhost"), objectNameCaptor.getAllValues().get(5));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=sessionWriteQueueMonitor,remoteAddress=localhost2_1235"), objectNameCaptor.getAllValues().get(6));
        assertEquals(new ObjectName("CoUGAR.socket.transport:name=hostWriteQueueMonitor,remoteHost=localhost2"), objectNameCaptor.getAllValues().get(7));
    }



}
