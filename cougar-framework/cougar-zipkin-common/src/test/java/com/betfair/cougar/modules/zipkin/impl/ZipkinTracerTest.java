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

import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZipkinTracerTest {

    @Mock
    private ZipkinEmitter zipkinEmitter;

    @Mock
    private ZipkinRequestUUIDImpl requestUUID;

    @Mock
    private ZipkinData zipkinData;

    @Mock
    private OperationKey operationKey;

    @InjectMocks
    private ZipkinTracer victim = new ZipkinTracer();

    @Before
    public void init() {
        initMocks(this);
    }

    @Test
    public void start_WhenZipkinTracingIsEnabled_ShouldEmitServerReceive() {

        when(requestUUID.isZipkinTracingEnabled()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.start(requestUUID, operationKey);

        verify(zipkinEmitter).emitServerReceive(zipkinData);
    }

    @Test
    public void start_WhenZipkinTracingIsNotEnabled_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingEnabled()).thenReturn(false);

        victim.start(requestUUID, operationKey);

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void trace_WhenZipkinTracingIsReady_ShouldEmitAnnotation() {
        String msg = "Custom Annotation";

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.trace(requestUUID, msg);

        verify(zipkinEmitter).emitAnnotation(zipkinData, msg);
    }

    @Test
    public void trace_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.trace(requestUUID, "");

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void trace_FirstOverload_WhenZipkinTracingIsReady_ShouldEmitAnnotation() {
        String msg = "Custom Annotation num %d";
        Integer arg1 = 1;
        String formattedMessage = String.format(msg, arg1);

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.trace(requestUUID, msg, arg1);

        verify(zipkinEmitter).emitAnnotation(zipkinData, formattedMessage);
    }

    @Test
    public void trace_FirstOverload_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.trace(requestUUID, "", "");

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void trace_SecondOverload_WhenZipkinTracingIsReady_ShouldEmitAnnotation() {
        String msg = "Custom Annotation num %d.%d";
        Integer arg1 = 1;
        Integer arg2 = 2;
        String formattedMessage = String.format(msg, arg1, arg2);

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.trace(requestUUID, msg, arg1, arg2);

        verify(zipkinEmitter).emitAnnotation(zipkinData, formattedMessage);
    }

    @Test
    public void trace_SecondOverload_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.trace(requestUUID, "", "", "");

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void trace_ThirdOverload_WhenZipkinTracingIsReady_ShouldEmitAnnotation() {
        String msg = "Custom Annotation num %d.%d.%d";
        Integer arg1 = 1;
        Integer arg2 = 2;
        Integer arg3 = 3;
        String formattedMessage = String.format(msg, arg1, arg2, arg3);

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.trace(requestUUID, msg, arg1, arg2, arg3);

        verify(zipkinEmitter).emitAnnotation(zipkinData, formattedMessage);
    }

    @Test
    public void trace_ThirdOverload_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.trace(requestUUID, "", "", "", "");

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void trace_VarArgsOverload_WhenZipkinTracingIsReady_ShouldEmitAnnotation() {
        String msg = "Custom Annotation num %d.%d.%d.%d";
        Integer arg1 = 1;
        Integer arg2 = 2;
        Integer arg3 = 3;
        Integer arg4 = 3;
        String formattedMessage = String.format(msg, arg1, arg2, arg3, arg4);

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.trace(requestUUID, msg, arg1, arg2, arg3, arg4);

        verify(zipkinEmitter).emitAnnotation(zipkinData, formattedMessage);
    }

    @Test
    public void trace_VarArgsOverload_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.trace(requestUUID, "", "", "", "", "");

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void end_WhenZipkinTracingIsReady_ShouldEmitServerSend() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.end(requestUUID);

        verify(zipkinEmitter).emitServerSend(zipkinData);
    }

    @Test
    public void end_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.end(requestUUID);

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void endCall_WhenZipkinTracingIsReady_ShouldEmitClientReceive() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.endCall(null, requestUUID, null);

        verify(zipkinEmitter).emitClientReceive(zipkinData);
    }

    @Test
    public void endCall_WhenZipkinTracingIsNotReady_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingReady()).thenReturn(false);

        victim.endCall(null, requestUUID, null);

        verifyZeroInteractions(zipkinEmitter);
    }

    @Test
    public void startCall_WhenZipkinTracingIsEnabled_ShouldEmitClientSend() {

        when(requestUUID.isZipkinTracingEnabled()).thenReturn(true);
        when(requestUUID.getZipkinData()).thenReturn(zipkinData);

        victim.startCall(null, requestUUID, operationKey);

        verify(zipkinEmitter).emitClientSend(zipkinData);
    }

    @Test
    public void startCall_WhenZipkinTracingIsNotEnabled_ShouldNotEmitAnything() {

        when(requestUUID.isZipkinTracingEnabled()).thenReturn(false);

        victim.startCall(null, requestUUID, operationKey);

        verifyZeroInteractions(zipkinEmitter);
    }
}
