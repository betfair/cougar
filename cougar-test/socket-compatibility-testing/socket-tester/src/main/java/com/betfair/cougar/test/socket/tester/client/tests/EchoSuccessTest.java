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
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.WaitingObserver;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.test.socket.tester.client.*;
import com.betfair.cougar.test.socket.tester.common.ClientAuthRequirement;
import com.betfair.cougar.test.socket.tester.common.Common;
import com.betfair.cougar.test.socket.tester.common.EchoResponse;
import com.betfair.cougar.test.socket.tester.common.SslRequirement;

/**
*
*/
public class EchoSuccessTest implements ClientTest {
    private final ServerConfiguration server;
    private final SslRequirement sslRequirement;
    private final ClientAuthRequirement clientAuthRequirement;
    private final boolean expectSuccess;

    public EchoSuccessTest(ServerConfiguration server) {
        this(server,SslRequirement.None,true);
    }

    public EchoSuccessTest(ServerConfiguration server, boolean expectSuccess) {
        this(server,SslRequirement.None,expectSuccess);
    }

    public EchoSuccessTest(ServerConfiguration server, SslRequirement sslRequirement) {
        this(server,sslRequirement,true);
    }

    public EchoSuccessTest(ServerConfiguration server, SslRequirement sslRequirement, ClientAuthRequirement clientAuthRequirement) {
        this(server,sslRequirement, clientAuthRequirement,true);
    }

    public EchoSuccessTest(ServerConfiguration server, SslRequirement sslRequirement, boolean expectSuccess) {
        this(server,sslRequirement, ClientAuthRequirement.None,expectSuccess);
    }

    public EchoSuccessTest(ServerConfiguration server, SslRequirement sslRequirement, ClientAuthRequirement clientAuthRequirement, boolean expectSuccess) {
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

            WaitingObserver observer = clientInstance.execute(ctx, Common.echoOperationDefinition,new Object[] { "Hello world!" });
            if (!observer.await(10000)) {
                ret.setError("Didn't get a response in time");
                return;
            }

            if (observer.getExecutionResult().getResultType() == ExecutionResult.ResultType.Fault) {
                CougarException e = observer.getExecutionResult().getFault();
                if (expectSuccess) {
                    ret.setError(e);
                }
                else {
                    ret.setOutput("Expected exception received: "+e.getMessage());
                }
            }
            else {
                EchoResponse response = (EchoResponse) observer.getExecutionResult().getResult();
                if (expectSuccess) {
                    ret.setOutput("Result: " + response.getMessage() + "\n" + response.getExecutionContext());
                }
                else {
                    ret.setError("Unexpected success: " + response.getMessage() + "\n" + response.getExecutionContext());
                }
            }
        }
        finally {
            clientInstance.shutdown();
        }
    }

    @Override
    public String getName() {
        StringBuilder ret = new StringBuilder("Echo success");
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
