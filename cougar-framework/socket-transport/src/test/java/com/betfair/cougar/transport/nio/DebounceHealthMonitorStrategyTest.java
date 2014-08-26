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

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

import com.betfair.cougar.transport.nio.HealthMonitorStrategy.HealthMonitorStrategyListener;


public class DebounceHealthMonitorStrategyTest {

	@Test
	public void testInitialUpdate() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);

		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;
			}});

		sut.update(true);
		Assert.assertEquals(true, result[0]);

	}

	@Test
	public void testUpdate() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);

		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;

			}
		});
		sut.update(true); //initial update
		Assert.assertEquals(true, result[0]);
		sut.update(false);//update
		Thread.sleep(10);
		Assert.assertEquals(true, result[0]); //shouldn't have updated yet
		Thread.sleep(100);
		Assert.assertEquals(false, result[0]);//should have updated by now

	}

	@Test
	public void testUpdateCancelled() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);
		final boolean[] result = new boolean[1];
		final int[] updateCount = new int[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;
				updateCount[0]++;
			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Thread.sleep(10);
		Assert.assertEquals(true, result[0]);//shouldn't have updated yet
		sut.update(true); //should result in cancel of previous update
		Thread.sleep(100);
		Assert.assertEquals(true, result[0]);//should still be in inital state
		Assert.assertEquals(1, updateCount[0]);

	}

	@Test
	public void testUpdateAfterCancelled() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);
		final boolean[] result = new boolean[1];
		final int[] updateCount = new int[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;
				updateCount[0]++;

			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Thread.sleep(10);
		Assert.assertEquals(true, result[0]);//shouldn't have updated yet
		sut.update(true); //should result in cancel of previous update
		Thread.sleep(100);
		Assert.assertEquals(true, result[0]);//should still be in initial state
		sut.update(false) ;
		Thread.sleep(100);
		Assert.assertEquals(false, result[0]);
		Assert.assertEquals(2, updateCount[0]);

	}


	@Test
	public void testConsecutiveUpdates() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);
		final boolean[] result = new boolean[1];
		final int[] updateCount = new int[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;
				updateCount[0]++;

			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Assert.assertEquals(true, result[0]);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(100);
		Assert.assertEquals(false, result[0]);
		Assert.assertEquals(2, updateCount[0]);
	}

	@Test
	public void testConsecutiveUpdatesExceedDebounce() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);
		final boolean[] result = new boolean[1];
		final int[] updateCount = new int[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;
				updateCount[0]++;

			}
		});

		sut.update(true);
		Assert.assertEquals(true, result[0]);
		sut.update(false);
		Assert.assertEquals(true, result[0]);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		sut.update(false); //should not result in cancel of previous update
		Thread.sleep(10);
		Assert.assertEquals(false, result[0]);
		Assert.assertEquals(2, updateCount[0]);
	}


	@Test
	public void testUpdateToInitial() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);
		final int[] updateCount = new int[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				updateCount[0]++;

			}
		});

		sut.update(true);
		Assert.assertEquals(1, updateCount[0]);
		sut.update(true);
		Assert.assertEquals(1, updateCount[0]);
		Thread.sleep(100);
		Assert.assertEquals(1, updateCount[0]);
	}

	@Test
	public void gremlinTest() throws InterruptedException {
		DebounceHealthMonitorStrategy sut = new DebounceHealthMonitorStrategy(50);
		final boolean[] result = new boolean[1];
		sut.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				result[0] = isHealthy;

			}
		});

		Random r = new Random();
		boolean health = r.nextBoolean();

		sut.update(health);
		int sleep;
		for (int i=0; i<100; i++) {
			health = r.nextBoolean();
			sut.update(health);
			sleep = r.nextInt(10) < 8 ? 10 : 100;
			Thread.sleep(sleep);
		}
		Thread.sleep(100);

		Assert.assertEquals(health, result[0]);
	}

}
