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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextImpl;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.impl.CougarSpringCtxFactoryImpl;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.updates.UpdateBlock;
import com.betfair.cougar.perf.socket.v1_0.SocketPerfTestingSyncClient;
import com.betfair.cougar.perf.socket.v1_0.co.RpcControlCO;
import com.betfair.cougar.perf.socket.v1_0.co.client.RpcControlClientCO;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class RpcMain {

    private static List<TestRunner> runners;
    private static long startMillis;
    private static String name;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: RpcMain <client-name> <target host:port> [<clientExecutorPoolSize>]");
            System.exit(1);
        }
        name = args[0];
        String targetAddress = args[1];

        if (args.length > 2) {
            System.setProperty("cougar.client.socket.clientExecutor.maximumPoolSize", args[2]);
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

        ConnectedResponse rpcControlResponse = client.getRpcControl(ctx);
        Heap rpcControlHeap = rpcControlResponse.getHeap();
        final RpcControlCO rpcControl = RpcControlClientCO.rootFrom(rpcControlHeap);

        rpcControlHeap.addListener(new HeapListener() {
            @Override
            public void applyUpdate(UpdateBlock update) {
                if (rpcControl.getRunning()) {
                    startTest(rpcControl.getNumThreads(), client, ctx);
                }
                else {
                    stopTest();
                }
            }
        }, true);

        System.out.println("["+name+"]: READY");
    }

    private static void startTest(Integer integer, SocketPerfTestingSyncClient client, ExecutionContextImpl ctx) {
        startMillis = System.currentTimeMillis();
        runners = new ArrayList<TestRunner>();
        for (int i=0; i<integer; i++) {
            TestRunner tr = new TestRunner(client, ctx);
            runners.add(tr);
            Thread t = new Thread(tr);
            t.start();
        }
        System.out.println("["+name+"]: STARTED AT "+startMillis);
    }

    private static void stopTest() {
        // if it's null then we haven't started yet..
        if (runners != null) {
            long totalCalls = 0;
            for (TestRunner runner : runners) {
                totalCalls += runner.stop();
            }
            long endMillis = System.currentTimeMillis();
            long totalMillis = endMillis-startMillis;
            double meanCallsPerSecPerThread = ((double) (totalCalls/(totalMillis/1000))) / runners.size();
            double meanLatency = 1000 / meanCallsPerSecPerThread;
            System.out.println("["+name+"]: COMPLETED AT "+endMillis+" TOOK "+totalMillis+"ms @ "+(totalCalls/(totalMillis/1000))+" tps and "+meanLatency+" ms/call");
        }
    }

    private static class TestRunner implements Runnable {
        private SocketPerfTestingSyncClient client;
        private boolean running = true;
        private ExecutionContext context;
        private AtomicLong counter = new AtomicLong();

        public TestRunner(SocketPerfTestingSyncClient client, ExecutionContext context) {
            this.client = client;
            this.context = context;
        }
        @Override
        public void run() {
            while (running) {
                client.noop(context);
                counter.incrementAndGet();
            }
        }

        public long stop() {
            running = false;
            return counter.get();
        }
    }
}
