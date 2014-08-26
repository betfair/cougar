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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.junit.Assert.*;
/**
 * Date: 18/06/2013
 * Time: 10:38:56
 */
public class TestAnnotation {
    private static ClassPathXmlApplicationContext context;
    private static String SPRING_CONFIG_FILE = "testconf/test-kpi-async.xml";
    @Mock
    private ExecutionObserver mockObserver;
    @Mock
    private KPIMonitor mockMonitor;
    @Mock
    private ExecutionResult mockResult;

    private KPIAsyncTimer timer;
    private TestAnnotation target;


    @KPIAsyncTimedEvent(value = "myService", operation = "operation1")
    public void testMethod1(String arg1, ExecutionObserver observer) {
        assertEquals("argument", arg1);
        assertTrue(observer instanceof PerformanceMonitoringExecutionObserver);
        observer.onResult(mockResult);
    }

    @Test
    public void testMethod1() throws Exception {
        target.testMethod1("argument", mockObserver);

        verify(mockObserver).onResult(mockResult);
        verify(mockMonitor).addEvent(eq("myService"), eq("operation1"), anyDouble());
    }

    /**
     * Init spring context - do as static to avoid reloading overhead (quite slow)
     */
    @BeforeClass
    public static void beforeClass() {
        context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILE);
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        target = (TestAnnotation) context.getBean("target");
        timer = (KPIAsyncTimer) context.getBean("aspect");
        timer.setMonitor(mockMonitor);
        target.setMockResult(mockResult);

    }

    public void setMockResult(ExecutionResult mockResult) {
        this.mockResult = mockResult;
    }
}
