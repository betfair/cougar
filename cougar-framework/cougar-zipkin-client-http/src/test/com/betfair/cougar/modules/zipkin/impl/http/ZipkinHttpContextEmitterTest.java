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

import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinHttpContextEmitterTest {

    @Mock
    private CompoundContextEmitter compoundContextEmitter;

    @Mock
    private ClientCallContext ctx;

    @Mock
    private ZipkinRequestUUID zipkinRequestUUID;

    @Mock
    private ZipkinData zipkinData;

    private long traceId = 123456789L;
    private long spanId = 987654321L;
    private long parentSpanId = 567891234L;
    private long flags = 1L;

    private ZipkinHttpContextEmitter victim;

    @Before
    public void init() {
        initMocks(this);

        when(ctx.getRequestUUID()).thenReturn(zipkinRequestUUID);
    }

    @Test
    public void ZipkinHttpContextEmitter_OnCreation_ShouldRegisterItselfWithCompoundContextEmitter() {
        victim = new ZipkinHttpContextEmitter(compoundContextEmitter);

        verify(compoundContextEmitter).addEmitter(victim);
    }

    @Test
    public void emit_WhenZipkinTracingIsNotEnabled_ShouldDisableSamplingForTheEntireRequestChain() {
        List<Header> result = Lists.newArrayList();
        Header expectedHeader = new BasicHeader(ZipkinKeys.SAMPLED, ZipkinKeys.DO_NOT_SAMPLE_VALUE);

        victim = new ZipkinHttpContextEmitter(compoundContextEmitter);

        when(ctx.traceLoggingEnabled()).thenReturn(false);
        when(zipkinRequestUUID.isZipkinTracingEnabled()).thenReturn(false);

        victim.emit(ctx, null, result);

        assertEquals(1, result.size());
        assertEquals(expectedHeader.toString(), result.get(0).toString());
    }

    @Test
    public void emit_WhenZipkinTracingIsEnabledOnNonFlaggedOriginalRequest_ShouldAppendZipkinHeaders() {
        List<Header> result = Lists.newArrayList();
        List<Header> expectedZipkinHeaders = createZipkinHeaders(traceId, spanId, null, null);

        victim = new ZipkinHttpContextEmitter(compoundContextEmitter);

        when(ctx.traceLoggingEnabled()).thenReturn(false);
        when(zipkinRequestUUID.isZipkinTracingEnabled()).thenReturn(true);
        when(zipkinRequestUUID.getZipkinData()).thenReturn(zipkinData);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getParentSpanId()).thenReturn(null);
        when(zipkinData.getFlags()).thenReturn(null);

        victim.emit(ctx, null, result);

        assertEquals(3, result.size());
        assertEquals(expectedZipkinHeaders.get(0).toString(), result.get(0).toString());
        assertEquals(expectedZipkinHeaders.get(1).toString(), result.get(1).toString());
        assertEquals(expectedZipkinHeaders.get(2).toString(), result.get(2).toString());
    }

    @Test
    public void emit_WhenZipkinTracingIsEnabledOnChildRequest_ShouldAppendZipkinHeaders() {
        List<Header> result = Lists.newArrayList();
        List<Header> expectedZipkinHeaders = createZipkinHeaders(traceId, spanId, parentSpanId, flags);

        victim = new ZipkinHttpContextEmitter(compoundContextEmitter);

        when(ctx.traceLoggingEnabled()).thenReturn(false);
        when(zipkinRequestUUID.isZipkinTracingEnabled()).thenReturn(true);
        when(zipkinRequestUUID.getZipkinData()).thenReturn(zipkinData);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getParentSpanId()).thenReturn(parentSpanId);
        when(zipkinData.getFlags()).thenReturn(flags);

        victim.emit(ctx, null, result);

        assertEquals(5, result.size());
        assertEquals(expectedZipkinHeaders.get(0).toString(), result.get(0).toString());
        assertEquals(expectedZipkinHeaders.get(1).toString(), result.get(1).toString());
        assertEquals(expectedZipkinHeaders.get(2).toString(), result.get(2).toString());
        assertEquals(expectedZipkinHeaders.get(3).toString(), result.get(3).toString());
        assertEquals(expectedZipkinHeaders.get(4).toString(), result.get(4).toString());
    }

    private List<Header> createZipkinHeaders(long traceId, long spanId, Long parentSpanId, Long flags) {
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader(ZipkinKeys.SAMPLED, ZipkinKeys.DO_SAMPLE_VALUE));
        headers.add(new BasicHeader(ZipkinKeys.TRACE_ID, Long.toHexString(traceId)));
        headers.add(new BasicHeader(ZipkinKeys.SPAN_ID, Long.toHexString(spanId)));

        if (parentSpanId != null) {
            headers.add(new BasicHeader(ZipkinKeys.PARENT_SPAN_ID, Long.toHexString(parentSpanId)));
        }

        if (flags != null) {
            headers.add(new BasicHeader(ZipkinKeys.FLAGS, Long.toHexString(flags)));
        }

        return headers;
    }
}
