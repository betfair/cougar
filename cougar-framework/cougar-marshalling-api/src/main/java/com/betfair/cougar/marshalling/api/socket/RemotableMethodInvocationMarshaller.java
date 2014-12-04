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

package com.betfair.cougar.marshalling.api.socket;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.api.protocol.socket.InvocationRequest;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;

import java.io.IOException;
import java.util.Map;

/**
 * This interface describes a component for serialising and deserialising binary
 * transport request/responses
 */
public interface RemotableMethodInvocationMarshaller {

    public void writeInvocationRequest(InvocationRequest request, CougarObjectOutput out, IdentityResolver identityResolver, Map<String,String> additionalData, byte protocolVersion) throws IOException;

    public void writeInvocationResponse(InvocationResponse response, CougarObjectOutput out, byte protocolVersion) throws IOException;

    public InvocationResponse readInvocationResponse(ParameterType resultType, CougarObjectInput in) throws IOException;

    public OperationKey readOperationKey(CougarObjectInput in) throws IOException;

    public Object [] readArgs(Parameter[] argTypes, CougarObjectInput in) throws IOException;

    public DehydratedExecutionContext readExecutionContext(CougarObjectInput in, String remoteAddress, java.security.cert.X509Certificate[] clientCertChain, int transportSecurityStrengthFactor, byte protocolVersion) throws IOException;

    TimeConstraints readTimeConstraintsIfPresent(CougarObjectInput in, byte protocolVersion) throws IOException;
}
