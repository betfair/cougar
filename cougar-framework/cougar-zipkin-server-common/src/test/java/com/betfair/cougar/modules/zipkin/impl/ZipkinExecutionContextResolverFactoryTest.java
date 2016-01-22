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
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinExecutionContextResolverFactoryTest {

    @InjectMocks
    private ZipkinExecutionContextResolverFactory victim;

    @Mock
    private ZipkinManager zipkinManager;

    @Mock
    private DehydratedExecutionContextResolver httpRequestResolver;

    @Mock
    private DehydratedExecutionContextResolver socketRequestResolver;

    @Before
    public void init() {
        victim = new ZipkinExecutionContextResolverFactory();
        initMocks(this);
    }

    @Test
    public void testGetName() {
        assertEquals("Zipkin ContextResolverFactory", victim.getName());
    }

    @Test
    public void resolvers_WhenUnderlyingTransportIsHttp_ShouldReturnZipkinHttpRequestUuidResolver() {
        Protocol protocol = Protocol.RESCRIPT;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertArrayEquals(new DehydratedExecutionContextResolver[]{httpRequestResolver}, result);
    }

    @Test
    public void resolvers_WhenProtocolIsSocket_ShouldReturnZipkinSocketRequestUuidResolver() {
        Protocol protocol = Protocol.SOCKET;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertArrayEquals(new DehydratedExecutionContextResolver[]{socketRequestResolver}, result);
    }

    @Test
    public void resolvers_WhenProtocolIsNotHttpNorSocketBased_ShouldReturnNull() {
        Protocol protocol = Protocol.IN_PROCESS;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNull(result);
    }
}
