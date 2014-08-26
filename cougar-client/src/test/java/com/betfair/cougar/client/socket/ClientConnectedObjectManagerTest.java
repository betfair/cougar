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

import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.ev.WaitingObserver;
import com.betfair.cougar.netutil.nio.HeapDelta;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.NioUtils;
import com.betfair.cougar.netutil.nio.TerminateSubscription;
import com.betfair.cougar.netutil.nio.connected.*;
import com.betfair.cougar.test.ParameterizedMultiRunner;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;
import com.betfair.cougar.transport.api.protocol.socket.NewHeapSubscription;
import com.betfair.cougar.transport.socket.InvocationResponseImpl;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.NodeType;
import com.betfair.platform.virtualheap.updates.UpdateBlock;
import org.apache.mina.common.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(value = ParameterizedMultiRunner.class)
public class ClientConnectedObjectManagerTest {

    private ClientConnectedObjectManager subject;
    private int ioSessionId = 1;

    private int numThreads;
    private CougarObjectIOFactory objectIOFactory;

    public ClientConnectedObjectManagerTest(int numThreads) {
        this.numThreads = numThreads;
    }

    @ParameterizedMultiRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{1},{2}});
    }

    @BeforeClass
    public static void multiSetup() {
        ParameterizedMultiRunner.setNumRuns(Integer.parseInt(System.getProperty("connectedObjects.numTestRuns","1")));
    }

    @Before
    public void before() {
        subject = new ClientConnectedObjectManager();
        subject.setMaxInitialPopulationWait(50L);
        subject.setPullerAwaitTimeout(20L);
        subject.setMissingDeltaTimeout(20L);
        subject.setMaxDeltaQueue(10);
        subject.setNioLogger(new NioLogger("TRANSPORT"));
        subject.setNumProcessingThreads(numThreads);
        objectIOFactory = mock(CougarObjectIOFactory.class);
        subject.setObjectIOFactory(objectIOFactory);
        subject.start();
    }

    @After
    public void after() {
        subject.stop();
    }

    @Test
    public void firstSubscription() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "firstSubscription");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta delta = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, delta);

        waitForAndAssertNotFault(observer);

        Subscription sub = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub);
        assertNull(sub.getCloseReason());
    }

    @Test
    public void firstSubscriptionNotReceivingInitialUpdate() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "firstSubscriptionNotReceivingInitialUpdate");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        subject.handleSubscriptionResponse(session, response, observer);

        assertTrue(observer.await(1000L));
        assertTrue(observer.getExecutionResult().isFault());

        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
    }

    @Test
    public void firstSubscriptionInitialUpdateReceivedLate() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "firstSubscriptionInitialUpdateReceivedLate");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));
        subject.handleSubscriptionResponse(session, response, observer);

        assertTrue(observer.await(1000L));
        assertTrue(observer.getExecutionResult().isFault());

        HeapDelta delta = new HeapDelta(1, 0, createUpdateList(createInitial(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, true))));
        subject.applyDelta(session, delta);

        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
    }

    @Test
    public void secondSubscriptionToSameHeap() throws Exception {
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "secondSubscriptionToSameHeap");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta delta = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, delta);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response2, observer2);

        waitForAndAssertNotFault(observer2);

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));
    }

    @Test
    public void twoSubscriptionsToDifferentHeaps() throws Exception {
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "twoSubscriptionsToDifferentHeaps-1");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        subject.applyDelta(session, new HeapDelta(1, 0, createUpdateList(createInitial())));

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(2, "sub2", "twoSubscriptionsToDifferentHeaps-2");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response2, observer2);

        subject.applyDelta(session, new HeapDelta(2, 0, createUpdateList(createInitial())));

        waitForAndAssertNotFault(observer2);

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));
    }

    @Test
    public void basicUpdate() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "basicUpdate");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForEnd(1000L));
    }

    @Test
    public void basicMultiUpdate() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "basicMultiUpdate");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(3);
        heap.addListener(listener, false);

        subject.applyDelta(session, new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1)))));
        subject.applyDelta(session, new HeapDelta(1, 2, createUpdateList(createUpdate(new SetScalar(0, 2)))));
        subject.applyDelta(session, new HeapDelta(1, 3, createUpdateList(createUpdate(new SetScalar(0, 3)))));

        assertTrue(listener.waitForEnd(1000L));
    }

    @Test
    public void basicUpdateFromTwoSessions() throws Exception {
        // session1 - initial sub
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "basicUpdateFromTwoSessions");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        // session2 - initial sub
        IoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2", "basicUpdateFromTwoSessions");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session2, response2, observer2);

        HeapDelta initial2 = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session2, initial2);

        // wait for initial completions
        waitForAndAssertNotFault(observer);
        waitForAndAssertNotFault(observer2);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));

        // session1 - basic update
        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        // session2 - basic update
        Heap heap2 = ((ConnectedResponse) observer2.getExecutionResult().getResult()).getHeap();
        WaitingListener listener2 = new WaitingListener(1);
        heap2.addListener(listener2, false);

        HeapDelta delta2 = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session2, delta2);

        // wait for basic updates
        assertTrue(listener.waitForEnd(1000L));
        assertTrue(listener2.waitForEnd(1000L));
    }

    @Test
    public void basicMultiUpdateToTwoSessions() throws Exception {
        // session1 - initial sub
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "basicMultiUpdateToTwoSessions");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        // session2 - initial sub
        IoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2", "basicMultiUpdateToTwoSessions");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session2, response2, observer2);

        HeapDelta initial2 = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session2, initial2);

        // wait for initial completions
        waitForAndAssertNotFault(observer);
        waitForAndAssertNotFault(observer2);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));

        // session1 - basic update
        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(3);
        heap.addListener(listener, false);

        subject.applyDelta(session, new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1)))));
        subject.applyDelta(session, new HeapDelta(1, 2, createUpdateList(createUpdate(new SetScalar(0, 2)))));
        subject.applyDelta(session, new HeapDelta(1, 3, createUpdateList(createUpdate(new SetScalar(0, 3)))));

        // session2 - basic update
        Heap heap2 = ((ConnectedResponse) observer2.getExecutionResult().getResult()).getHeap();
        WaitingListener listener2 = new WaitingListener(3);
        heap2.addListener(listener2, false);

        subject.applyDelta(session2, new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1)))));
        subject.applyDelta(session2, new HeapDelta(1, 2, createUpdateList(createUpdate(new SetScalar(0, 2)))));
        subject.applyDelta(session2, new HeapDelta(1, 3, createUpdateList(createUpdate(new SetScalar(0, 3)))));

        // wait for basic updates
        assertTrue(listener.waitForEnd(1000L));
        assertTrue(listener2.waitForEnd(1000L));
    }

    @Test
    public void addSubscriptionMidStream() throws Exception {
        // sub1 - initial sub
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "addSubscriptionMidStream");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        // wait for initial completion
        waitForAndAssertNotFault(observer);

        // sub1 - basic update (1)
        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        subject.applyDelta(session, new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1)))));


        // sub2 - initial sub
        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2", "1");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response2, observer2);

        // wait for initial completion
        waitForAndAssertNotFault(observer2);

        Heap heap2 = ((ConnectedResponse) observer2.getExecutionResult().getResult()).getHeap();
        WaitingListener listener2 = new WaitingListener(2);
        heap2.addListener(listener2, false);


        // more updates (for both
        listener.resetLatch(2);
        subject.applyDelta(session, new HeapDelta(1, 2, createUpdateList(createUpdate(new SetScalar(0, 2)))));
        subject.applyDelta(session, new HeapDelta(1, 3, createUpdateList(createUpdate(new SetScalar(0, 3)))));

        // wait for basic updates
        assertTrue(listener.waitForEnd(1000L));
        assertTrue(listener2.waitForEnd(1000L));

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));
    }

    @Test
    public void heapTerminationMidStream() throws Exception {
        // we don't need to test termination at the beginning as this is picked up by the server and returned as an operation exception on subscription

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "heapTerminationMidStream");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        Lock heapSubMutationLock = subject.getHeapSubMutationLock();

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new TerminateHeap())));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForHeapTermination(1000L));

        // now wait for it to exit the lock, so we can check it's done it's work
        try {
            heapSubMutationLock.lock();
        }
        finally {
            heapSubMutationLock.unlock();
        }

        assertEquals(Subscription.CloseReason.REQUESTED_BY_PUBLISHER, sub1.getCloseReason());

        // heap termination should trigger cleanup..
        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
    }

    @Test
    public void sessionClosed() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "basicUpdate");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        assertNotNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));

        subject.sessionTerminated(session);

        assertEquals(Subscription.CloseReason.CONNECTION_CLOSED, sub1.getCloseReason());

        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
    }

    @Test
    public void oneOfTwoSessionsClosed() throws Exception {
        // session1 - initial sub
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "oneOfTwoSessionsClosed1");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        // session2 - initial sub
        IoSession session2 = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2", "oneOfTwoSessionsClosed");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session2, response2, observer2);

        HeapDelta initial2 = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session2, initial2);

        // wait for initial completions
        waitForAndAssertNotFault(observer);
        waitForAndAssertNotFault(observer2);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));

        assertNotNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
        assertNotNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session2)));

        subject.sessionTerminated(session);

        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
        assertEquals(Subscription.CloseReason.CONNECTION_CLOSED, sub1.getCloseReason());
        assertNotNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session2)));
        assertNull(sub2.getCloseReason());
    }

    @Test
    public void exceptionInPuller() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "exceptionInPuller");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta mockedDelta = mock(HeapDelta.class);
