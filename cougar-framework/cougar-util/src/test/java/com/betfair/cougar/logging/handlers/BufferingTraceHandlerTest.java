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

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.logging.PatternFormatter;
import com.betfair.cougar.logging.records.SimpleLogRecord;
import com.betfair.cougar.logging.records.TraceLogRecord;

public class BufferingTraceHandlerTest extends CougarUtilTestCase {
	private static final String FILENAME = "test-log.log";
    private static final String LS = System.getProperty("line.separator");
	private BufferingTraceHandler handler;


    private static Formatter NO_FORMAT = new Formatter() {
        public String format(LogRecord record) {
            // The message is pre-formatted.
            return record.getMessage() + LS;
        }
    };


	public void setUp() throws Exception {
		super.setUp();
		
		handler = new BufferingTraceHandler(FILENAME, false, "MONTH", NO_FORMAT);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler.close();
	}


	public void testFlushAndClose() throws Exception {
		TraceLogRecord tlr = new TraceLogRecord("UUID", "MESSAGE");
		handler.publish(tlr);
		handler.flush();
		handler.close();
		validateFileContents(FILENAME, "UUID: MESSAGE", false);
	}

	public void testSimpleMessage() throws Exception {
		TraceLogRecord tlr = new TraceLogRecord("UUID", "MESSAGE");
		handler.publish(tlr);
		handler.flush();
		validateFileContents(FILENAME, "UUID: MESSAGE", false);
	}

	public void testMessageWithArgs() throws Exception {
		TraceLogRecord tlr = new TraceLogRecord("UUID", "MESSAGE %s", "ONE");
		handler.publish(tlr);
		handler.flush();
		validateFileContents(FILENAME, "UUID: MESSAGE ONE", false);
	}

	public void testMessageWithException() throws Exception {
		TraceLogRecord tlr = new TraceLogRecord("UUID", "EXCEPTION", new RuntimeException("RTEX"));
		handler.publish(tlr);
		handler.flush();
		validateFileContents(FILENAME, "UUID: EXCEPTION", true);
        validateFileContents(FILENAME, "java.lang.RuntimeException: RTEX", true);
        validateFileContents(FILENAME, "	at com.betfair.cougar.logging.handlers.BufferingTraceHandlerTest.testMessageWithException", true);
	}
	public void testNoLogNonTraceMessages() throws Exception {
		SimpleLogRecord slr = new SimpleLogRecord("TESTLOG", Level.FINE, "message");
		handler.publish(slr);
		handler.flush();
		validateFileContents(FILENAME, null, true);

	}
}
