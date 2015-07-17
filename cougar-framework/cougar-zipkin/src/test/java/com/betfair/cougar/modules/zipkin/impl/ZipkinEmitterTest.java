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

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.util.time.Clock;
import com.github.kristofa.brave.zipkin.ZipkinSpanCollector;
import com.google.common.collect.Lists;
import com.twitter.zipkin.gen.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.nio.ByteBuffer;
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

    private String key = "key";

    @Before
    public void init() {
        initMocks(this);

        // ZipkinData
        when(zipkinData.getSpanName()).thenReturn(spanName);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getPort()).thenReturn(port);

        when(zipkinClock.millis()).thenReturn(timestampMillis);

        victim = new ZipkinEmitter(serviceName, zipkinSpanCollector, zipkinClock, serviceIPv4);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinEmitter_WhenServiceNameIsNull_ShouldThrowNPE() {

        new ZipkinEmitter(null, zipkinSpanCollector, zipkinClock, serviceIPv4);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinEmitter_WhenZipkinSpanCollectorIsNull_ShouldThrowNPE() {

        new ZipkinEmitter(serviceName, null, zipkinClock, serviceIPv4);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinEmitter_WhenZipkinClockIsNull_ShouldThrowNPE() {

        new ZipkinEmitter(serviceName, zipkinSpanCollector, null, serviceIPv4);
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

    @Test
    public void emitAnnotation_OnStringOverload_ShouldEmitAnnotation() {

        String value = "value";
        ByteBuffer wrappedValue = ByteBuffer.wrap(value.getBytes());

        BinaryAnnotation binaryAnnotation =
                new BinaryAnnotation(key, wrappedValue, AnnotationType.STRING);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, value);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitAnnotation_OnShortOverload_ShouldEmitAnnotation() {

        short value = 327;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Short.SIZE / 8).putShort(value);
        wrappedValue.flip();

        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, wrappedValue, AnnotationType.I16);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, value);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitAnnotation_OnIntOverload_ShouldEmitAnnotation() {

        int value = 327;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Integer.SIZE / 8).putInt(value);
        wrappedValue.flip();

        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, wrappedValue, AnnotationType.I32);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, value);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitAnnotation_OnLongOverload_ShouldEmitAnnotation() {

        long value = 327L;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Long.SIZE / 8).putLong(value);
        wrappedValue.flip();

        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, wrappedValue, AnnotationType.I64);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, value);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitAnnotation_OnDoubleOverload_ShouldEmitAnnotation() {

        double value = 327D;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Double.SIZE / 8).putDouble(value);
        wrappedValue.flip();

        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, wrappedValue, AnnotationType.DOUBLE);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, value);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitAnnotation_OnBooleanOverload_ShouldEmitAnnotation() {

        ByteBuffer wrappedValue = ByteBuffer.wrap(new byte[]{1});

        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, wrappedValue, AnnotationType.BOOL);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, true);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }

    @Test
    public void emitAnnotation_OnBytesOverload_ShouldEmitAnnotation() {

        byte[] value = "327".getBytes();
        ByteBuffer wrappedValue = ByteBuffer.wrap(value);

        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, wrappedValue, AnnotationType.BYTES);
        binaryAnnotation.setHost(endpoint);
        List<Annotation> annotations = Collections.emptyList();

        Span expectedSpan = new Span(traceId, spanName, spanId, annotations, Lists.newArrayList(binaryAnnotation));
        expectedSpan.setParent_id(0);

        victim.emitAnnotation(zipkinData, key, value);

        verify(zipkinSpanCollector).collect(expectedSpan);
    }
}
