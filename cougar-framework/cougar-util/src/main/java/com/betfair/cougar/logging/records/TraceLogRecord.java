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

package com.betfair.cougar.logging.records;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;


public class TraceLogRecord extends CougarLogRecord {
	private final String uUID;
    private static final String TRACE_LOG_FORMAT = "%1$s: %2$s";

	public TraceLogRecord(String uUID, String msg, Object... args) {
		super(null, Level.FINEST, String.format(TRACE_LOG_FORMAT, uUID, msg), args);
		this.uUID = uUID;
	}

	public TraceLogRecord(String uUID, String msg, Throwable exception) {
		super(null, Level.FINEST, exceptionMessageConstructor(uUID, msg, exception));
		setThrown(exception);
		this.uUID = uUID;
	}

	public String getUUID() {
		return uUID;
	}

    private static String exceptionMessageConstructor(String uUID, String msg, Throwable exception) {
        StringBuilder sb = new StringBuilder(String.format(TRACE_LOG_FORMAT, uUID, msg));
        if (exception != null) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            // Tack the exception on the end...
            sb.append(sw.toString());
        }
        return sb.toString();
    }
}
