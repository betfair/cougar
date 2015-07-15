/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.client.tests;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.ev.WaitingObserver;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.test.socket.tester.client.*;
import com.betfair.cougar.test.socket.tester.common.ClientAuthRequirement;
import com.betfair.cougar.test.socket.tester.common.Common;
import com.betfair.cougar.test.socket.tester.common.EchoResponse;
import com.betfair.cougar.test.socket.tester.common.SslRequirement;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.MapNode;
import com.betfair.platform.virtualheap.ScalarNode;

/**
 *
 */
public class HeapTest implements ClientTest {
    private final ServerConfiguration server;
    private final SslRequirement sslRequirement;
    private final ClientAuthRequirement clientAuthRequirement;
    private final boolean expectSuccess;

    public HeapTest(ServerConfiguration server) {
        this(server,SslRequirement.None,true);
    }

    public HeapTest(ServerConfiguration server, boolean expectSuccess) {
        this(server,SslRequirement.None,expectSuccess);
    }

    public HeapTest(ServerConfiguration server, SslRequirement sslRequirement) {
        this(server,sslRequirement,true);
    }

    public HeapTest(ServerConfiguration server, SslRequirement sslRequirement, ClientAuthRequirement clientAuthRequirement) {
        this(server,sslRequirement, clientAuthRequirement,true);
    }

    public HeapTest(ServerConfiguration server, SslRequirement sslRequirement, boolean expectSuccess) {
        this(server,sslRequirement, ClientAuthRequirement.None,expectSuccess);
    }

    public HeapTest(ServerConfiguration server, SslRequirement sslRequirement, ClientAuthRequirement clientAuthRequirement, boolean expectSuccess) {
        this.server = server;
        this.sslRequirement = sslRequirement;
        this.clientAuthRequirement = clientAuthRequirement;
        this.expectSuccess = expectSuccess;
    }

    @Override
    public void test(TestResult ret) throws Exception {
        if (server.getMinProtocolVersion() > CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED) {
            ret.setOutput("My protocol version too low: "+CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED+" < "+server.getMinProtocolVersion());
            return;
        }

        ClientInstance clientInstance = new ClientInstance("ALL", server.getPort(), sslRequirement, clientAuthRequirement);
        try {
            ExecutionContext ctx = ClientMain.createExecutionContext();

            String clientId = String.valueOf(System.nanoTime()); // unique enough for now

            WaitingObserver observer = clientInstance.execute(ctx, Common.heapSubscribeOperationDefinition, new Object[] { clientId });
            if (!observer.await(10000)) {
                ret.setError("Didn't get a response in time");
                return;
            }

            Heap heap;
            Subscription sub;
            if (observer.getExecutionResult().getResultType() == ExecutionResult.ResultType.Fault) {
                CougarException e = observer.getExecutionResult().getFault();
                if (expectSuccess) {
                    ret.setError(e);
                }
                else {
                    ret.setOutput("Expected exception received: "+e.getMessage());
                }
                return;
            }
            else {
                ConnectedResponse response = (ConnectedResponse) observer.getExecutionResult().getResult();
                if (!expectSuccess) {
                    ret.setError("Unexpected success: " + response.getSubscription());
                    return;
                }

                heap = response.getHeap();
                sub = response.getSubscription();
            }

            // right, now we need to trigger changes..
            observer = clientInstance.execute(ctx, Common.heapSetOperationDefinition, new Object[] { clientId, "Hello world!" });
            if (!observer.await(10000)) {
                ret.setError("Didn't get a response in time");
                return;
            }

            if (observer.getExecutionResult().getResultType() == ExecutionResult.ResultType.Fault) {
                CougarException e = observer.getExecutionResult().getFault();
                ret.setError(e);
                return;
            }

            // void operation, so not expecting a result, but we need to make sure we get a change
            long expiry = System.currentTimeMillis() + 10000;
            while (!heap.isRootInstalled() && System.currentTimeMillis() < expiry) {
                Thread.sleep(100);
            }
            // make sure the update is done
            heap.beginUpdate();
            heap.endUpdate();

            if (!heap.isRootInstalled()) {
                ret.setError("Didn't receive heap update within 10 seconds");
                return;
            }

            MapNode mapNode = (MapNode) heap.getRoot();
            ScalarNode scalarNode = (ScalarNode) mapNode.getField("message");

            if (!scalarNode.get().equals("Hello world!")) {
                ret.setError("Unexpected message in heap: '"+scalarNode.get()+"'");
                return;
            }


            observer = clientInstance.execute(ctx, Common.heapCloseOperationDefinition, new Object[] { clientId });
            if (!observer.await(10000)) {
                ret.setError("Didn't get a response in time");
                return;
            }

            if (observer.getExecutionResult().getResultType() == ExecutionResult.ResultType.Fault) {
                CougarException e = observer.getExecutionResult().getFault();
                ret.setError(e);
                return;
            }

            // void operation, so not expecting a result, but we need to make sure we get a change
            expiry = System.currentTimeMillis() + 10000;
            while (!heap.isTerminated() && System.currentTimeMillis() < expiry) {
                Thread.sleep(100);
            }
            // make sure the update is done
            heap.beginUpdate();
            heap.endUpdate();

            if (!heap.isTerminated()) {
                ret.setError("Didn't receive heap termination within 10 seconds");
                return;
            }

            sub.close();
        }
        finally {
            clientInstance.shutdown();
        }
    }

    @Override
    public String getName() {
        StringBuilder ret = new StringBuilder("Heap / CO");
        if (sslRequirement != SslRequirement.None) {
            ret.append(" (").append(sslRequirement.toString().toLowerCase()).append(" SSL");
            if (clientAuthRequirement != ClientAuthRequirement.None) {
                ret.append(" with client auth");
            }
            ret.append(")");
        }
        return ret.toString();
    }

    public String getServerVariant() {
        return server.getVariant();
    }
}
