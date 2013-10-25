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

package com.betfair.cougar.logging.handlers;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.logging.records.TraceLogRecord;

public class BufferingTraceHandler extends RollingFileHandler {
	final static CougarLogger logger = CougarLoggingUtils.getLogger(BufferingTraceHandler.class);
	
	public BufferingTraceHandler(String fileName, boolean append, String policy, Formatter formatter) throws IOException {
		// Now initialise the underlying handler. Any name will do.
		super(fileName, append, false /* do not auto flush */, policy, formatter);

	}
    
	@Override
	public void publish(LogRecord record) {
		if (record instanceof TraceLogRecord) {
			super.publish(record);
		}
	}

}
