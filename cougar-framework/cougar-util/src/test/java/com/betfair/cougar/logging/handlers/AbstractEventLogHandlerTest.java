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
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.logging.records.EventLogRecord;


public class AbstractEventLogHandlerTest extends CougarUtilTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
    }
	

    public void testConcrete() throws Exception {
		MyEventLogHandler handler = new MyEventLogHandler(false);
		try {
			handler.clone("ns","Service1");
			fail();
		} catch (IllegalStateException e) {
			// OK - can't clone a non abstract handler 
		}
		handler.close();
	}
	
    public void testConcreteGeneration() throws Exception {
		MyEventLogHandler abstractHandler = new MyEventLogHandler(true);
		MyEventLogRecord foobar = new MyEventLogRecord();
		
		try {
            // Can't publish to an abstract handler
			abstractHandler.publish(new EventLogRecord(foobar, null));
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}
		
		AbstractEventLogHandler concreteHandler = abstractHandler.clone("ns","Service1");
		try {
            // Can't publish a standard record to an event log
			concreteHandler.publish(new LogRecord(Level.ALL, "FOO"));
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}
		
		try {
            // Check that an IO failure in the handler reports OK
			concreteHandler.publish(new EventLogRecord(foobar, null));
			fail();
		} catch (IllegalStateException e) {
            assertEquals("SIMULATED IO FAILURE", e.getCause().getMessage());
			// OK
		}
	}
	
	
	private static final class MyEventLogRecord implements LoggableEvent {

		@Override
		public Object[] getFieldsToLog() {
			return null;
		}

        @Override
        public String getLogName() {
            return "foo";
        }
		
	}
	
	private static final class MyEventLogHandler extends AbstractEventLogHandler {

		public MyEventLogHandler(boolean abstractHandler) {
			super(abstractHandler);
		}

		@Override
		protected AbstractEventLogHandler cloneHandlerToName(String namespace, String name)
				throws IOException {
			return new MyEventLogHandler(false);
		}

		@Override
		public void publishEvent(EventLogRecord event) throws IOException {
			throw new IOException("SIMULATED IO FAILURE");
			
		}

		@Override
		public void close() throws SecurityException {
			
		}

		@Override
		public void flush() {
			
		}
		
	}
}
