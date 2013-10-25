/*
 * Copyright 2013, The Sporting Exchange Limited
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

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.util.monitors.JMXMonitor.IsHealthyExpression;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import org.junit.Test;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.logging.Level;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JMXMonitorTest extends CougarUtilTestCase {
	MonitorRegistry registry = mock(MonitorRegistry.class);
	MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    public JMXMonitorTest() {
        super(JMXMonitor.class);
    }

	private static final IsHealthyExpression notHealthy = new IsHealthyExpression() {
		public boolean evaluate(Object value) {
			return false;
		} };

	private static final IsHealthyExpression isHealthy = new IsHealthyExpression() {
		public boolean evaluate(Object value) {
			return true;
		} };


    @Test
    public void testJMXMonitorBadFailState() throws Exception{
        try {
            JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
                    "java.lang:type=ClassLoading",
                    "Verbose",
                    "false",
                    false,
                    Status.OK);
            fail();
        } catch (IllegalArgumentException e) {
            // good
        }
    }


    @Test
	public void testJMXMonitorOK() throws Exception{
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"false",
				false);
		
		getMessageLog().clear();
		assertEquals(Status.OK, jxm.getStatus());
		assertEquals(getMessageLog().size(), 0);

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
	public void testJMXMonitorIgnoreOK() throws Exception{
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=NON_EXISTENT",
				"Verbose",
				"true",
				true);
		
		getMessageLog().clear();
		assertEquals(Status.OK, jxm.getStatus());
		assertEquals(getMessageLog().size(), 0);

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
	public void testJMXMonitorFail() throws Exception{
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"true",
				false);
		
		getMessageLog().clear();
		assertEquals(Status.FAIL, jxm.getStatus());
		assertEquals(getMessageLog().size(), 1);

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
    public void testJMXMonitorWarning() throws Exception{
        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
                "java.lang:type=ClassLoading",
                "Verbose",
                "true",
                false,
                Status.WARN);

        getMessageLog().clear();
        assertEquals(Status.WARN, jxm.getStatus());
        assertEquals(getMessageLog().size(), 1);

        verify(registry, times(1)).addMonitor(any(Monitor.class));
    }

    @Test
	public void testJMXMonitorIgnoreFail() throws Exception{
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=NON_EXISTENT",
				"Verbose",
				"true",
				false);
		
		getMessageLog().clear();
		assertEquals(Status.FAIL, jxm.getStatus());
		assertEquals(getMessageLog().size(), 1);

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
	public void testJMXMonitorExpressionEvaluation() throws Exception{
        IsHealthyExpression expression = new IsHealthyExpression() {
            @Override
            public boolean evaluate(Object value) {
                return false;
            }
        };
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=NON_EXISTENT",
				"Verbose",
				expression,
				false,
                Status.WARN);

		getMessageLog().clear();
		assertEquals(Status.WARN, jxm.getStatus());
		assertEquals(getMessageLog().size(), 1);

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
    public void testJMXMonitorMultiFail() throws Exception{
		final IsHealthyExpression healthy = mock(IsHealthyExpression.class);
		final IsHealthyExpression unhealthy = mock(IsHealthyExpression.class);
        when(healthy.evaluate(anyString())).thenReturn(true);
        when(unhealthy.evaluate(anyString())).thenReturn(false);
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"true",
				false);
		
		getMessageLog().clear();
		assertEquals(Status.FAIL, jxm.getStatus());
		assertEquals(getMessageLog().size(), 1);
		assertEquals(Level.WARNING, getMessageLog().get(0).getLevel());

		// Check it doesn't log each time a failure occurs
		getMessageLog().clear();
		jxm.getStatus();
		jxm.getStatus();
		assertEquals(getMessageLog().size(), 0);

		
		// Put it back into an OK state
		Field f = jxm.getClass().getDeclaredField("isHealthyExpression");
		f.setAccessible(true);
		f.set(jxm, isHealthy);
		
		assertEquals(Status.OK, jxm.getStatus());
		assertEquals(getMessageLog().size(), 1);
        assertEquals(Level.INFO, getMessageLog().get(0).getLevel());
        getMessageLog().clear();

		// And back to failure.
		f.set(jxm, notHealthy);
		
		assertEquals(Status.FAIL, jxm.getStatus());
		assertEquals(getMessageLog().size(), 1);

        verify(registry, times(1)).addMonitor(any(Monitor.class));

	}
}
