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

package com.betfair.cougar.modules.zipkin.impl.socket;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.betfair.cougar.modules.zipkin.impl.ZipkinExecutionContextResolverFactory;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import com.betfair.cougar.netutil.nio.marshalling.SocketContextResolutionParams;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinSocketRequestUuidResolverTest {

    @Mock
    private ZipkinExecutionContextResolverFactory resolverFactory;

    @Mock
    private ZipkinManager zipkinManager;

    @Mock
    private SocketContextResolutionParams params;

    @Mock
    private ZipkinRequestUUID requestUUID;

    @Mock
    private DehydratedExecutionContextBuilder builder;

    private ZipkinSocketRequestUuidResolver victim;

    private int serverPort = 9003;

    @BeforeClass
    public static void setUp() {
        // Required to initialize generator so we don't cause NPEs on code we don't even want to test
        new RequestUUIDImpl(new UUIDGeneratorImpl());
    }

    @Before
    public void init() {
        initMocks(this);

        victim = new ZipkinSocketRequestUuidResolver(resolverFactory, zipkinManager, serverPort);
    }

    @Test
    public void resolve_WhenAdditionalDataIsNull_ShouldDeferHeaderInitialization() {

        when(params.getAdditionalData()).thenReturn(null);

        when(zipkinManager.createNewZipkinRequestUUID(any(RequestUUID.class), eq((String) null), eq((String) null),
                eq((String) null), eq((String) null), eq((String) null), eq(serverPort)))
                .thenReturn(requestUUID);

        victim.resolve(params, null, builder);

        verify(builder).setRequestUUID(requestUUID);
    }

    @Test
    public void resolve_WhenAdditionalDataExists_ShouldPropagateHeaders() {

        String traceId = "123456789";
        String spanId = "987654321";
        String parentSpanId = "543216789";
        String sampled = "1";
        String flags = "327";

        Map<String, String> additionalData = Maps.newHashMap();
        additionalData.put(ZipkinKeys.TRACE_ID, traceId);
        additionalData.put(ZipkinKeys.SPAN_ID, spanId);
        additionalData.put(ZipkinKeys.PARENT_SPAN_ID, parentSpanId);
        additionalData.put(ZipkinKeys.SAMPLED, sampled);
        additionalData.put(ZipkinKeys.FLAGS, flags);

        when(params.getAdditionalData()).thenReturn(additionalData);

        when(zipkinManager.createNewZipkinRequestUUID(any(RequestUUID.class), eq(traceId), eq(spanId),
                eq(parentSpanId), eq(sampled), eq(flags), eq(serverPort)))
                .thenReturn(requestUUID);

        victim.resolve(params, null, builder);

        verify(builder).setRequestUUID(requestUUID);
    }
}
