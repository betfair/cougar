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

import com.betfair.cougar.api.ExecutionContextImpl;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.impl.CougarSpringCtxFactoryImpl;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.updates.UpdateBlock;
import com.betfair.cougar.perf.socket.v1_0.SocketPerfTestingSyncClient;
import com.betfair.cougar.perf.socket.v1_0.co.client.HeapTesterClientCO;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class PushMain {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: PushMain <client-name> <target host:port> <subscriptionConcurrency>");
            System.exit(1);
        }
        String name = args[0];
        String targetAddress = args[1];

        int subscriptionConcurrency = 1;
        if (args.length > 2) {
            subscriptionConcurrency = Integer.parseInt(args[2]);
        }

        System.setProperty("server.address", targetAddress);

        CougarSpringCtxFactoryImpl cougarSpringCtxFactory = new CougarSpringCtxFactoryImpl();
        ClassPathXmlApplicationContext context = cougarSpringCtxFactory.create();

        final SocketPerfTestingSyncClient client = (SocketPerfTestingSyncClient) context.getBean("perfTestClient");
        final ExecutionContextImpl ctx = new ExecutionContextImpl();
        ctx.setGeoLocationDetails(new GeoLocationDetails() {
            @Override
            public String getRemoteAddr() {
                return "127.0.0.1";
            }

            @Override
            public List<String> getResolvedAddresses() {
                return Collections.singletonList("127.0.0.1");
            }

            @Override
            public String getCountry() {
                return "GB";
            }

            @Override
            public boolean isLowConfidenceGeoLocation() {
                return false;
            }

            @Override
            public String getLocation() {
                return "Somewhere over the rainbow";
            }

            @Override
            public String getInferredCountry() {
                return "GB";
            }
        });
        int numHeaps = client.getNumHeapsForTest(ctx);
        final Heap[] heaps = new Heap[numHeaps];
        final Subscription[] subs = new Subscription[numHeaps];
        final HeapTesterClientCO[] heapProjections = new HeapTesterClientCO[numHeaps];
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch subCreationLatch = new CountDownLatch(numHeaps);
        ExecutorService subscriptionExecutorService = Executors.newFixedThreadPool(subscriptionConcurrency);
        long subStart = System.currentTimeMillis();
        for (int i=0; i<numHeaps; i++) {
            final int finalI = i;
            subscriptionExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    ConnectedResponse cr = client.subscribeToHeap(ctx, finalI);
                    heaps[finalI] = cr.getHeap();
                    subs[finalI] = cr.getSubscription();
                    heapProjections[finalI] = HeapTesterClientCO.rootFrom(heaps[finalI]);
                    // look for first update
                    heaps[finalI].addListener(new HeapListener() {
                        @Override
                        public void applyUpdate(UpdateBlock update) {
                            startLatch.countDown();
                        }
                    }, false);
                    subCreationLatch.countDown();
                }
            });
        }
        subCreationLatch.await();
        subscriptionExecutorService.shutdown();
        long subEnd = System.currentTimeMillis();
        System.out.println("["+name+"]: SUBSCRIPTIONS TOOK "+(subEnd-subStart)+" ms");

        final CountDownLatch terminationLatch = new CountDownLatch(numHeaps);
        for (int i=0; i<numHeaps; i++) {
            subs[i].addListener(new Subscription.SubscriptionListener() {
                @Override
                public void subscriptionClosed(Subscription subscription, Subscription.CloseReason reason) {
                    if (reason != Subscription.CloseReason.REQUESTED_BY_PUBLISHER) {
                        System.out.println("Subscription closed due to: "+reason);
                    }
                    terminationLatch.countDown();
                }
            });
        }

        System.out.println("["+name+"]: READY ("+numHeaps+")");
        startLatch.await();
        long startMillis = System.currentTimeMillis();
        System.out.println("["+name+"]: STARTED AT "+startMillis);
        terminationLatch.await();
        long endMillis = System.currentTimeMillis();
        System.out.println("["+name+"]: COMPLETED AT "+endMillis+" TOOK "+(endMillis-startMillis)+" ms");

        for (int i=0; i<numHeaps; i++) {
            long expectedValue = heapProjections[i].getExpectedFinalValue();
            long actualValue = heapProjections[i].getValue();
            if (expectedValue != actualValue) {
                System.err.println("Heap["+i+"]: Expected: "+expectedValue+", Actual: "+actualValue);
            }
        }
        System.exit(0);

    }
}
