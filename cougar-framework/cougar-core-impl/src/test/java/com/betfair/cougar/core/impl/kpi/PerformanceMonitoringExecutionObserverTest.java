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
import com.betfair.tornjak.kpi.KPITimer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Date: 18/06/2013
 * Time: 10:03:24
 */
public class PerformanceMonitoringExecutionObserverTest {

    @Mock
    private KPITimer mockTimer;
    @Mock
    private ExecutionObserver mockObserver;
    @Mock
    private KPIMonitor mockMonitor;
    @Mock
    private ExecutionResult mockResult;

    private PerformanceMonitoringExecutionObserver observer;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
         public void shouldReportDuration() throws Exception {
        when(mockTimer.stop()).thenReturn(32D);
        observer = new PerformanceMonitoringExecutionObserver(mockObserver, mockMonitor, mockTimer, false, "name", "operation");

        observer.onResult(mockResult);

        verify(mockObserver).onResult(mockResult);
        verify(mockMonitor).addEvent("name", "operation", 32D);
    }

    @Test
    public void shouldReportDurationOnFault() throws Exception {
        when(mockTimer.stop()).thenReturn(32D);
        when(mockResult.isFault()).thenReturn(true);
        observer = new PerformanceMonitoringExecutionObserver(mockObserver, mockMonitor, mockTimer, false, "name", "operation");

        observer.onResult(mockResult);

        verify(mockObserver).onResult(mockResult);
        verify(mockMonitor).addEvent("name", "operation", 32D);
    }

    @Test
    public void shouldReportDurationCatchFailures() throws Exception {
        when(mockTimer.stop()).thenReturn(32D);
        observer = new PerformanceMonitoringExecutionObserver(mockObserver, mockMonitor, mockTimer, true, "name", "operation");

        observer.onResult(mockResult);

        verify(mockObserver).onResult(mockResult);
        verify(mockMonitor).addEvent("name", "operation", 32D, true);
    }

    @Test
    public void shouldReportDurationOnFaultCatchFailures() throws Exception {
        when(mockTimer.stop()).thenReturn(32D);
        when(mockResult.isFault()).thenReturn(true);
        observer = new PerformanceMonitoringExecutionObserver(mockObserver, mockMonitor, mockTimer, true, "name", "operation");

        observer.onResult(mockResult);

        verify(mockObserver).onResult(mockResult);
        verify(mockMonitor).addEvent("name", "operation", 32D, false);
    }
}
