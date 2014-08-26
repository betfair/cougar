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

package com.betfair.cougar.core.api;

import java.util.Date;


public class RequestTimer {
	long start = System.nanoTime();
	Date startTime = new Date();
	long end;
	Date endTime;
	boolean complete;

	public void requestComplete() {
		if (!complete) {
			end=System.nanoTime();
			endTime = new Date();
			complete = true;
		}
	}

	public Date getReceivedTime() {
		return startTime;
	}

	public long  getReceivedNano() {
		return start;
	}

	public Date getResponseTime() {
		checkComplete();
		return endTime;
	}

	public long getProcessTimeNanos() {
		checkComplete();
		return end-start;
	}

	private void checkComplete() {
		if (!complete) {
			throw new IllegalStateException("Request timer has not been flagged as complete");
		}
	}
}
