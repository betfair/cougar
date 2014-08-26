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

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.util.monitors.JMXMonitor.IsHealthyExpression;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JMXMonitorTest extends CougarUtilTestCase {
	private MonitorRegistry registry;
	private MBeanServer mBeanServer;
    private Logger logger;
    private Logger oldLogger;

    public JMXMonitorTest() {
        super(JMXMonitor.class);
    }

    @Before
    public void setUp() {
        registry = mock(MonitorRegistry.class);
        mBeanServer = mock(MBeanServer.class);
        logger = mock(Logger.class);
        oldLogger = JMXMonitor.setLogger(logger);
    }

    @After
    public void tearDown() {
        JMXMonitor.setLogger(oldLogger);
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

    private void verifyZeroWarnInteractions() {
        verify(logger, times(0)).warn(anyString(), anyObject());
        verify(logger, times(0)).warn(anyString(), anyObject(), anyObject());
        verify(logger, times(0)).warn(anyString(), any(Object[].class));
        verify(logger, times(0)).warn(anyString());
        verify(logger, times(0)).warn(anyString(), any(Throwable.class));
    }

    @Test
	public void testJMXMonitorOK() throws Exception{
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);
        when(mBeanServer.getAttribute(any(ObjectName.class), anyString())).thenReturn("false");

        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"false",
				false);

		assertEquals(Status.OK, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
	public void testJMXMonitorFail_NotInMBeanServer() throws Exception{
		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"false",
				false);

		assertEquals(Status.FAIL, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject());
//        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}



    @Test
	public void testJMXMonitorIgnoreOK() throws Exception{
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(false);

        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=NON_EXISTENT",
				"Verbose",
				"true",
				true);

		assertEquals(Status.OK, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
	public void testJMXMonitorFail() throws Exception{
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"true",
				false);

		assertEquals(Status.FAIL, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
    public void testJMXMonitorWarning() throws Exception{
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
                "java.lang:type=ClassLoading",
                "Verbose",
                "true",
                false,
                Status.WARN);

        assertEquals(Status.WARN, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

        verify(registry, times(1)).addMonitor(any(Monitor.class));
    }

    @Test
	public void testJMXMonitorIgnoreFail() throws Exception{
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=NON_EXISTENT",
				"Verbose",
				"true",
				false);

		assertEquals(Status.FAIL, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

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
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

		JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=NON_EXISTENT",
				"Verbose",
				expression,
				false,
                Status.WARN);

		assertEquals(Status.WARN, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verifyZeroWarnInteractions();

        verify(registry, times(1)).addMonitor(any(Monitor.class));
	}

    @Test
    public void testJMXMonitorMultiFail() throws Exception{
		final IsHealthyExpression healthy = mock(IsHealthyExpression.class);
		final IsHealthyExpression unhealthy = mock(IsHealthyExpression.class);
        when(healthy.evaluate(anyString())).thenReturn(true);
        when(unhealthy.evaluate(anyString())).thenReturn(false);
        when(mBeanServer.isRegistered(any(ObjectName.class))).thenReturn(true);

        JMXMonitor jxm = new JMXMonitor(registry, mBeanServer,
				"java.lang:type=ClassLoading",
				"Verbose",
				"true",
				false);

		assertEquals(Status.FAIL, jxm.getStatus());

        verify(logger, times(1)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).warn(anyString(), anyString(), anyString(), anyString());

		// Check it doesn't log each time a failure occurs
		jxm.getStatus();
		jxm.getStatus();

        verify(logger, times(3)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString());
        verify(logger, times(0)).info(anyString(), anyString(), anyString(), anyString());
        verify(logger, times(1)).warn(anyString(), anyString(), anyString(), anyString());


		// Put it back into an OK state
		Field f = jxm.getClass().getDeclaredField("isHealthyExpression");
		f.setAccessible(true);
		f.set(jxm, isHealthy);

		assertEquals(Status.OK, jxm.getStatus());

        verify(logger, times(4)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString(), anyString(), anyString());
        verify(logger, times(1)).warn(anyString(), anyString(), anyString(), anyString());

		// And back to failure.
		f.set(jxm, notHealthy);

		assertEquals(Status.FAIL, jxm.getStatus());

        verify(logger, times(5)).debug(anyString(), anyObject(), anyObject());
        verify(logger, times(1)).info(anyString(), anyString(), anyString());
        verify(logger, times(2)).warn(anyString(), anyString(), anyString(), anyString());

        verify(registry, times(1)).addMonitor(any(Monitor.class));

	}
}
