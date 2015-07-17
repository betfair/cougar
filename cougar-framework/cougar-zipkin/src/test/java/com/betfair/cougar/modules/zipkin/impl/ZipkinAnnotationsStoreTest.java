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
import com.twitter.zipkin.gen.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinAnnotationsStoreTest {

    @Mock
    private ZipkinData zipkinData;

    @Mock
    private Endpoint endpoint;

    private ZipkinAnnotationsStore victim;

    private String key = "key";

    @Before
    public void init() {
        initMocks(this);

        victim = new ZipkinAnnotationsStore(zipkinData);
    }

    @Test
    public void addAnnotation_OnStringOverload_ShouldAddBinaryAnnotation() {
        String value = "value";
        ByteBuffer wrappedValue = ByteBuffer.wrap(value.getBytes());

        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.STRING, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_OnShortOverload_ShouldAddBinaryAnnotation() {
        short value = 327;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Short.SIZE / 8).putShort(value);

        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.I16, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_OnIntOverload_ShouldAddBinaryAnnotation() {
        int value = 327;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Integer.SIZE / 8).putInt(value);

        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.I32, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_OnLongOverload_ShouldAddBinaryAnnotation() {
        long value = 327L;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Long.SIZE / 8).putLong(value);

        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.I64, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_OnDoubleOverload_ShouldAddBinaryAnnotation() {
        double value = 327D;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Double.SIZE / 8).putDouble(value);

        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.DOUBLE, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_OnBooleanOverload_ShouldAddBinaryAnnotation() {
        ByteBuffer wrappedValue = ByteBuffer.wrap(new byte[]{1});

        ZipkinAnnotationsStore result = victim.addAnnotation(key, true);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.BOOL, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_OnBytesOverload_ShouldAddBinaryAnnotation() {
        byte[] value = "327".getBytes();
        ByteBuffer wrappedValue = ByteBuffer.wrap(value);

        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.BYTES, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
    }

    @Test
    public void addAnnotation_ShouldAddRegularAnnotation() {
        String value = "value";
        long timestamp = System.currentTimeMillis();

        victim.defaultEndpoint(endpoint);
        ZipkinAnnotationsStore result = victim.addAnnotation(timestamp, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getAnnotationsSize());

        Annotation createdAnnotation = generatedSpan.getAnnotations().get(0);
        assertEquals(timestamp, createdAnnotation.getTimestamp());
        assertEquals(value, createdAnnotation.getValue());
        assertEquals(endpoint, createdAnnotation.getHost());
    }

    @Test
    public void addBinaryAnnotation_WhenAnEndpointIsDefined_ShouldSetEndpointAsHost() {
        short value = 327;
        ByteBuffer wrappedValue = ByteBuffer.allocate(Short.SIZE / 8).putShort(value);

        victim.defaultEndpoint(endpoint);
        ZipkinAnnotationsStore result = victim.addAnnotation(key, value);
        assertNotNull(result);

        Span generatedSpan = result.generate();
        assertEquals(1, generatedSpan.getBinary_annotationsSize());

        BinaryAnnotation createdBinaryAnnotation = generatedSpan.getBinary_annotations().get(0);
        assertEquals(key, createdBinaryAnnotation.getKey());
        assertEquals(AnnotationType.I16, createdBinaryAnnotation.getAnnotation_type());
        assertTrue(Arrays.equals(wrappedValue.array(), createdBinaryAnnotation.getValue()));
        assertEquals(endpoint, createdBinaryAnnotation.getHost());
    }
}
