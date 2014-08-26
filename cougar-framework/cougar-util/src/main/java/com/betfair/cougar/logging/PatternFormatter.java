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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.betfair.cougar.logging.records.CougarLogRecord;
import com.betfair.cougar.logging.records.TraceLogRecord;

/**
 * Format a log record using a <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/">java format</a> string.
 * parameters passed in are:
 * 	1. Time of the message (long)
 * 	2. Log level (String)
 * 	3. Logger Name (String)
 * 	4. Message (String)
 * 	5. sequence number (long)
 * 	6. Thread id (String)
 *  7. Nano Time (long)
 *  8. Request UUID (String)
 */
public class PatternFormatter extends Formatter {
	private static final String SEPARATOR = System.getProperty("line.separator");
	private final String pattern;

	public PatternFormatter(String pattern) {
		this.pattern = pattern + SEPARATOR;
	}

	public String format(LogRecord record) {
		// Set up default values
		String message = record.getMessage();
		if (record.getThrown() != null) {
			StringWriter sw = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(sw));
			// Tack the exception on the end...
			return formatRecord(record, message) + sw.toString() + SEPARATOR;
		} else {
			return formatRecord(record, message);
		}
	}

	private String formatRecord(LogRecord record, String message) {
        long nanoTime = (record instanceof CougarLogRecord) ? ((CougarLogRecord)record).getNanoTime() : 0;
        String uUid = (record instanceof TraceLogRecord) ? ((TraceLogRecord)record).getUUID() : "";
		return String.format(pattern,
				record.getMillis(), // 1
				record.getLevel().getName(), // 2
				record.getLoggerName(),  // 3
				message,  // 4
				record.getSequenceNumber(), // 5
				record.getThreadID(), // 6
                nanoTime, // 7
                uUid // 8
			);
	}

}
