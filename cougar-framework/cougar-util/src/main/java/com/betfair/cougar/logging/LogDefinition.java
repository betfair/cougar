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

package com.betfair.cougar.logging;


import com.betfair.cougar.logging.handlers.AbstractLogHandler;

public class LogDefinition {
	private AbstractLogHandler handler;
	private String logName;
	private boolean traceLog;
	
	public void setHandler(AbstractLogHandler handler) {
		this.handler = handler;
	}
	public void setLogName(String logName) {
		this.logName = logName;
	}
	public void setTraceLog(boolean traceLog) {
		this.traceLog = traceLog;
	}

	public void register() {
		if (traceLog) {
            CougarLogger cougarLogger = CougarLoggingUtils.getLogger(logName);
			CougarLoggingUtils.setTraceLogger(cougarLogger);
		}
	}

}
