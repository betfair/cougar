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

package com.betfair.cougar.util.monitors;

import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class SimpleHealthMonitorTest {


    @Test
    public void testLoad() {
        TestingComponent component = new TestingComponent();
        final MonitorRegistry registry = mock(MonitorRegistry.class);

        SimpleHealthMonitor monitor = new SimpleHealthMonitor("blah", component, registry, Status.FAIL);

        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(registry, times(1)).addMonitor(captor.capture());
        assertSame(monitor, captor.getValue());

        assertEquals("blah",monitor.getName());

        //-- and we should pass back health statii correctly

        component.healthy = false;
        assertEquals(Status.FAIL, monitor.getStatus());

        component.healthy = true;
        assertEquals(Status.OK, monitor.getStatus());

        component.healthy = false;    // and back again
        assertEquals(Status.FAIL, monitor.getStatus());
    }

        @Test
    public void testFailState() {
        TestingComponent component = new TestingComponent();
        final MonitorRegistry registry = mock(MonitorRegistry.class);

        SimpleHealthMonitor monitor = new SimpleHealthMonitor("blah", component, registry, Status.WARN);

            ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
            verify(registry, times(1)).addMonitor(captor.capture());
            assertSame(monitor, captor.getValue());

        assertEquals("blah",monitor.getName());

        //-- and we should pass back health statii correctly

        component.healthy = false;
        assertEquals(Status.WARN, monitor.getStatus());

        component.healthy = true;
        assertEquals(Status.OK, monitor.getStatus());

        component.healthy = false;    // and back again
        assertEquals(Status.WARN, monitor.getStatus());
    }

    private class TestingComponent implements HealthAware {

        boolean healthy;

        @Override
        public boolean isHealthy() {
            return healthy;
        }
    }
}
