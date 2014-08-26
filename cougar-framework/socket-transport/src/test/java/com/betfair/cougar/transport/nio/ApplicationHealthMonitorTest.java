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

package com.betfair.cougar.transport.nio;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.betfair.cougar.api.Service;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusAggregator;
import com.betfair.tornjak.monitor.StatusChangeEvent;
import com.betfair.tornjak.monitor.StatusChangeListener;
import com.betfair.tornjak.monitor.StatusSource;

import static org.mockito.Mockito.*;


public class ApplicationHealthMonitorTest {

	private ExecutionVenueNioServer executionVenueNioServer;
	private StatusAggregator aggregator;
	private StatusSource statusSource;
    private MonitorRegistry monitorRegistry;

    @Before
	public void before() {
		executionVenueNioServer = mock(ExecutionVenueNioServer.class);
		monitorRegistry = mock(MonitorRegistry.class);
		aggregator = mock(StatusAggregator.class);
        when(monitorRegistry.getStatusAggregator()).thenReturn(aggregator);

		statusSource = mock(StatusSource.class);

	}

	@Test
	public void testActiveMonitoringOK() throws InterruptedException {
		final boolean[] result = new boolean[1];
		AbstractHealthMonitorStrategy stategy = new AbstractHealthMonitorStrategy() {

			@Override
			public void update(boolean isHealthy) {
				result[0] = isHealthy;

			}
		};
		ApplicationHealthMonitor sut = new ApplicationHealthMonitor(executionVenueNioServer, stategy, 50, monitorRegistry);
		sut.setServices(null);
		when(aggregator.getStatus()).thenReturn(Status.OK);

		Thread.sleep(100);
		Assert.assertEquals(true, result[0]);

	}

	@Test
	public void testActiveMonitoringFAIL() throws InterruptedException {
		final boolean[] result = new boolean[1];
		AbstractHealthMonitorStrategy stategy = new AbstractHealthMonitorStrategy() {

			@Override
			public void update(boolean isHealthy) {
				result[0] = isHealthy;

			}
		};
		ApplicationHealthMonitor sut = new ApplicationHealthMonitor(executionVenueNioServer, stategy, 50, monitorRegistry);
		sut.setServices(null);
		when(aggregator.getStatus()).thenReturn(Status.FAIL);

		Thread.sleep(100);
		Assert.assertEquals(false, result[0]);

	}

	@Test
	public void testActiveMonitoringWARN() throws InterruptedException {
		final boolean[] result = new boolean[1];
		AbstractHealthMonitorStrategy stategy = new AbstractHealthMonitorStrategy() {

			@Override
			public void update(boolean isHealthy) {
				result[0] = isHealthy;

			}
		};
		ApplicationHealthMonitor sut = new ApplicationHealthMonitor(executionVenueNioServer, stategy, 50, monitorRegistry);
		sut.setServices(null);
		when(aggregator.getStatus()).thenReturn(Status.WARN);

		Thread.sleep(100);
		Assert.assertEquals(true, result[0]);

	}

	@Test
	public void testPassiveMonitoringUpdateFAIL() {
		final boolean[] result = new boolean[1];
		AbstractHealthMonitorStrategy stategy = new AbstractHealthMonitorStrategy() {

			@Override
			public void update(boolean isHealthy) {
				result[0] = isHealthy;

			}
		};
		ApplicationHealthMonitor sut = new ApplicationHealthMonitor(executionVenueNioServer, stategy, 0, monitorRegistry);
		sut.setServices(null);

		when(aggregator.getStatus()).thenReturn(Status.OK);

		Assert.assertEquals(true, result[0]);


		ArgumentCaptor<StatusChangeListener> captor = ArgumentCaptor.forClass(StatusChangeListener.class);
		verify(aggregator).addStatusChangeListener(captor.capture());
		StatusChangeListener listener = captor.getValue();
		listener.statusChanged(new StatusChangeEvent(statusSource, null, Status.FAIL));

		Assert.assertEquals(false, result[0]);

	}

	@Test
	public void testPassiveMonitoringUpdateWARN() {
		final boolean[] result = new boolean[1];
		AbstractHealthMonitorStrategy stategy = new AbstractHealthMonitorStrategy() {

			@Override
			public void update(boolean isHealthy) {
				result[0] = isHealthy;

			}
		};
		ApplicationHealthMonitor sut = new ApplicationHealthMonitor(executionVenueNioServer, stategy, 0, monitorRegistry);
		sut.setServices(null);

		when(aggregator.getStatus()).thenReturn(Status.OK);

		Assert.assertEquals(true, result[0]);


		ArgumentCaptor<StatusChangeListener> captor = ArgumentCaptor.forClass(StatusChangeListener.class);
		verify(aggregator).addStatusChangeListener(captor.capture());
		StatusChangeListener listener = captor.getValue();
		listener.statusChanged(new StatusChangeEvent(statusSource, null, Status.WARN));

		Assert.assertEquals(true, result[0]);

	}







}
