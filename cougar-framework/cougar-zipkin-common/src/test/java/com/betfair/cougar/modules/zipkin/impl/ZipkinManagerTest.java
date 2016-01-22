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

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinManagerTest {

    @Mock
    private ThreadLocalRandom threadLocalRandom;

    @Mock
    private SecureRandom secureRandom;

    @Mock
    private ThreadLocal<SecureRandom> secureRandomTl;

    @Mock
    private RequestUUID cougarUuid;

    private ZipkinManager victim = new ZipkinManager();

    private String traceId = "123456789";
    private String spanId = "987654321";
    private String parentSpanId = "432156789";
    private String sampled = "1";
    private String flags = "1";
    private int port = 9101;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        initMocks(this);

        Field randomField = ZipkinManager.class.getDeclaredField("RANDOM");
        randomField.setAccessible(true);
        randomField.set(ZipkinManager.class, threadLocalRandom);
    }

    @Test
    public void shouldTrace_WhenSamplingLevelIsZero_ShouldReturnFalse() {

        victim.setSamplingLevel(0);

        assertFalse(victim.shouldTrace());
    }

    @Test
    public void shouldTrace_WhenRandomNextIntIsUnderTheSamplingLevel_ShouldReturnTrue() {

        victim.setSamplingLevel(500);

        when(threadLocalRandom.nextInt(0, 1000)).thenReturn(499);

        assertTrue(victim.shouldTrace());
    }

    @Test
    public void shouldTrace_WhenRandomNextIntIsOverTheSamplingLevel_ShouldReturnFalse() {

        victim.setSamplingLevel(500);

        when(threadLocalRandom.nextInt(0, 1000)).thenReturn(501);

        assertFalse(victim.shouldTrace());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSamplingLevel_WhenLevelIsBelowLowerBound_ShouldThrowIAE() {
        victim.setSamplingLevel(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSamplingLevel_WhenLevelIsOverUpperBound_ShouldThrowIAE() {
        victim.setSamplingLevel(1001);
    }

    @Test
    public void setSamplingLevel_WhenLevelBetweenBounds_ShouldSetSamplingLevel() {
        int samplingLevel = 327;

        victim.setSamplingLevel(samplingLevel);

        assertEquals(samplingLevel, victim.getSamplingLevel());
    }

    @Test
    public void hexUnsignedStringToLong_Test1() {

        long result = ZipkinManager.hexUnsignedStringToLong("f");

        assertEquals(15, result);
    }

    @Test
    public void hexUnsignedStringToLong_Test2() {

        long result = ZipkinManager.hexUnsignedStringToLong("147");

        assertEquals(327, result);
    }

    @Test
    public void hexUnsignedStringToLong_Test3() {

        long result = ZipkinManager.hexUnsignedStringToLong("7fffffffffffffff");

        assertEquals(Long.MAX_VALUE, result);
    }

    @Test(expected = NullPointerException.class)
    public void createNewZipkinRequestUUID_WhenCougarUUIDIsNull_ShouldThrowNPE() {
        victim.createNewZipkinRequestUUID(null, traceId, spanId, parentSpanId, sampled, flags, port);
    }

    @Test
    public void createNewZipkinRequestUUID_WhenRequestIsMarkedAsNotSampled_ShouldNotFillZipkinData() {
        String notSampledHeader = "0";

        ZipkinRequestUUID result = victim.createNewZipkinRequestUUID(cougarUuid, traceId, spanId, parentSpanId, notSampledHeader, flags, port);

        assertNotNull(result);
        assertFalse(result.isZipkinTracingEnabled());
    }

    @Test
    public void createNewZipkinRequestUUID_WhenRequestIsAlreadyBeingTraced_ShouldContinueTracing() {

        ZipkinRequestUUID result = victim.createNewZipkinRequestUUID(cougarUuid, traceId, spanId, parentSpanId, sampled, flags, port);
        ZipkinData resultingData = result.buildZipkinData("");

        assertNotNull(result);
        assertTrue(result.isZipkinTracingEnabled());
        assertEquals(new BigInteger(traceId, 16).longValue(), resultingData.getTraceId());
        assertEquals(new BigInteger(spanId, 16).longValue(), resultingData.getSpanId());
        assertEquals(new BigInteger(parentSpanId, 16).longValue(), resultingData.getParentSpanId().longValue());
        assertEquals(Long.valueOf(flags), resultingData.getFlags());
        assertEquals(port, resultingData.getPort());
    }

    @Test
    public void createNewZipkinRequestUUID_WhenRequestShouldNotBeTraced_ShouldNotFillZipkinData() {

        victim.setSamplingLevel(500);

        when(threadLocalRandom.nextInt(0, 1000)).thenReturn(500);

        ZipkinRequestUUID result = victim.createNewZipkinRequestUUID(cougarUuid, null, null, null, null, null, port);

        assertNotNull(result);
        assertFalse(result.isZipkinTracingEnabled());
    }

    @Test
    public void createNewZipkinRequestUUID_WhenRequestShouldBeTraced_ShouldGenerateZipkinData() {

        victim.setSamplingLevel(500);

        when(threadLocalRandom.nextInt(0, 1000)).thenReturn(499);

        ZipkinRequestUUID result = victim.createNewZipkinRequestUUID(cougarUuid, null, null, null, null, null, port);

        ZipkinData resultingData = result.buildZipkinData("");

        assertNotNull(result);
        assertTrue(result.isZipkinTracingEnabled());
        assertNotNull(resultingData.getTraceId());
        assertNotNull(resultingData.getSpanId());
        assertNull(resultingData.getParentSpanId());
        assertNull(resultingData.getFlags());
        assertEquals(port, resultingData.getPort());
    }
}
