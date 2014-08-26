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

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public abstract class CougarLogRecord extends LogRecord {

    private static final long serialVersionUID = 1L;

	private final long nanoTime;
	private final AtomicReference<String> formattedMessage = new AtomicReference<String>();

	public CougarLogRecord(String logName, Level level, String msg, Object... args) {
        super(level, msg);
        nanoTime = System.nanoTime();
        setParameters(args);
        setLoggerName(logName);
    }

    public long getNanoTime() {
		return nanoTime;
	}

    public String getMessage() {
    	// Store the formatted message in a AtomicReference in case we're logging to more than
    	// one handler. (AtomicReference in case we're logging asynchronously)
    	String msg = formattedMessage.get();
    	if (msg == null) {
	    	Object[] params = getParameters();
	        if (params != null && params.length > 0) {
	            msg = String.format(super.getMessage().replace("{}","%s"), params);
	        } else {
	        	msg = super.getMessage();
	        }
	        formattedMessage.set(msg);
    	}
    	return msg;
    }
}
