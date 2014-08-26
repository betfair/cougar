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

package com.betfair.cougar.core.impl.jmx;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import com.betfair.cougar.test.CougarTestCase;


public class ThreadDumperTest  extends CougarTestCase {
	public void testPath() {
		assertEquals("threaddump.jsp", new ThreadDumper().getPath());
	}

	public void testThreadContentionMonitoring() {
		ThreadMXBean mx = ManagementFactory.getThreadMXBean();
		mx.setThreadContentionMonitoringEnabled(false);

		Map<String, String> params = new HashMap<String, String>();
		params.put("command", "toggleCM");
		String value = new ThreadDumper().process(params);
		assertTrue(value.contains("Contention monitoring enabled: true"));

		value = new ThreadDumper().process(params);
		assertTrue(value.contains("Contention monitoring enabled: false"));
	}

	public void testGeneralResponse() {
		ThreadMXBean mx = ManagementFactory.getThreadMXBean();
		mx.resetPeakThreadCount();

		Map<String, String> params = new HashMap<String, String>();
		String value = new ThreadDumper().process(params);
		assertTrue(value.contains("All threads:"));
		assertTrue(value.contains("Live thread count:"));
		assertTrue(value.contains("Report generated at "));
	}
}
