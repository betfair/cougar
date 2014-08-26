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

package com.betfair.cougar.logging.events;

import java.util.logging.Level;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.logging.records.CougarLogRecord;
import com.betfair.cougar.logging.records.EventLogRecord;
import com.betfair.cougar.logging.records.TraceLogRecord;

public class LogRecordTest extends CougarUtilTestCase {

	public void testLogFormattingSimple() {
		String msg = "MESSAGE";
		CougarLogRecord e = new CougarLogRecord("NAME", Level.INFO, msg) {};
		assertTrue(e.getMessage() == msg);
	}

	public void testLogFormattingComplex() {
		String msg = "String {}, float %1.2f, int %d";
		CougarLogRecord e = new CougarLogRecord("NAME", Level.INFO, msg, "string", 3.14, 100) {};
		assertTrue(e.getMessage().equals("String string, float 3.14, int 100"));
	}

	public void testEventSimple() throws Exception{
		final Object[] result = new Object[] {"foo", "bar"};
		LoggableEvent e = new LoggableEvent() {
			@Override
			public Object[] getFieldsToLog() {
				return result;
			}

            @Override
            public String getLogName() {
                return "EVENT-LOG-RECORD";
            }};
		assertEquals("foo,bar", new String(new EventLogRecord(e,null).getBytes()));
		assertEquals("foo,bar", new EventLogRecord(e,null).getMessage());
		assertTrue(new EventLogRecord(e,null).getLoggerName().equals("EVENT-LOG-RECORD"));

	}

	public void testTraceEvent() {
		TraceLogRecord e = new TraceLogRecord("UUID", "message %s", "foo");
        assertEquals("UUID: message foo", e.getMessage());
		assertEquals(e.getUUID(), "UUID" );
	}
	public void testLogMessageStored() {
		String msg = "MESSAGE";
		CougarLogRecord e = new CougarLogRecord("NAME", Level.INFO, msg) {};
		assertTrue(e.getMessage() == msg);
		e.setMessage("UNUSED");
		assertTrue(e.getMessage() == msg);
	}

	public void testLogNanoTime() throws Exception {
		long nanoStart = System.nanoTime();
		Thread.sleep(1);
		CougarLogRecord e = new CougarLogRecord("NAME", Level.INFO, "foo") {};
		Thread.sleep(1);
		long nanoEnd = System.nanoTime();
		assertTrue(nanoStart < e.getNanoTime());
		assertTrue(nanoEnd > e.getNanoTime());
	}

}


