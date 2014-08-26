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

package com.betfair.cougar.transport.socket;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.core.impl.ev.ConnectedResponseImpl;
import com.betfair.cougar.core.impl.ev.DefaultSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TerminateSubscription;
import com.betfair.cougar.netutil.nio.connected.*;
import com.betfair.cougar.test.ParameterizedMultiRunner;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.socket.NewHeapSubscription;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.MutableHeap;
import com.betfair.platform.virtualheap.NodeType;
import com.betfair.platform.virtualheap.ObservableHeap;
import junit.framework.AssertionFailedError;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import static com.betfair.platform.virtualheap.projection.ProjectorFactory.objectProjector;
import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(value = ParameterizedMultiRunner.class)
public class PooledServerConnectedObjectManagerTest {

    private static Logger LOGGER = LoggerFactory.getLogger(PooledServerConnectedObjectManagerTest.class);

    private PooledServerConnectedObjectManager subject;
    private ExpectingOutput cougarOutput;
    private int ioSessionId;

    private int numThreads;

    public PooledServerConnectedObjectManagerTest(int numThreads) {
        this.numThreads = numThreads;
    }

    @ParameterizedMultiRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{1}, {2}});
    }

    @BeforeClass
    public static void multiSetup() {
        ParameterizedMultiRunner.setNumRuns(Integer.parseInt(System.getProperty("connectedObjects.numTestRuns", "1")));
    }

    @Before
    public void defaults() throws Exception {
        subject = new PooledServerConnectedObjectManager();
        subject.setNumProcessingThreads(numThreads);
        subject.setNioLogger(new NioLogger("ALL"));
        CougarObjectIOFactory ioFactory;
        subject.setObjectIOFactory(ioFactory = mock(CougarObjectIOFactory.class));
        subject.setEventLogger(new EventLogger() {
            @Override
            public void logEvent(LoggableEvent event) {
            }

            @Override
            public void logEvent(LoggableEvent loggableEvent, Object[] extensionFields) {
            }
        });
        cougarOutput = new ExpectingOutput(1000L);
        doReturn(cougarOutput).when(ioFactory).newCougarObjectOutput(any(ByteArrayOutputStream.class),anyByte());
        subject.start();
    }

    @After
    public void after() {
        subject.stop();
    }

    @Test
    public void firstSubscription() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("firstSubscription");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        ArgumentCaptor<ExecutionResult> resultCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(commandProcessor).writeSuccessResponse(any(SocketTransportRPCCommand.class), resultCaptor.capture(), any(DehydratedExecutionContext.class));

        ExecutionResult executionResult = resultCaptor.getValue();

        assertTrue(executionResult.getResult() instanceof NewHeapSubscription);
        NewHeapSubscription response = (NewHeapSubscription) executionResult.getResult();
        assertEquals(1, response.getHeapId());
        assertEquals("firstSubscription", response.getUri());
    }

    @Test
    public void secondSubscriptionToSameHeap() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("secondSubscription");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        ConnectedResponse subscriptionResult2 = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        cougarOutput.setExpectedUpdates(expectedUpdates);

        // 2 subs at about the same time, we're interested in the second..
        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);
        subject.addSubscription(commandProcessor, command, subscriptionResult2, operationDefinition, requestContext, null);

        ArgumentCaptor<ExecutionResult> resultCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(commandProcessor, times(2)).writeSuccessResponse(any(SocketTransportRPCCommand.class), resultCaptor.capture(), any(DehydratedExecutionContext.class));

        ExecutionResult executionResult = resultCaptor.getAllValues().get(1);

        assertTrue(executionResult.getResult() instanceof NewHeapSubscription);
        NewHeapSubscription response = (NewHeapSubscription) executionResult.getResult();
        assertEquals(1, response.getHeapId());
        assertNull(response.getUri());

        // There should be only one subscription to the main heap
        assertEquals(1, getHeapListeners(heap).size());

        assertExpectedUpdatesWritten();

        assertExpectedSessionWrites(session, 1);
    }

    @Test
    public void twoSubscriptionsToDifferentHeaps() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        // 2 subs at about the same time, we're interested in the second..
        Subscription sub = mock(Subscription.class);
        subject.addSubscription(commandProcessor, command, new ConnectedResponseImpl(new MutableHeap("firstHeap"), sub), operationDefinition, requestContext, null);
        subject.addSubscription(commandProcessor, command, new ConnectedResponseImpl(new MutableHeap("secondHeap"), sub), operationDefinition, requestContext, null);

        ArgumentCaptor<ExecutionResult> resultCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(commandProcessor, times(2)).writeSuccessResponse(any(SocketTransportRPCCommand.class), resultCaptor.capture(), any(DehydratedExecutionContext.class));

        ExecutionResult executionResult0 = resultCaptor.getAllValues().get(0);
        assertTrue(executionResult0.getResult() instanceof NewHeapSubscription);
        NewHeapSubscription response0 = (NewHeapSubscription) executionResult0.getResult();
        assertEquals(1, response0.getHeapId());
        assertEquals("firstHeap", response0.getUri());

        ExecutionResult executionResult1 = resultCaptor.getAllValues().get(1);
        assertTrue(executionResult1.getResult() instanceof NewHeapSubscription);
        NewHeapSubscription response1 = (NewHeapSubscription) executionResult1.getResult();
        assertEquals(2, response1.getHeapId());
        assertEquals("secondHeap", response1.getUri());
    }

    @Test
    public void subscribeToTerminatedHeap() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        IoSession session = new MyIoSession("1");
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("subscribeToTerminatedHeap");
        heap.beginUpdate();
        heap.terminateHeap();
        heap.endUpdate();
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        verify(commandProcessor).writeErrorResponse(any(SocketTransportCommand.class), any(DehydratedExecutionContext.class), any(CougarFrameworkException.class), eq(true));

        assertNull(subject.getHeapsByClient().get(session));
        assertEquals(0, subject.getHeapStates().size());

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void secondSubscribeToTerminatedHeap() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("subscribeToTerminatedHeap");
        Subscription sub1 = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub1);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);
        verify(commandProcessor).writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class));

        heap.beginUpdate();
        heap.terminateHeap();
        heap.endUpdate();

        Subscription sub2 = mock(Subscription.class);
        subscriptionResult = new ConnectedResponseImpl(heap, sub2);
        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        verify(commandProcessor).writeErrorResponse(any(SocketTransportCommand.class), any(DehydratedExecutionContext.class), any(CougarFrameworkException.class), eq(true));

        assertNull(subject.getHeapsByClient().get(session));
        assertEquals(0, subject.getHeapStates().size());

        // sub should have been closed
        verify(sub1).close(Subscription.CloseReason.REQUESTED_BY_PUBLISHER);
        verify(sub2, never()).close();
        verify(sub2, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void basicUpdate() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("basicUpdate");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void basicMultiUpdate() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("basicMultiUpdate");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1), new SetScalar(1, 2), new SetScalar(1, 3)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        heap.beginUpdate();
        object.value().set(2);
        heap.endUpdate();

        heap.beginUpdate();
        object.value().set(3);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void basicUpdateToTwoSessions() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("basicUpdateToTwoSessions");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        MyIoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));
        session2.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session2);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        assertEquals(3, cougarOutput.getAllValues().size());

        assertExpectedSessionWrites(session, 2);
        assertExpectedSessionWrites(session2, 2);

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void basicMultiUpdateToTwoSessions() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        ArgumentCaptor<ExecutionResult> resultCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), resultCaptor.capture(), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("basicMultiUpdateToTwoSessions");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1), new SetScalar(1, 2), new SetScalar(1, 3)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        MyIoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));
        session2.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session2);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(3);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 2;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);
        assertExpectedSessionWrites(session2, updatesWritten + 1);

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void addSubscriptionMidStream() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("addSubscriptionMidStream");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        expectedUpdates.add(createUpdate(new SetScalar(1, 2), new SetScalar(1, 3)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(3);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void addSecondSubscriptionMidStream() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("addSecondSubscriptionMidStream");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        expectedUpdates.add(createUpdate(new SetScalar(1, 2)));
        // new initial for new sub
        expectedUpdates.add(createInitial(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 2)));
        expectedUpdates.add(createUpdate(new SetScalar(1, 3)));

        cougarOutput.setExpectedUpdates(expectedUpdates);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        MyIoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));
        session2.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session2);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(3);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 2;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);
        assertExpectedSessionWrites(session2, updatesWritten); // +1 for initial update, but -1 for the one this doesn't see

        verify(sub, never()).close();
        verify(sub, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void heapTerminationMidStream() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("heapTerminationMidStream");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1), new SetScalar(1, 2), new TerminateHeap()));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        // we need this later to ensure that all the work associated with the termination is done...
        Lock heapStateLock = subject.getHeapStates().get("heapTerminationMidStream").getUpdateLock();

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        heap.beginUpdate();
        heap.terminateHeap();
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        // if we've locked and unlocked, then it's safe to query stuff affected within the lock..
        try {
            heapStateLock.lock();
        } finally {
            heapStateLock.unlock();
        }

        assertNull(subject.getHeapsByClient().get(session));
        assertNull(subject.getHeapStates().get("heapTerminationMidStream"));

        // sub should have been closed
        verify(sub).close(Subscription.CloseReason.REQUESTED_BY_PUBLISHER);
    }

    @Test
    public void sessionClosed() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++)) {
            // In practice, PSCOMT gets registered as a handler listener and
            // thus gets notified automatically when a session is closed
            // Doing it here explicitly for the purpose of the test
            @Override
            public CloseFuture close() {
                subject.sessionClosed(this);
                return super.close();
            }
        };
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("sessionClosed");
        Subscription subscription = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, subscription);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        session.close();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(3);
        heap.endUpdate();

        assertNull(subject.getHeapsByClient().get(session));
        assertNull(subject.getHeapStates().get("sessionClosed"));

        // sub should have been closed as it was the last sub
        verify(subscription).close(Subscription.CloseReason.CONNECTION_CLOSED);

        // There should be no listeners on the main heap
        assertEquals(0, getHeapListeners(heap).size());
    }

    @Test
    public void oneOfTwoSessionsClosed() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("oneOfTwoSessionsClosed");
        Subscription subscription1 = mock(Subscription.class);
        Subscription subscription2 = mock(Subscription.class);
        ConnectedResponse subscriptionResult1 = new ConnectedResponseImpl(heap, subscription1);
        ConnectedResponse subscriptionResult2 = new ConnectedResponseImpl(heap, subscription2);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1), new SetScalar(1, 2), new SetScalar(1, 3)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);
        subject.addSubscription(commandProcessor, command, subscriptionResult1, operationDefinition, requestContext, null);

        MyIoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));
        session2.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session2);
        subject.addSubscription(commandProcessor, command, subscriptionResult2, operationDefinition, requestContext, null);

        // make sure initial sub goes through..
        session.awaitWrite(1, 2000L);
        session2.awaitWrite(1, 2000L);

        // we need this later to ensure that all the work associated with the termination is done...
        Lock heapStateLock = subject.getHeapStates().get("oneOfTwoSessionsClosed").getUpdateLock();
        // and has been fully processed
        try {
            heapStateLock.lock();
        } finally {
            heapStateLock.unlock();
        }

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        session.close();
        subject.sessionClosed(session);

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(3);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 2;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session2, updatesWritten + 1);

        assertNull(subject.getHeapsByClient().get(session));
        assertEquals(1, subject.getHeapsByClient().get(session2).keySet().size());
        assertEquals(1, subject.getHeapStates().get("oneOfTwoSessionsClosed").getSessions().size());
        assertEquals(0, subject.getHeapStates().get("oneOfTwoSessionsClosed").getQueuedChanges().size());

        verify(subscription1).close(Subscription.CloseReason.CONNECTION_CLOSED);
        verify(subscription2, never()).close();
        verify(subscription2, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void exceptionInPusher() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("exceptionInPusher");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        // this last one we'll see on the cougarOutput, but not on the session write (since it's gonna fail)
        expectedUpdates.add(createUpdate(new SetScalar(1, 2)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        // we need this later to ensure that all the work associated with the termination is done...
        Lock heapStateLock = subject.getHeapStates().get("exceptionInPusher").getUpdateLock();

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        session.awaitWrite(2, 2000L); // ensures we've had something through after the initial update

        session.throwExceptionOnNextWrite();

        heap.beginUpdate();
        object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(2);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 2;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        // make sure all the work is complete!
        try {
            heapStateLock.lock();
        } finally {
            heapStateLock.unlock();
        }

        assertNull(subject.getHeapsByClient().get(session));
        assertNull(subject.getHeapStates().get("exceptionInPusher"));

        verify(sub).close(Subscription.CloseReason.INTERNAL_ERROR);
    }

    @Test
    public void secondSubscriptionClosedByPublisher() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createInitial());
        cougarOutput.setExpectedUpdates(expectedUpdates);

        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        MutableHeap heap = new MutableHeap("secondSubscriptionClosedByPublisher");
        Subscription sub1 = mock(Subscription.class);
        ConnectedResponse subscriptionResult1 = new ConnectedResponseImpl(heap, sub1);

        subject.addSubscription(commandProcessor, command, subscriptionResult1, operationDefinition, requestContext, null);

        Subscription sub2 = new DefaultSubscription();
        ConnectedResponse subscriptionResult2 = new ConnectedResponseImpl(heap, sub2);

        subject.addSubscription(commandProcessor, command, subscriptionResult2, operationDefinition, requestContext, null);

        String subscriptionId2 = getSubscriptionId(subject.getHeapStates().get("secondSubscriptionClosedByPublisher"), sub2);
        cougarOutput.getExpectedSubTerminations().add(new TerminateSubscription(1, subscriptionId2, Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name()));

        session.awaitWrite(1, 2000L); // ensures we've had the initial updates through

        sub2.close(Subscription.CloseReason.REQUESTED_BY_PUBLISHER);

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        verify(sub1, never()).close();
        verify(sub1, never()).close(any(Subscription.CloseReason.class));
    }

    @Test
    public void lastSubscriptionClosedByPublisher() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("lastSubscriptionClosedByPublisher");
        Subscription sub = new DefaultSubscription();
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        String subscriptionId = getSubscriptionId(subject.getHeapStates().get("lastSubscriptionClosedByPublisher"), sub);
        cougarOutput.getExpectedSubTerminations().add(new TerminateSubscription(1, subscriptionId, Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name()));

        session.awaitWrite(1, 2000L); // ensures we've had the initial update through

        sub.close(Subscription.CloseReason.REQUESTED_BY_PUBLISHER);

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        assertNull(subject.getHeapsByClient().get(session));
        assertNull(subject.getHeapStates().get("lastSubscriptionClosedByPublisher"));
    }

    @Test
    public void subscriptionCloseNotificationFails() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("subscriptionCloseNotificationFails");
        Subscription sub = new DefaultSubscription();
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        String subscriptionId = getSubscriptionId(subject.getHeapStates().get("subscriptionCloseNotificationFails"), sub);
        cougarOutput.getExpectedSubTerminations().add(new TerminateSubscription(1, subscriptionId, Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name()));

        session.awaitWrite(1, 2000L); // ensures we've had the initial update through

        session.throwExceptionOnNextWrite();

        sub.close(Subscription.CloseReason.REQUESTED_BY_PUBLISHER);

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten);

        assertNull(subject.getHeapsByClient().get(session));
        assertNull(subject.getHeapStates().get("subscriptionCloseNotificationFails"));
    }

    @Test
    public void secondSubscriptionClosedBySubscriber() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        MutableHeap heap = new MutableHeap("secondSubscriptionClosedBySubscriber");
        Subscription sub1 = mock(Subscription.class);
        ConnectedResponse subscriptionResult1 = new ConnectedResponseImpl(heap, sub1);

        subject.addSubscription(commandProcessor, command, subscriptionResult1, operationDefinition, requestContext, null);

        Subscription sub2 = mock(Subscription.class);
        ConnectedResponse subscriptionResult2 = new ConnectedResponseImpl(heap, sub2);

        subject.addSubscription(commandProcessor, command, subscriptionResult2, operationDefinition, requestContext, null);

        String subscriptionId2 = getSubscriptionId(subject.getHeapStates().get("secondSubscriptionClosedBySubscriber"), sub2);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        subject.terminateSubscription(session, new TerminateSubscription(1, subscriptionId2, Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER.name()));

        verify(sub1, never()).close();
        verify(sub1, never()).close(any(Subscription.CloseReason.class));
        verify(sub2).close(Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER);
    }

    @Test
    public void lastSubscriptionClosedBySubscriber() throws Exception {
        SocketTransportCommandProcessor commandProcessor = mock(SocketTransportCommandProcessor.class);
        when(commandProcessor.writeSuccessResponse(any(SocketTransportRPCCommand.class), any(ExecutionResult.class), any(DehydratedExecutionContext.class))).thenReturn(true);

        SocketTransportRPCCommand command = mock(SocketTransportRPCCommand.class);
        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        session.setAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME, CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        when(command.getSession()).thenReturn(session);

        DehydratedExecutionContext requestContext = mock(DehydratedExecutionContext.class);

        MutableHeap heap = new MutableHeap("lastSubscriptionClosedBySubscriber");
        Subscription sub = mock(Subscription.class);
        ConnectedResponse subscriptionResult = new ConnectedResponseImpl(heap, sub);
        OperationDefinition operationDefinition = mock(OperationDefinition.class);

        List<Update> expectedUpdates = new ArrayList<Update>();
        expectedUpdates.add(createInitial());
        expectedUpdates.add(createUpdate(new InstallRoot(0, NodeType.OBJECT), new InstallField(0, 1, "value", NodeType.SCALAR), new SetScalar(1, 1)));
        cougarOutput.setExpectedUpdates(expectedUpdates);

        subject.addSubscription(commandProcessor, command, subscriptionResult, operationDefinition, requestContext, null);

        String subscriptionId = getSubscriptionId(subject.getHeapStates().get("lastSubscriptionClosedBySubscriber"), sub);

        heap.beginUpdate();
        SimpleConnectedObject object = objectProjector(SimpleConnectedObject.class).project(heap.ensureRoot(NodeType.OBJECT));
        object.value().set(1);
        heap.endUpdate();

        assertExpectedUpdatesWritten();

        // might be related to the optimisation whereby if we need to send the same message to multiple clients we serialise it only once
        int updatesWritten = cougarOutput.getAllValues().size() - 1;

        // +1 to include the initial update for that session
        assertExpectedSessionWrites(session, updatesWritten + 1);

        subject.terminateSubscription(session, new TerminateSubscription(1, subscriptionId, Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER.name()));

        verify(sub).close(Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER);
        assertNull(subject.getHeapsByClient().get(session));
        assertNull(subject.getHeapStates().get("lastSubscriptionClosedBySubscriber"));

        assertEquals(0, getHeapListeners(heap).size());
    }

    private String getSubscriptionId(PooledServerConnectedObjectManager.HeapState heapState, Subscription sub) {
        Map<String, PooledServerConnectedObjectManager.HeapState.SubscriptionDetails> subs = heapState.getSubscriptions();
        String subscriptionId = null;
        for (String id : subs.keySet()) {
            PooledServerConnectedObjectManager.HeapState.SubscriptionDetails sd = subs.get(id);
            if (sd.subscription == sub) {
                subscriptionId = id;
            }
        }
        return subscriptionId;
    }

    private void assertExpectedUpdatesWritten() throws InterruptedException {
        final AtomicReference<String> failureText = new AtomicReference<String>();
        cougarOutput.addListener(new ExpectingOutput.ExpectingOutputListener() {
            @Override
            public void failure(String s) {
                failureText.set(s);
            }

            @Override
            public void complete() {
            }
        });

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting wait for expected updates");
        }

        cougarOutput.start();

        BlockingDeque queue = subject.getHeapsWaitingForUpdate();
        while (!queue.isEmpty()) {
            Thread.sleep(10);
        }
        // queue empty, now check the heap stats
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (PooledServerConnectedObjectManager.HeapState heapState : subject.getHeapStates().values()) {
                Lock lock = heapState.getUpdateLock();
                lock.lock();
                try {
                    if (!heapState.getQueuedChanges().isEmpty()) {
                        allDone = false;
                        break;
                    }
                }
                finally {
                    lock.unlock();
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CougarObjectOutput.writeObject():");
            for (Object o : new ArrayList<Object>(cougarOutput.getAllValues())) {
                LOGGER.debug(String.valueOf(o));
            }
        }

        if (failureText.get() != null) {
            fail(failureText.get());
        }

        // add in checks for the terminate subs
        assertEquals(cougarOutput.getExpectedSubTerminations(), cougarOutput.getSubTerminations());
    }

    private void assertExpectedSessionWrites(MyIoSession session, int writes) throws InterruptedException {
        InterruptedException ie = null;
        AssertionFailedError afe = null;
        try {
            session.awaitWrite(writes, 2000L);
        } catch (InterruptedException ie1) {
            ie = ie1;
        } catch (AssertionFailedError afe1) {
            afe = afe1;
        }

        if (ie != null || afe != null || writes != session.getWritten().size()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("session(" + session.getSessionId() + ").write():");
                for (Object o : session.getWritten()) {
                    LOGGER.debug(String.valueOf(o));
                }
            }

            if (ie != null) {
                throw ie;
            }
            if (afe != null) {
                throw afe;
            }
        }

        assertEquals(writes, session.getWritten().size());
    }

    private InitialUpdate createInitial(UpdateAction... actions) {
        Update u = new Update();
        u.setActions(createActionsList(actions));
        return new InitialUpdate(u);
    }

    private Update createUpdate(UpdateAction... actions) {
        Update u = new Update();
        u.setActions(createActionsList(actions));
        return u;
    }

    private List<UpdateAction> createActionsList(UpdateAction... actions) {
        // not using this as it gives a list we can't call addAll() on
        //return Arrays.asList(actions);
        List<UpdateAction> ret = new ArrayList<UpdateAction>();
        Collections.addAll(ret, actions);
        return ret;
    }

    private Set<HeapListener> getHeapListeners(MutableHeap heap) throws Exception {
        Field listeners = ObservableHeap.class.getDeclaredField("listeners");
        listeners.setAccessible(true);
        return (Set<HeapListener>) listeners.get(heap);
    }

}
