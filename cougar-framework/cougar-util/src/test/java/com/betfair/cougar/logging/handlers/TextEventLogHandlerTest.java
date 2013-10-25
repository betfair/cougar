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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.logging.records.EventLogRecord;
import com.betfair.cougar.logging.records.Loggable;


public class TextEventLogHandlerTest extends CougarUtilTestCase {
    private static final String FILENAME = "TextEventLogHandlerTest.log";
    private static final String FILENAME_TEMPLATE = "TextEventLogHandlerTest-##NAME##.log";
    private static final String FILENAME_TEMPLATE_NS = "TextEventLogHandlerTest-##NAME####NAMESPACE##.log";
    private static final String FILENAME1 = "TextEventLogHandlerTest-Service1.log";
    private static final String FILENAME2 = "TextEventLogHandlerTest-Service2.log";
    private static final String FILENAME_NS = "TextEventLogHandlerTest-Service1-NS.log";

    public void setUp() throws Exception {
        super.setUp();
        deleteFiles();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        Thread.sleep(10);
        deleteFiles();
    }
	
    private void deleteFiles() {
        new File(FILENAME).delete();
        new File(FILENAME1).delete();
        new File(FILENAME2).delete();
        new File(FILENAME_NS).delete();
    }

    public void testConcrete() throws Exception {
		TextEventLogHandler handler = new TextEventLogHandler(FILENAME, true, false, "MONTH", false);
		handler.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"foo", "bar", new LoggableField("LOGGABLE"), new Date(0), null, new Date(0), true, false}), null));
		validateFileContents(FILENAME, "foo,bar,LOGGABLE,1970-01-01 00:00:00.000,,1970-01-01 00:00:00.000,Y,N", false);
		
		handler.close();
	}
	
    public void testConcreteNoFlush() throws Exception {
		TextEventLogHandler handler = new TextEventLogHandler(FILENAME, false, false, "MONTH", false);
		handler.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"foo", "bar", new LoggableField("LOGGABLE"), new Date(0), null, new Date(0)}), null));
		handler.flush();
		validateFileContents(FILENAME, "foo,bar,LOGGABLE,1970-01-01 00:00:00.000,,1970-01-01 00:00:00.000", false);
		
		handler.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"foo", "bar"}), null));
		validateFileContents(FILENAME, "foo,bar,LOGGABLE,1970-01-01 00:00:00.000,,1970-01-01 00:00:00.000", false);
		handler.flush();
		validateFileContents(FILENAME, "foo,bar", false);

		handler.close();
	}
    
    public void testConcreteWithList() throws Exception {
		TextEventLogHandler handler = new TextEventLogHandler(FILENAME, true, false, "MONTH", false);
		List<Date> dates = new ArrayList<Date>();
		dates.add(new Date(0));
		dates.add(new Date(1));
		dates.add(new Date(2));
		handler.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"foo", "bar", dates}), null));
		handler.flush();
		validateFileContents(FILENAME, "foo,bar,[1970-01-01 00:00:00.000,1970-01-01 00:00:00.001,1970-01-01 00:00:00.002]", false);
		
		handler.close();
	}

    
    public void testConcreteWithArray() throws Exception {
		TextEventLogHandler handler = new TextEventLogHandler(FILENAME, true, false, "MONTH", false);
		Loggable[] fields = new Loggable[3];
		fields[0] = new LoggableField("ONE");
		fields[1] = new LoggableField("TWO");
		fields[2] = new LoggableField("THREE");

		handler.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"foo", "bar", fields}), null));
		handler.flush();
		validateFileContents(FILENAME, "foo,bar,[ONE,TWO,THREE]", false);
		
		handler.close();
	}

    
    public void testConcreteGeneration() throws Exception {
        checkFilesDoNotExist();
		TextEventLogHandler abstractHandler = new TextEventLogHandler(FILENAME_TEMPLATE, true, false, "MONTH", true);
		assertFalse(new File(FILENAME_TEMPLATE).exists());

		TextEventLogRecord foobar = new TextEventLogRecord(new Object[] {"foo", "bar"});
		
		AbstractEventLogHandler handler1 = abstractHandler.clone(null,"Service1");
		AbstractEventLogHandler handler2 = abstractHandler.clone("foo","Service2");

		handler1.publish(new EventLogRecord(foobar, null));
		handler2.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"bar", "foo"}), null));
		handler1.close();
		handler2.close();
		
		validateFileContents(FILENAME1, "foo,bar", false);
		validateFileContents(FILENAME2, "bar,foo", false);
		
		
	}
	
	
    public void testConcreteGenerationWithNamepsace() throws Exception {
        checkFilesDoNotExist();
		TextEventLogHandler abstractHandler = new TextEventLogHandler(FILENAME_TEMPLATE_NS, true, false, "MONTH", true);
		assertFalse(new File(FILENAME_TEMPLATE_NS).exists());
		TextEventLogRecord foobar = new TextEventLogRecord(new Object[] {"foo", "bar"});

		AbstractEventLogHandler handler1 = abstractHandler.clone("NS","Service1");
		AbstractEventLogHandler handler2 = abstractHandler.clone(null,"Service1");

		handler1.publish(new EventLogRecord(foobar, null));
		handler2.publish(new EventLogRecord(new TextEventLogRecord(new Object[] {"bar", "foo"}), null));
		handler1.close();
		handler2.close();

		validateFileContents(FILENAME_NS, "foo,bar", false);
		validateFileContents(FILENAME1, "bar,foo", false);


	}

    private void checkFilesDoNotExist() throws Exception {
        String[] fileNames = {FILENAME, FILENAME1, FILENAME2, FILENAME_NS};
        for (String fName: fileNames) {
            File f = new File(fName);
            int count = 0;
            while (f.exists()) {
                if (count++ > 10) {
                    throw new IllegalStateException(("Could not delete "+fName));
                } else {
                    Thread.sleep(100);
                }
                f.delete();
            }
        }
    }
	private static final class TextEventLogRecord implements LoggableEvent {
		private Object[] result;
		
		public TextEventLogRecord(Object[] result) {
			this.result = result;
		}

		@Override
		public Object[] getFieldsToLog() {
			return result;
		}

        @Override
        public String getLogName() {
            return "logName";
        }
		
	}
	
	private static final class LoggableField implements Loggable {
		private String data;
		
		public LoggableField(String data) {
			this.data = data;
		}

		@Override
		public void writeTo(StringBuilder record) {
			record.append(data);
			
		}

		@Override
		public void writeTo(OutputStream stream) throws IOException {
			stream.write(data.getBytes());
		}
		
	}
}
