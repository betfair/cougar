package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.github.kristofa.brave.zipkin.ZipkinSpanCollector;
import com.google.common.collect.Lists;
import com.twitter.zipkin.gen.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinEmitterTest {

    @Mock
    private ZipkinSpanCollector zipkinSpanCollector;

    @Mock
    private ZipkinData zipkinData;

    @Mock
    private Clock zipkinClock;

    private ZipkinEmitter victim;

    private int serviceIPv4 = 2130706433;
    private String serviceName = "Service Name";

    // ZipkinData mock stuff
    private String spanName = "Span Name";
    private long traceId = 123456789;
    private long spanId = 987654321;
    private short port = 9101;
    private Endpoint endpoint = new Endpoint(serviceIPv4, port, serviceName);
    private long timestampMillis = 327;
    private long timestampMicros = TimeUnit.MILLISECONDS.toMicros(timestampMillis);

    @Before
    public void init() {
        initMocks(this);

        // ZipkinData
        when(zipkinData.getSpanName()).thenReturn(spanName);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getPort()).thenReturn(port);

        when(zipkinClock.millis()).thenReturn(timestampMillis);

        victim = new ZipkinEmitter(serviceIPv4, serviceName, zipkinSpanCollector, zipkinClock);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinEmitter_WhenServiceNameIsNull_ShouldThrowNPE() {

        new ZipkinEmitter(serviceIPv4, null, zipkinSpanCollector, zipkinClock);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinEmitter_WhenZipkinSpanCollectorIsNull_ShouldThrowNPE() {

        new ZipkinEmitter(serviceIPv4, serviceName, null, zipkinClock);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinEmitter_WhenZipkinClockIsNull_ShouldThrowNPE() {

        new ZipkinEmitter(serviceIPv4, serviceName, zipkinSpanCollector, null);
    }

    @Test
    public void emitServerReceive_ShouldEmitServerRecvAnnotation() {

        Annotation annotation = new Annotation(timestampMicros, zipkinCoreConstants.SERVER_RECV);
        annotation.setHost(endpoint);
        List<BinaryAnnotation> binaryAnnotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, Lists.newArrayList(annotation), binaryAnnotations);
        expectedSpan.setParent_id(0);

        victim.emitServerReceive(zipkinData);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitServerSend_ShouldEmitServerSendAnnotation() {

        Annotation annotation = new Annotation(timestampMicros, zipkinCoreConstants.SERVER_SEND);
        annotation.setHost(endpoint);
        List<BinaryAnnotation> binaryAnnotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, Lists.newArrayList(annotation), binaryAnnotations);
        expectedSpan.setParent_id(0);

        victim.emitServerSend(zipkinData);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitClientSend_ShouldEmitClientSendAnnotation() {

        Annotation annotation = new Annotation(timestampMicros, zipkinCoreConstants.CLIENT_SEND);
        annotation.setHost(endpoint);
        List<BinaryAnnotation> binaryAnnotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, Lists.newArrayList(annotation), binaryAnnotations);
        expectedSpan.setParent_id(0);

        victim.emitClientSend(zipkinData);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitClientReceive_ShouldEmitClientRecvAnnotation() {

        Annotation annotation = new Annotation(timestampMicros, zipkinCoreConstants.CLIENT_RECV);
        annotation.setHost(endpoint);
        List<BinaryAnnotation> binaryAnnotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, Lists.newArrayList(annotation), binaryAnnotations);
        expectedSpan.setParent_id(0);

        victim.emitClientReceive(zipkinData);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

//    @Test
//    public void emitAnnotation_OnStringOverload_ShouldEmitAnnotation() {
//
//        Annotation annotation = new Annotation(timestampMicros, zipkinCoreConstants.CLIENT_RECV);
//        annotation.setHost(endpoint);
//        List<BinaryAnnotation> binaryAnnotations = Collections.emptyList();
//
//        Span expectedSpan = new Span(traceId, spanName, spanId, Lists.newArrayList(annotation), binaryAnnotations);
//        expectedSpan.setParent_id(0);
//
//        victim.emitClientReceive(zipkinData);
//
//        verify(zipkinSpanCollector).collect(expectedSpan);
//    }
}
