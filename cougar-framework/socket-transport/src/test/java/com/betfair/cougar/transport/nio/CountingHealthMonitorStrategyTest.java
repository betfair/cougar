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

import junit.framework.Assert;

import org.junit.Test;

import com.betfair.cougar.transport.nio.HealthMonitorStrategy.HealthMonitorStrategyListener;


public class CountingHealthMonitorStrategyTest {


	@Test
	public void testInitialUpdate() {
		CountingHealthMonitorStrategy sut = new CountingHealthMonitorStrategy(3);
		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0]= isHealthy;

			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
	}

	@Test
	public void testUpdate() {
		CountingHealthMonitorStrategy sut = new CountingHealthMonitorStrategy(3);
		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0]= isHealthy;

			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Assert.assertEquals(true,result[0]);
		sut.update(false);
		Assert.assertEquals(true,result[0]);
		sut.update(false);
		Assert.assertEquals(false,result[0]);
	}

	@Test
	public void testResetCount() {
		CountingHealthMonitorStrategy sut = new CountingHealthMonitorStrategy(3);
		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0]= isHealthy;

			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Assert.assertEquals(true,result[0]);
		sut.update(true);
		Assert.assertEquals(true,result[0]);
		sut.update(false);
		Assert.assertEquals(true,result[0]);
		sut.update(false);
		Assert.assertEquals(true,result[0]);
		sut.update(false);
		Assert.assertEquals(false,result[0]);
	}

	@Test
	public void testUpdateAfterListenersAdvised() {
		CountingHealthMonitorStrategy sut = new CountingHealthMonitorStrategy(3);
		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0]= isHealthy;

			}
		});
		sut.update(true);
		Assert.assertEquals(true, result[0]);

		sut.update(false);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Assert.assertEquals(false, result[0]);
		sut.update(true);
		Assert.assertEquals(false, result[0]);

	}
}