//        when(mockedDelta.containsFirstUpdate()).thenThrow(new NullPointerException());
        when(mockedDelta.getHeapId()).thenReturn(1L);
        when(mockedDelta.getUpdateId()).thenReturn(2L);

        Lock heapUpdateLock = subject.getHeapsByServer().get(NioUtils.getSessionId(session)).getHeapState(1).getHeapUpdateLock();
        final CountDownLatch latch = new CountDownLatch(1);
        when(mockedDelta.containsFirstUpdate()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                throw new NullPointerException();
            }
        });

        subject.applyDelta(session, mockedDelta);

        // wait for our exception method to be called
        latch.await(1000, TimeUnit.MILLISECONDS);

        // now wait for it to exit the lock, so we can check it's done it's work
        try {
            heapUpdateLock.lock();
        }
        finally {
            heapUpdateLock.unlock();
        }

        // heap termination should trigger cleanup..
        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
        assertEquals(Subscription.CloseReason.INTERNAL_ERROR, sub1.getCloseReason());
    }

    @Test
    public void secondNewHeapSubscription() throws Exception {
        // if you get a second newheapsub sent then it shouldn't stop updates for previous connected objects..
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "secondNewHeapSubscription");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap1 = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener1 = new WaitingListener(1);
        heap1.addListener(listener1, false);

        // second new heap message
        newHeapSubscription = new NewHeapSubscription(1, "sub2", "secondNewHeapSubscription");
        response = new InvocationResponseImpl(newHeapSubscription);
        observer = new WaitingObserver();

        subject.handleSubscriptionResponse(session, response, observer);

        waitForAndAssertNotFault(observer);

        Subscription sub2 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        Heap heap2 = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener2 = new WaitingListener(1);
        heap2.addListener(listener2, false);

        // now fire in the update
        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        // this is going to be no prob
        assertTrue(listener2.waitForEnd(1000L));
        // this is the interesting one..
        assertTrue(listener1.waitForEnd(1000L));

        assertFalse(sub1.equals(sub2));
    }

    @Test
    public void secondNewHeapSubscriptionWithSameSubId() throws Exception {
        // if you get a second newheapsub sent then it shouldn't stop updates for previous connected objects..
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "secondNewHeapSubscription");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap1 = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener1 = new WaitingListener(1);
        heap1.addListener(listener1, false);

        // second new heap message
        newHeapSubscription = new NewHeapSubscription(1, "sub1", "secondNewHeapSubscription");
        response = new InvocationResponseImpl(newHeapSubscription);
        observer = new WaitingObserver();

        subject.handleSubscriptionResponse(session, response, observer);

