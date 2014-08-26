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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * Strategy that fires the update only if the status does not change again for a period of time
 */
public class DebounceHealthMonitorStrategy extends AbstractHealthMonitorStrategy {

	private final long debouncePeriod;
	private ScheduledExecutorService scheduledExecutor;
	private ScheduledFuture scheduledFuture;
	private boolean currentState;

	public DebounceHealthMonitorStrategy(long debouncePeriod) {
		this.debouncePeriod = debouncePeriod;
	}


	@Override
	public synchronized void update(final boolean isHealthy) {

		if (scheduledExecutor == null) {
			scheduledExecutor = Executors.newSingleThreadScheduledExecutor(createThreadFactory());
			adviseListeners(isHealthy);
			currentState = isHealthy;
		}

		if (scheduledFuture == null) {
			if (isHealthy != currentState) {
				scheduledFuture = scheduledExecutor.schedule(new Runnable() {

					@Override
					public void run() {
						boolean update = false;
						synchronized (DebounceHealthMonitorStrategy.this) {
							update = scheduledFuture != null;
							scheduledFuture = null;
							if (update) {
								currentState = isHealthy;
							}
						}
						if (update) {
							adviseListeners(isHealthy);
						}


					}
				}, debouncePeriod, TimeUnit.MILLISECONDS);
			}
		}
		else {
			if (currentState == isHealthy) {
				scheduledFuture.cancel(false);
				scheduledFuture = null;
			}
		}
	}

	private ThreadFactory createThreadFactory() {
		return new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r,"Debounce health monitor");
				t.setDaemon(true);
				return t;
			}
		};
	}

}
