package com.betfair.cougar.modules.zipkin.impl.socket;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.HttpContextEmitter;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.AbstractMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinSocketContextEmitterTest {

    @Mock
    private GeoLocationSerializer geoLocationSerializer;

    @Mock
    private CompoundContextEmitter compoundContextEmitter;

    @Mock
    private ClientCallContext ctx;

    @Mock
    private HttpContextEmitter request;

    @Mock
    private RequestUUID requestUUID;

    @Mock
    private ZipkinRequestUUID zipkinRequestUUID;

    @Mock
    private ZipkinData zipkinData;

    @Mock
    private GeoLocationDetails geoLocationDetails;

    private String uuidHeader = "X-UUID";
    private String uuidParentsHeader = "X-UUID-Parents";
    private long traceId = 123456789L;
    private long spanId = 987654321L;
    private long parentSpanId = 567891234L;
    private long flags = 1L;

    private ZipkinSocketContextEmitter victim;

    @Before
    public void init() {
        initMocks(this);

        when(ctx.getRequestUUID()).thenReturn(zipkinRequestUUID);
    }

    @Test
    public void ZipkinSocketContextEmitter_OnCreation_ShouldRegisterItselfWithCompoundContextEmitter() {
        victim = new ZipkinSocketContextEmitter(geoLocationSerializer, uuidHeader, uuidParentsHeader,
                compoundContextEmitter);

        verify(compoundContextEmitter).addEmitter(victim);
    }

    @Test
    public void emit_WhenZipkinTracingIsNotEnabled_ShouldDisableSamplingForTheEntireRequestChain() {
        Map<String, String> additionalData = Maps.newHashMap();
        Map.Entry<String, String> expectedHeader = new AbstractMap.SimpleEntry<>(ZipkinKeys.SAMPLED, ZipkinKeys.DO_NOT_SAMPLE_VALUE);

        victim = new ZipkinSocketContextEmitter(geoLocationSerializer, null, null, compoundContextEmitter);

        when(ctx.traceLoggingEnabled()).thenReturn(false);
        when(zipkinRequestUUID.isZipkinTracingEnabled()).thenReturn(false);

        victim.emit(ctx, additionalData, null);

        assertEquals(2, additionalData.size());
        assertTrue(additionalData.containsKey(expectedHeader.getKey()));
        assertEquals(additionalData.get(expectedHeader.getKey()), expectedHeader.getValue());
    }

    @Test
    public void emit_WhenZipkinTracingIsEnabledOnNonFlaggedOriginalRequest_ShouldAppendZipkinHeaders() {
        Map<String, String> additionalData = Maps.newHashMap();
        Map<String, String> expectedZipkinHeaders = createZipkinHeaders(traceId, spanId, null, null);

        victim = new ZipkinSocketContextEmitter(geoLocationSerializer, null, null, compoundContextEmitter);

        when(ctx.traceLoggingEnabled()).thenReturn(false);
        when(zipkinRequestUUID.isZipkinTracingEnabled()).thenReturn(true);
        when(zipkinRequestUUID.getZipkinData()).thenReturn(zipkinData);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getParentSpanId()).thenReturn(null);
        when(zipkinData.getFlags()).thenReturn(null);

        victim.emit(ctx, additionalData, null);

        assertEquals(4, additionalData.size());
        assertTrue(additionalData.entrySet().containsAll(expectedZipkinHeaders.entrySet()));
    }

    @Test
    public void emit_WhenZipkinTracingIsEnabledOnChildRequest_ShouldAppendZipkinHeaders() {
        Map<String, String> additionalData = Maps.newHashMap();
        Map<String, String> expectedZipkinHeaders = createZipkinHeaders(traceId, spanId, parentSpanId, flags);

        victim = new ZipkinSocketContextEmitter(geoLocationSerializer, null, null, compoundContextEmitter);

        when(ctx.traceLoggingEnabled()).thenReturn(false);
        when(zipkinRequestUUID.isZipkinTracingEnabled()).thenReturn(true);
        when(zipkinRequestUUID.getZipkinData()).thenReturn(zipkinData);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getParentSpanId()).thenReturn(parentSpanId);
        when(zipkinData.getFlags()).thenReturn(flags);

        victim.emit(ctx, additionalData, null);

        assertEquals(6, additionalData.size());
        assertTrue(additionalData.entrySet().containsAll(expectedZipkinHeaders.entrySet()));
    }

    private Map<String, String> createZipkinHeaders(long traceId, long spanId, Long parentSpanId, Long flags) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(ZipkinKeys.SAMPLED, ZipkinKeys.DO_SAMPLE_VALUE);
        headers.put(ZipkinKeys.TRACE_ID, Long.toHexString(traceId));
        headers.put(ZipkinKeys.SPAN_ID, Long.toHexString(spanId));

        if (parentSpanId != null) {
            headers.put(ZipkinKeys.PARENT_SPAN_ID, Long.toHexString(parentSpanId));
        }

        if (flags != null) {
            headers.put(ZipkinKeys.FLAGS, Long.toHexString(flags));
        }

        return headers;
    }
}
