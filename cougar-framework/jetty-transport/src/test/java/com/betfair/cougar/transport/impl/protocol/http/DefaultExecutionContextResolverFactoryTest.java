/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.transport.impl.protocol.http;


import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.apache.axiom.om.OMElement;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultExecutionContextResolverFactoryTest {

    @Test
    public void returnsSoapSpecific() {
        DefaultExecutionContextResolverFactory factory = new DefaultExecutionContextResolverFactory();
        DehydratedExecutionContextResolver<HttpCommand,OMElement>[] resolvers = factory.resolvers(Protocol.SOAP);
        assertTrue(resolvers[0] instanceof SoapIdentityTokenResolver);
    }

    @Test
    public void otherHttp() {
        DefaultExecutionContextResolverFactory factory = new DefaultExecutionContextResolverFactory();
        DehydratedExecutionContextResolver<HttpCommand,Void>[] resolvers = factory.resolvers(Protocol.RESCRIPT);
        assertTrue(resolvers[0] instanceof HttpIdentityTokenResolver);
    }

    @Test
    public void nonHttp() {
        DefaultExecutionContextResolverFactory factory = new DefaultExecutionContextResolverFactory();
        DehydratedExecutionContextResolver<?,?>[] resolvers = factory.resolvers(Protocol.SOCKET);
        assertNull(resolvers);
    }
}
