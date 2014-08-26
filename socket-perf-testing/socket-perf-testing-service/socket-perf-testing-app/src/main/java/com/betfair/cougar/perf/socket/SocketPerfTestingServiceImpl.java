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

package com.betfair.cougar.perf.socket;

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.impl.ev.ConnectedResponseImpl;
import com.betfair.cougar.core.impl.ev.DefaultSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.MutableHeap;
import com.betfair.cougar.perf.socket.v1_0.SocketPerfTestingService;
import com.betfair.cougar.perf.socket.v1_0.co.HeapTesterCO;
import com.betfair.cougar.perf.socket.v1_0.co.RpcControlCO;
import com.betfair.cougar.perf.socket.v1_0.co.server.HeapTesterServerCO;
import com.betfair.cougar.perf.socket.v1_0.co.server.RpcControlServerCO;
import com.betfair.tornjak.monitor.MonitorRegistry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SocketPerfTestingServiceImpl implements SocketPerfTestingService {

    private static CougarLogger LOG = CougarLoggingUtils.getLogger(SocketPerfTestingServiceImpl.class);

    private int numHeapsForTest = 1000;
    private int concurrency = numHeapsForTest/3;
    private int numUpdates = 1000000;

    private ExecutorService executorService;
    private Heap[] heaps;
    private HeapTesterCO[] heapProjections;

    private Heap rpcControlHeap;
    private RpcControlCO rpcControlProjection;
    private AtomicInteger activeSubscriptions = new AtomicInteger();


    public SocketPerfTestingServiceImpl() {
    }

    public void setNumHeapsForTest(int numHeapsForTest) {
        this.numHeapsForTest = numHeapsForTest;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public void setNumUpdates(int numUpdates) {
        this.numUpdates = numUpdates;
    }

    @Override
    public void startRpcTest(RequestContext ctx, Long length , Integer numClientThreads) {
        rpcControlHeap.beginUpdate();
        rpcControlProjection.setNumThreads(numClientThreads != null ? numClientThreads : 1);
        rpcControlProjection.setRunning(true);
        rpcControlHeap.endUpdate();

        try {
            Thread.sleep(length);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Test ended early due to interrupted exception");
        }

        rpcControlHeap.beginUpdate();
        rpcControlProjection.setRunning(false);
        rpcControlHeap.endUpdate();
    }

    @Override
    public ConnectedResponse getRpcControl(RequestContext ctx) {
        return new ConnectedResponseImpl(rpcControlHeap, new DefaultSubscription());
    }

    @Override
    public void noop(RequestContext ctx) {
        // literally do nothing
    }

    @Override
    public void startPushTest(RequestContext ctx) {
        long startTime = System.currentTimeMillis();
        // slam 'em all in
        final CountDownLatch latch = new CountDownLatch(numHeapsForTest);
        for (int j=0; j<numHeapsForTest; j++) {
            final int heap = j;
//                LOG.log(Level.SEVERE, "Scheduling update "+update+" for "+heap);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i=0; i<numUpdates && activeSubscriptions.get() > 0; i++) {
                            final int update = i;
                            heaps[heap].beginUpdate();
                            heapProjections[heap].setValue(heapProjections[heap].getValue()+update);
                            heaps[heap].endUpdate();
                        }

                        heaps[heap].beginUpdate();
                        heaps[heap].terminateHeap();
                        heaps[heap].endUpdate();
//                            LOG.log(Level.SEVERE, "Send update "+update+" to "+heap);
                    }
                    catch (Exception e) {
                        LOG.log(Level.SEVERE, "Error", e);
                    }
                    latch.countDown();
                }
            };
            executorService.submit(r);
        }
        long midTime = System.currentTimeMillis();
        try {
            latch.await();
        }
        catch (InterruptedException ie) {
            // ignore
        }
        long endTime = System.currentTimeMillis();
        LOG.log(Level.SEVERE, "For test started at "+startTime+" took "+(midTime-startTime)+"ms to submit all updates");
        LOG.log(Level.SEVERE, "For test started at "+startTime+" took "+(endTime-startTime)+"ms to emit all updates");
    }

    @Override
    public Integer getNumHeapsForTest(RequestContext ctx) {
        return numHeapsForTest;
    }

    @Override
    public ConnectedResponse subscribeToHeap(RequestContext ctx, Integer heapNumber) {
        activeSubscriptions.incrementAndGet();
        DefaultSubscription ds = new DefaultSubscription();
        ds.addListener(new Subscription.SubscriptionListener() {
            @Override
            public void subscriptionClosed(Subscription subscription, Subscription.CloseReason reason) {
                activeSubscriptions.decrementAndGet();
            }
        });
        return new ConnectedResponseImpl(heaps[heapNumber], ds);
    }

    @Override
    public void init(ContainerContext cc) {
        heaps = new Heap[numHeapsForTest];
        heapProjections = new HeapTesterCO[numHeapsForTest];
        long totalExpected = 0;
        for (int i=0; i<numUpdates; i++) {
            totalExpected += i;
        }
        for (int i=0; i<numHeapsForTest; i++) {
            heaps[i] = new MutableHeap("push-perf-test-"+i);
            heaps[i].beginUpdate();
            heapProjections[i] = HeapTesterServerCO.rootFrom(heaps[i]);
            heapProjections[i].setExpectedFinalValue(totalExpected);
            heapProjections[i].setValue(0L);
            heaps[i].endUpdate();
        }
        executorService = Executors.newFixedThreadPool(concurrency);

        rpcControlHeap = new MutableHeap("rpcControl");
        rpcControlHeap.beginUpdate();
        rpcControlProjection = RpcControlServerCO.rootFrom(rpcControlHeap);
        rpcControlProjection.setNumThreads(1);
        rpcControlProjection.setRunning(false);
        rpcControlHeap.endUpdate();

        LOG.log(Level.SEVERE, "Running test with following parameters:");
        LOG.log(Level.SEVERE, "numHeapsForTest = "+numHeapsForTest);
        LOG.log(Level.SEVERE, "concurrency = "+concurrency);
        LOG.log(Level.SEVERE, "numUpdates = "+numUpdates);
    }
}
