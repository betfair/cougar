/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.core.impl.kpi;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.tornjak.kpi.KPIMonitor;
import com.betfair.tornjak.kpi.aop.KPIAsyncTimedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Captor;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.junit.Assert.*;

/**
 * Date: 18/06/2013
 * Time: 10:02:56
 */
public class KPIAsyncTimerTest {
    @Mock
    private KPIMonitor mockMonitor;
    @Mock
    private KPIAsyncTimedEvent mockEvent;
    @Mock
    private ExecutionObserver mockObserver;
    @Mock
    private ExecutionResult mockResult;
    @Mock
    private ProceedingJoinPoint mockJP;
    @Captor
    private ArgumentCaptor<Object[]> argCaptor;

    private KPIAsyncTimer timer;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        timer = new KPIAsyncTimer();
        timer.setMonitor(mockMonitor);

        when(mockEvent.catchFailures()).thenReturn(false);
        when(mockEvent.operation()).thenReturn("myOperation");
        when(mockEvent.value()).thenReturn("my.metric");

    }

    @Test
    public void shouldCallProceedWithWrappedObserver() throws Throwable {
        when(mockJP.getArgs()).thenReturn(new Object[]{"arg1", "arg2", mockObserver});

        timer.measureAsyncMethod(mockJP, mockEvent, mockObserver);

        verify(mockJP).proceed(argCaptor.capture());

        assertTrue(argCaptor.getValue()[2] instanceof PerformanceMonitoringExecutionObserver);

        ExecutionObserver obs = (ExecutionObserver) argCaptor.getValue()[2];
        obs.onResult(mockResult);

        verify(mockMonitor).addEvent(eq("my.metric"), eq("myOperation"), anyDouble());
    }

    @Test
    public void observerNotLastInArgList() throws Throwable {

        when(mockJP.getArgs()).thenReturn(new Object[]{"arg1",  mockObserver, "arg3"});

        timer.measureAsyncMethod(mockJP, mockEvent, mockObserver);

        verify(mockJP).proceed(argCaptor.capture());

        assertFalse(argCaptor.getValue()[1] instanceof PerformanceMonitoringExecutionObserver);
    }

    @Test
    public void multipleObserverArgs() throws Throwable {

        when(mockJP.getArgs()).thenReturn(new Object[]{"arg1",  mockObserver, mockObserver});

        timer.measureAsyncMethod(mockJP, mockEvent, mockObserver);

        verify(mockJP).proceed(argCaptor.capture());

        assertFalse(argCaptor.getValue()[1] instanceof PerformanceMonitoringExecutionObserver);
        assertTrue(argCaptor.getValue()[2] instanceof PerformanceMonitoringExecutionObserver);

        ExecutionObserver obs = (ExecutionObserver) argCaptor.getValue()[2];
        obs.onResult(mockResult);

        verify(mockMonitor).addEvent(eq("my.metric"), eq("myOperation"), anyDouble());
    }


}
