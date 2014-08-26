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


/**
 * A strategy that requires the same health status be received a configurable number of times before firing the change to it's listeners
 */
public class CountingHealthMonitorStrategy extends AbstractHealthMonitorStrategy {


	private Boolean currentState;
	private int     requiredUpdateCount;
	private int 	changesReceived;


	public CountingHealthMonitorStrategy(int requiredUpdateCount) {
		this.requiredUpdateCount = requiredUpdateCount;
	}

	@Override
	public synchronized void update(boolean isHealthy) {
		if (currentState == null) {
			//first update received, pass it on
			adviseListeners(isHealthy);
			currentState = isHealthy;
		}
		else if (isHealthy != currentState) {
			if (++changesReceived >= requiredUpdateCount) {
				adviseListeners(isHealthy);
				currentState = isHealthy;
				changesReceived = 0;
			}

		}
		else {
			changesReceived = 0;
		}

	}

}
