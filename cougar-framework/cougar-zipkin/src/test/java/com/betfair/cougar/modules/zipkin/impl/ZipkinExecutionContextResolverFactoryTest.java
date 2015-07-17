/*
 * Copyright 2015, The Sporting Exchange Limited
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

package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.modules.zipkin.impl.http.ZipkinHttpRequestUuidResolver;
import com.betfair.cougar.modules.zipkin.impl.socket.ZipkinSocketRequestUuidResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class ZipkinExecutionContextResolverFactoryTest {

    private ZipkinExecutionContextResolverFactory victim;

    @Mock
    private ZipkinManager zipkinManager;

    private String cougarUUIDHeader = "X-UUID";
    private String uuidParentsHeader = "X-UUID-Parents";
    private int socketServerPort = 9003;

    @Before
    public void init() {
        victim = new ZipkinExecutionContextResolverFactory();
        victim.setCougarUUIDHeader(cougarUUIDHeader);
        victim.setUuidParentsHeader(uuidParentsHeader);
        victim.setZipkinManager(zipkinManager);
        victim.setSocketServerPort(socketServerPort);
    }

    @Test
    public void testGetName() {
        assertEquals("Zipkin ContextResolverFactory", victim.getName());
    }

    @Test
    public void resolvers_WhenUnderlyingTransportIsHttp_ShouldReturnZipkinHttpRequestUuidResolver() {
        Protocol protocol = Protocol.RESCRIPT;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0].getClass().equals(ZipkinHttpRequestUuidResolver.class));
    }

    @Test
    public void resolvers_WhenProtocolIsSocket_ShouldReturnZipkinSocketRequestUuidResolver() {
        Protocol protocol = Protocol.SOCKET;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0].getClass().equals(ZipkinSocketRequestUuidResolver.class));
    }

    @Test
    public void resolvers_WhenProtocolIsNotHttpNorSocketBased_ShouldReturnNull() {
        Protocol protocol = Protocol.IN_PROCESS;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNull(result);
    }
}