//        initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
//        subject.applyDelta(session, initial);

        waitForAndAssertFault(observer);

        Subscription sub2 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNull(sub2);

        // heap termination should trigger cleanup..
        assertFalse(session.isConnected());
        // this doesn't happen as the listeners don't get called in these tests
        //assertEquals(Subscription.CloseReason.CONNECTION_CLOSED, sub1.getCloseReason());
    }

    @Test
    public void delayedDelta() throws Exception {
        // if the delta queue for a heap grows too long then we've lost a message and need to abort/disconnect
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "delayedDelta");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        // miss out update 2
        subject.applyDelta(session, new HeapDelta(1, 2, createUpdateList(createUpdate(new SetScalar(0, true)))));

        // timeout is 20ms + puller timeout of 20ms, but tests sometimes break with a 50ms timeout..

        // disconnection should trigger cleanup..
        awaitRemoval(subject.getHeapsByServer(), NioUtils.getSessionId(session), 1000);

        assertEquals(Subscription.CloseReason.INTERNAL_ERROR, sub1.getCloseReason());
    }

    private <T> void awaitRemoval(Map<T, ? extends Object> map, T key, long millis) {
        long expiryTime = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < expiryTime) {
            if (!map.containsKey(key)) {
                return;
            }
            try {
                Thread.sleep(1);
            }
            catch (InterruptedException ie) {}
        }
        fail("Key value ("+key+") not removed within "+millis+"ms, value is: "+map.get(key));
    }

    @Test
    public void lostDelta() throws Exception {
        // if a delta goes missing for too long (ie a gap) we need to abort/disconnect
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "lostDelta");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        // miss out update 2, max queue is 10, so lets send 11 updates
        subject.applyDelta(session, new HeapDelta(1, 2, createUpdateList(createUpdate(new SetScalar(0, true)))));
        subject.applyDelta(session, new HeapDelta(1, 3, createUpdateList(createUpdate(new SetScalar(0, false)))));
        subject.applyDelta(session, new HeapDelta(1, 4, createUpdateList(createUpdate(new SetScalar(0, true)))));
        subject.applyDelta(session, new HeapDelta(1, 5, createUpdateList(createUpdate(new SetScalar(0, false)))));
        subject.applyDelta(session, new HeapDelta(1, 6, createUpdateList(createUpdate(new SetScalar(0, true)))));
        subject.applyDelta(session, new HeapDelta(1, 7, createUpdateList(createUpdate(new SetScalar(0, false)))));
        subject.applyDelta(session, new HeapDelta(1, 8, createUpdateList(createUpdate(new SetScalar(0, true)))));
        subject.applyDelta(session, new HeapDelta(1, 9, createUpdateList(createUpdate(new SetScalar(0, false)))));
        subject.applyDelta(session, new HeapDelta(1, 10, createUpdateList(createUpdate(new SetScalar(0, true)))));
        subject.applyDelta(session, new HeapDelta(1, 11, createUpdateList(createUpdate(new SetScalar(0, false)))));
        subject.applyDelta(session, new HeapDelta(1, 12, createUpdateList(createUpdate(new SetScalar(0, true)))));

        // now wait 2x the await timeout, which handily is 20ms, for everything to queue up nicely and be processed
        Thread.sleep(40);

        // disconnection should trigger cleanup..
        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));

        assertEquals(Subscription.CloseReason.INTERNAL_ERROR, sub1.getCloseReason());
    }

    @Test
    public void secondSubscriptionClosedByPublisher() throws Exception {
        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "secondSubscriptionClosedByPublisher");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response2, observer2);

        waitForAndAssertNotFault(observer2);

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));

        subject.terminateSubscription(session, new TerminateSubscription(1, "sub2", Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name()));

        assertNull(sub1.getCloseReason());
        assertEquals(Subscription.CloseReason.REQUESTED_BY_PUBLISHER, sub2.getCloseReason());
    }

    @Test
    public void lastSubscriptionClosedByPublisher() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "lastSubscriptionClosedByPublisher");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForEnd(1000L));

        subject.terminateSubscription(session, new TerminateSubscription(1, "sub1", Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name()));

        assertEquals(Subscription.CloseReason.REQUESTED_BY_PUBLISHER, sub1.getCloseReason());
    }

    @Test
    public void nonExistingSubscriptionClosedByPublisher() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "nonExistingSubscriptionClosedByPublisher");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        IoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForEnd(1000L));

        // mostly checking this doesn't throw an exception..
        subject.terminateSubscription(session, new TerminateSubscription(1, "sub2", Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name()));

        assertNull(sub1.getCloseReason());
    }

    @Test
    public void secondSubscriptionClosedBySubscriber() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "secondSubscriptionClosedBySubscriber");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        NewHeapSubscription newHeapSubscription2 = new NewHeapSubscription(1, "sub2");
        InvocationResponse response2 = new InvocationResponseImpl(newHeapSubscription2);
        WaitingObserver observer2 = new WaitingObserver();
        subject.handleSubscriptionResponse(session, response2, observer2);

        waitForAndAssertNotFault(observer2);

        Subscription sub2 = getSubscriptionFrom(observer2.getExecutionResult().getResult());
        assertNotNull(sub2);
        assertNull(sub2.getCloseReason());

        assertFalse(sub1.equals(sub2));

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForEnd(1000L));

        CougarObjectOutput coo = mock(CougarObjectOutput.class);
        when(objectIOFactory.newCougarObjectOutput(any(OutputStream.class), anyByte())).thenReturn(coo);

        sub2.close();

        assertEquals(Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER, sub2.getCloseReason());
        assertNull(sub1.getCloseReason());
    }

    @Test
    public void lastSubscriptionClosedBySubscriber() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "lastSubscriptionClosedBySubscriber");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForEnd(1000L));

        CougarObjectOutput coo = mock(CougarObjectOutput.class);
        when(objectIOFactory.newCougarObjectOutput(any(OutputStream.class), anyByte())).thenReturn(coo);

        sub1.close();

        assertEquals(Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER, sub1.getCloseReason());
    }

    @Test
    public void subscriptionClosedBySubscriberAndNotificationFails() throws Exception {
        NewHeapSubscription newHeapSubscription = new NewHeapSubscription(1, "sub1", "subscriptionClosedBySubscriberAndNotificationFails");
        InvocationResponse response = new InvocationResponseImpl(newHeapSubscription);
        WaitingObserver observer = new WaitingObserver();

        MyIoSession session = new MyIoSession(String.valueOf(ioSessionId++));

        subject.handleSubscriptionResponse(session, response, observer);

        HeapDelta initial = new HeapDelta(1, 0, createUpdateList(createInitial()));
        subject.applyDelta(session, initial);

        waitForAndAssertNotFault(observer);

        Subscription sub1 = getSubscriptionFrom(observer.getExecutionResult().getResult());
        assertNotNull(sub1);
        assertNull(sub1.getCloseReason());

        Heap heap = ((ConnectedResponse) observer.getExecutionResult().getResult()).getHeap();
        WaitingListener listener = new WaitingListener(1);
        heap.addListener(listener, false);

        HeapDelta delta = new HeapDelta(1, 1, createUpdateList(createUpdate(new InstallRoot(0, NodeType.SCALAR), new SetScalar(0, 1))));
        subject.applyDelta(session, delta);

        assertTrue(listener.waitForEnd(1000L));

        when(objectIOFactory.newCougarObjectOutput(any(OutputStream.class), anyByte())).thenThrow(new RuntimeException());

        sub1.close();

        assertEquals(Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER, sub1.getCloseReason());
        // can't check this as this is triggered from session closure and our session isn't fully featured. This is tested in the sessionClosed test though..
        // assertEquals(Subscription.CloseReason.INTERNAL_ERROR, sub2.getCloseReason());

        assertNull(subject.getHeapsByServer().get(NioUtils.getSessionId(session)));
        assertTrue(session.getCloseFuture().isClosed());
    }

    private Subscription getSubscriptionFrom(Object result) {
        if (result == null) {
            return null;
        }
        return ((ConnectedResponse) result).getSubscription();
    }

    private void waitForAndAssertNotFault(WaitingObserver observer) throws InterruptedException {
        assertTrue(observer.await(1000L));
        if (observer.getExecutionResult().isFault()) {
            fail(observer.getExecutionResult().getFault().getMessage());
        }
    }

    private void waitForAndAssertFault(WaitingObserver observer) throws InterruptedException {
        assertTrue(observer.await(1000L));
        if (!observer.getExecutionResult().isFault()) {
            fail("Was expecting a fault");
        }
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

    private List<Update> createUpdateList(Update... updates) {
        List<Update> ret = new ArrayList<Update>();
        Collections.addAll(ret, updates);
        return ret;
    }


    private class WaitingListener implements HeapListener {

        private CountDownLatch latch;
        private CountDownLatch terminationLatch = new CountDownLatch(1);

        public WaitingListener(int i) {
            latch = new CountDownLatch(i);
        }

        private WaitingListener() {
            this(1);
        }

        public boolean waitForEnd(long millis) throws InterruptedException {
            boolean ret = latch.await(millis, TimeUnit.MILLISECONDS);
            if (!ret) {
                System.out.println(latch.getCount());
            }
            return ret;
        }

        @Override
        public void applyUpdate(UpdateBlock update) {
            for (com.betfair.platform.virtualheap.updates.Update u : update.list()) {
                if (u.getUpdateType() == com.betfair.platform.virtualheap.updates.Update.UpdateType.TERMINATE_HEAP) {
                    terminationLatch.countDown();
                }
            }
            latch.countDown();
        }

        public void resetLatch(int i) {
            latch = new CountDownLatch(i);
        }

        public boolean waitForHeapTermination(long millis) throws InterruptedException {
            boolean ret = terminationLatch.await(millis, TimeUnit.MILLISECONDS);
            if (!ret) {
                System.out.println(terminationLatch.getCount());
            }
            return ret;
        }
    }
}
