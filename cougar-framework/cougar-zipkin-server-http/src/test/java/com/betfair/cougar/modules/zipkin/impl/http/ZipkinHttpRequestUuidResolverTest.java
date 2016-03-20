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

package com.betfair.cougar.modules.zipkin.impl.http;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.betfair.cougar.modules.zipkin.impl.ZipkinExecutionContextResolverFactory;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinHttpRequestUuidResolverTest {

    private String uuidHeader = "X-UUID";
    private String uuidParentsHeader = "X-UUID-Parents";

    @Mock
    private ZipkinExecutionContextResolverFactory resolverFactory;

    @Mock
    private ZipkinManager zipkinManager;

    @Mock
    private HttpCommand httpCommand;

    @Mock
    private DehydratedExecutionContextBuilder contextBuilder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ZipkinRequestUUID requestUUID;

    private ZipkinHttpRequestUuidResolver victim;

    @BeforeClass
    public static void setUp() {
        // Required to initialize generator so we don't cause NPEs on code we don't even want to test
        new RequestUUIDImpl(new UUIDGeneratorImpl());
    }

    @Before
    public void init() {
        initMocks(this);
    }

    @Test
    public void resolve_ShouldPopulateZipkinUUID() {

        String traceId = "123456789";
        String spanId = "987654321";
        String parentSpanId = "543216789";
        String sampled = "1";
        String flags = "327";
        int port = 9101;

        String uuidString = "abcde-1234-fghij-5678-klmno";
        String uuidParentsString = "";

        when(httpCommand.getRequest()).thenReturn(request);
        when(request.getHeader(ZipkinKeys.TRACE_ID)).thenReturn(traceId);
        when(request.getHeader(ZipkinKeys.SPAN_ID)).thenReturn(spanId);
        when(request.getHeader(ZipkinKeys.PARENT_SPAN_ID)).thenReturn(parentSpanId);
        when(request.getHeader(ZipkinKeys.SAMPLED)).thenReturn(sampled);
        when(request.getHeader(ZipkinKeys.FLAGS)).thenReturn(flags);
        when(request.getLocalPort()).thenReturn(port);
        when(request.getHeader(uuidHeader)).thenReturn(uuidString);
        when(request.getHeader(uuidParentsHeader)).thenReturn(uuidParentsString);

        victim = new ZipkinHttpRequestUuidResolver(resolverFactory, zipkinManager, uuidHeader, uuidParentsHeader);

        when(httpCommand.getRequest()).thenReturn(request);
        when(zipkinManager.createNewZipkinRequestUUID(any(RequestUUID.class), eq(traceId), eq(spanId), eq(parentSpanId),
                eq(sampled), eq(flags), eq(port))).thenReturn(requestUUID);

        victim.resolve(httpCommand, null, contextBuilder);

        verify(contextBuilder).setRequestUUID(requestUUID);
    }
}
