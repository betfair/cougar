package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
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
    public void getUUID_ShouldDeferToCougarLogString() {
        String cougarLogString = "abcde-1234-fghij-5678-klmno";

        when(cougarUuid.toCougarLogString()).thenReturn(cougarLogString);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, null);

        assertEquals(cougarLogString, victim.getUUID());
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
        ZipkinData expectedZipkinData = mock(ZipkinData.class);
        ZipkinDataBuilder interimZipkinDataBuilder = mock(ZipkinDataBuilder.class);

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(interimZipkinDataBuilder);
        when(interimZipkinDataBuilder.build()).thenReturn(expectedZipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        ZipkinData result = victim.buildZipkinData(spanName);

        assertEquals(expectedZipkinData, result);
    }

    @Test(expected = IllegalStateException.class)
    public void buildZipkinData_WhenZipkinDataAlreadyExists_ShouldThrowISE() {
        String spanName = "Span Name";
        ZipkinData expectedZipkinData = mock(ZipkinData.class);
        ZipkinDataBuilder interimZipkinDataBuilder = mock(ZipkinDataBuilder.class);

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(interimZipkinDataBuilder);
        when(interimZipkinDataBuilder.build()).thenReturn(expectedZipkinData);

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
        ZipkinData expectedZipkinData = mock(ZipkinData.class);
        ZipkinDataBuilder interimZipkinDataBuilder = mock(ZipkinDataBuilder.class);

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(interimZipkinDataBuilder);
        when(interimZipkinDataBuilder.build()).thenReturn(expectedZipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.buildZipkinData(spanName);

        assertTrue(victim.isZipkinTracingReady());
    }

    @Test
    public void toString_ShouldConcatenateZipkinDataWithCougarUUID() {
        String zipkinSpanName = "Span Name";
        String cougarLogString = "abcde-1234-fghij-5678-klmno";
        String zipkinDataToString = "ZipkinDataImpl{spanName=" + zipkinSpanName + "}";
        ZipkinData expectedZipkinData = mock(ZipkinData.class);
        String expectedResult = "ZipkinRequestUUIDImpl{cougarUuid=" + cougarLogString +
                ", zipkinData=" + zipkinDataToString + "}";

        ZipkinDataBuilder interimZipkinDataBuilder = mock(ZipkinDataBuilder.class);

        when(zipkinDataBuilder.spanName(zipkinSpanName)).thenReturn(interimZipkinDataBuilder);
        when(interimZipkinDataBuilder.build()).thenReturn(expectedZipkinData);
        when(cougarUuid.toCougarLogString()).thenReturn(cougarLogString);
        when(expectedZipkinData.toString()).thenReturn(zipkinDataToString);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.buildZipkinData(zipkinSpanName);
        String result = victim.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void getZipkinData_WhenZipkinDataHasBeenBuilt_ShouldReturnBuiltData() {
        String spanName = "Span Name";
        ZipkinData expectedZipkinData = mock(ZipkinData.class);
        ZipkinDataBuilder interimZipkinDataBuilder = mock(ZipkinDataBuilder.class);

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(interimZipkinDataBuilder);
        when(interimZipkinDataBuilder.build()).thenReturn(expectedZipkinData);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);

        victim.buildZipkinData(spanName);

        assertEquals(victim.getZipkinData(), expectedZipkinData);
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
        RequestUUID requestUUID = mock(RequestUUID.class);

        when(cougarUuid.getNewSubUUID()).thenReturn(requestUUID);

        victim = new ZipkinRequestUUIDImpl(cougarUuid);

        RequestUUID result = victim.getNewSubUUID();

        assertNotNull(result);
        assertFalse(((ZipkinRequestUUID)result).isZipkinTracingEnabled());
    }

    @Test
    public void getNewSubUUID_WhenZipkinTracingIsEnabled_ShouldReturnTraceableChild() {
        String spanName = "Span Name";
        ZipkinData expectedZipkinData = mock(ZipkinData.class);
        ZipkinDataBuilder interimZipkinDataBuilder = mock(ZipkinDataBuilder.class);
        RequestUUID requestUUID = mock(RequestUUID.class);
        long traceId = 123456789;
        long spanId = 987654321;
        short port = 9101;

        when(zipkinDataBuilder.spanName(spanName)).thenReturn(interimZipkinDataBuilder);
        when(interimZipkinDataBuilder.build()).thenReturn(expectedZipkinData);
        when(cougarUuid.getNewSubUUID()).thenReturn(requestUUID);
        when(expectedZipkinData.getTraceId()).thenReturn(traceId);
        when(expectedZipkinData.getSpanId()).thenReturn(spanId);
        when(expectedZipkinData.getPort()).thenReturn(port);

        victim = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);
        victim.buildZipkinData(spanName);

        RequestUUID result = victim.getNewSubUUID();

        assertNotNull(result);
        assertTrue(((ZipkinRequestUUID)result).isZipkinTracingEnabled());
    }
}
