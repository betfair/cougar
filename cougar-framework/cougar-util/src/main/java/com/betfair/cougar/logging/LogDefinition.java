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

package com.betfair.cougar.logging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDefinition {
	private String logName;
	private boolean traceLog;

	public void setLogName(String logName) {
		this.logName = logName;
	}
	public void setTraceLog(boolean traceLog) {
		this.traceLog = traceLog;
	}

	public void register() {
		if (traceLog) {
            Logger traceLogger = LoggerFactory.getLogger(logName);
			CougarLoggingUtils.setTraceLogger(traceLogger);
		}
	}

}
