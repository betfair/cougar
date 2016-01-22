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
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinRequestUUIDImplTest {

    private ZipkinRequestUUID victim;

    @Mock
    private RequestUUID cougarUuid;

    @Mock
    private ZipkinData zipkinData;

    @Mock
    private ZipkinDataBuilder zipkinDataBuilder;

    @Mock
    private RequestUUID requestUUID;

    @Before
    public void init() {
        initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void ZipkinRequestUUIDImpl_WhenCougarIdIsNull_ShouldThrowNPE() {
        victim = new ZipkinRequestUUIDImpl(null, null);
    }

    @Test
    public void getRootUUIDComponent_ShouldDeferToCougarUUID() {
        String rootUUIDComponent = "abcde-1234-fghij-5678-klmno";

        when(cougarUuid.getRootUUIDComponent()).thenReturn(rootUUIDComponent);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        assertEquals(rootUUIDComponent, victim.getRootUUIDComponent());
    }

    @Test
    public void getParentUUIDComponent_ShouldDeferToCougarUUID() {
        String parentUUIDComponent = "abcde-1234-fghij-5678-klmno";

        when(cougarUuid.getParentUUIDComponent()).thenReturn(parentUUIDComponent);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        assertEquals(parentUUIDComponent, victim.getParentUUIDComponent());
    }

    @Test
    public void getLocalUUIDComponent_ShouldDeferToCougarUUID() {
        String localUUIDComponent = "abcde-1234-fghij-5678-klmno";

        when(cougarUuid.getLocalUUIDComponent()).thenReturn(localUUIDComponent);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        assertEquals(localUUIDComponent, victim.getLocalUUIDComponent());
    }

    @Test
    public void toCougarLogString_ShouldDeferToCougarUUID() {
        String cougarLogString = "abcde-1234-fghij-5678-klmno";

        when(cougarUuid.toCougarLogString()).thenReturn(cougarLogString);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        assertEquals(cougarLogString, victim.toCougarLogString());
    }

    @Test
    public void getUUID_ShouldDeferToCougarGetUUID() {
        String cougarUUID = "abcde-1234-fghij-5678-klmno";

        when(cougarUuid.getUUID()).thenReturn(cougarUUID);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        assertEquals(cougarUUID, victim.getUUID());
    }

    @Test
    public void isZipkinTracingEnabled_WhenZipkinDataBuilderIsNull_ShouldReturnFalse() {
        victim = new ZipkinRequestUUIDImpl(cougarUuid);

        assertFalse(victim.isZipkinTracingEnabled());
    }

    @Test
    public void isZipkinTracingEnabled_WhenZipkinDataBuilderIsNotNull_ShouldReturnTrue() {
        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        assertTrue(victim.isZipkinTracingEnabled());
    }

    @Test(expected = NullPointerException.class)
    public void buildZipkinData_WhenSpanNameIsNull_ShouldThrowNPE() {
        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        victim.buildZipkinData(null);
    }

    @Test
    public void buildZipkinData_WhenCalledForTheFirstTime_ShouldPopulateAndReturnZipkinData() {
        String spanName = "Span Name";

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(zipkinDataBuilder);
        when(zipkinDataBuilder.build()).thenReturn(zipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        ZipkinData result = victim.buildZipkinData(spanName);

        assertEquals(zipkinData, result);
    }

    @Test(expected = IllegalStateException.class)
    public void buildZipkinData_WhenZipkinDataAlreadyExists_ShouldThrowISE() {
        String spanName = "Span Name";

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(zipkinDataBuilder);
        when(zipkinDataBuilder.build()).thenReturn(zipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        // Build Zipkin data for the first time
        victim.buildZipkinData(spanName);

        // Attempt to build Zipkin data again
        victim.buildZipkinData(spanName);
    }

    @Test
    public void isZipkinTracingReady_WhenZipkinDataHasNotBeenBuilt_ShouldReturnFalse() {
        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        assertFalse(victim.isZipkinTracingReady());
    }

    @Test
    public void isZipkinTracingReady_WhenZipkinDataHasBeenBuilt_ShouldReturnTrue() {
        String spanName = "Span Name";

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(zipkinDataBuilder);
        when(zipkinDataBuilder.build()).thenReturn(zipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.buildZipkinData(spanName);

        assertTrue(victim.isZipkinTracingReady());
    }

    @Test
    public void toString_ShouldConcatenateZipkinDataWithCougarUUID() {
        String zipkinSpanName = "Span Name";
        String cougarUUID = "abcde-1234-fghij-5678-klmno";
        String zipkinDataToString = "ZipkinDataImpl{spanName=" + zipkinSpanName + "}";
        String expectedResult = "ZipkinRequestUUIDImpl{cougarUuid=" + cougarUUID +
                ", zipkinData=" + zipkinDataToString + "}";

        when(zipkinDataBuilder.spanName(zipkinSpanName)).thenReturn(zipkinDataBuilder);
        when(zipkinDataBuilder.build()).thenReturn(zipkinData);
        when(cougarUuid.getUUID()).thenReturn(cougarUUID);
        when(zipkinData.toString()).thenReturn(zipkinDataToString);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.buildZipkinData(zipkinSpanName);
        String result = victim.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void getZipkinData_WhenZipkinDataHasBeenBuilt_ShouldReturnBuiltData() {
        String spanName = "Span Name";

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(zipkinDataBuilder);
        when(zipkinDataBuilder.build()).thenReturn(zipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.buildZipkinData(spanName);

        assertEquals(victim.getZipkinData(), zipkinData);
    }

    @Test(expected = IllegalStateException.class)
    public void getZipkinData_WhenZipkinDataHasNotBeenBuiltYet_ShouldThrowISE() {
        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.getZipkinData();
    }

    @Test(expected = IllegalStateException.class)
    public void getZipkinData_WhenZipkinIsDisabledForThisRequest_ShouldThrowISE() {
        victim = new ZipkinRequestUUIDImpl(cougarUuid);

        victim.getZipkinData();
    }

    @Test
    public void getNewSubUUID_WhenZipkinTracingIsNotEnabled_ChildShouldNotBeTracedEither() {

        when(cougarUuid.getNewSubUUID()).thenReturn(requestUUID);

        victim = new ZipkinRequestUUIDImpl(cougarUuid);

        RequestUUID result = victim.getNewSubUUID();

        assertNotNull(result);
        assertFalse(((ZipkinRequestUUID) result).isZipkinTracingEnabled());
    }

    @Test
    public void getNewSubUUID_WhenZipkinTracingIsEnabled_ShouldReturnTraceableChild() {
        String spanName = "Span Name";
        long traceId = 123456789;
        long spanId = 987654321;
        short port = 9101;

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(zipkinDataBuilder);
        when(zipkinDataBuilder.build()).thenReturn(zipkinData);
        when(cougarUuid.getNewSubUUID()).thenReturn(requestUUID);
        when(zipkinData.getTraceId()).thenReturn(traceId);
        when(zipkinData.getSpanId()).thenReturn(spanId);
        when(zipkinData.getPort()).thenReturn(port);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);
        victim.buildZipkinData(spanName);

        RequestUUID result = victim.getNewSubUUID();

        assertNotNull(result);
        assertTrue(((ZipkinRequestUUID) result).isZipkinTracingEnabled());
    }
}
