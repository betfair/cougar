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

import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.logging.records.ExceptionLogRecord;
import com.betfair.cougar.logging.records.SimpleLogRecord;
import com.betfair.cougar.logging.records.TrackingLogRecord;
import com.betfair.cougar.logging.records.TrackingLogRecord.Action;

public class LoggingTest extends CougarUtilTestCase {
	private static final String TESTLOG = "";
	private static final String MSG = "message";
	private static final Object O1 = "O1";
	private static final Object O2 = "O2";
	private static final Object O3 = "O3";
	private static final Exception EX = new Exception(MSG);
	
	private static final String CLASS = "class";
	private static final String METHOD = "method";
	
	private final LogRecord finestRecord = new LogRecord(Level.FINEST, "FINEST");
	
	private static final String SEPARATOR = System.getProperty("line.separator");
	
	private MockCapturingLogger logger;
	
	public void setUp() throws Exception {
		super.setUp();
		logger = (MockCapturingLogger)CougarLoggingUtils.getLogger(TESTLOG);
		LogDefinition hnd = new LogDefinition();
        hnd.setLogName(TESTLOG);
        hnd.register();
        
		logger.setLevel(Level.INFO);
        logger.getLogRecords().clear();
    }

	public void testIsLoggable() {
		logger.setLevel(Level.ALL);
		assertTrue(logger.isLoggable(Level.FINEST));
		
		logger.setLevel(Level.OFF);
		assertFalse(logger.isLoggable(Level.SEVERE));

		logger.setLevel(Level.INFO);
		assertTrue(logger.isLoggable(Level.INFO));
		assertFalse(logger.isLoggable(Level.CONFIG));
	}

	public void testLogLevelStringObjectArray() {
		logger.log(Level.INFO, MSG, O1, O2, O3);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(logger.getLogRecords().get(0) instanceof SimpleLogRecord);
		assertTrue(logger.getLogRecords().get(0).getLoggerName() == TESTLOG);
		assertTrue(logger.getLogRecords().get(0).getMessage().equals(MSG));
		assertTrue(logger.getLogRecords().get(0).getParameters().length == 3);
		assertTrue(logger.getLogRecords().get(0).getLevel() == Level.INFO);
	}

	public void testLogThrowable() {
		logger.log(EX);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(logger.getLogRecords().get(0) instanceof ExceptionLogRecord);
		assertTrue(logger.getLogRecords().get(0).getLoggerName() == TESTLOG);
		assertFalse(logger.getLogRecords().get(0).getMessage().equals(MSG));
		assertTrue(logger.getLogRecords().get(0).getThrown() == EX);
		assertTrue(logger.getLogRecords().get(0).getLevel() == Level.WARNING);
	}

	public void testLogLevelStringThrowable() {
		logger.log(Level.SEVERE, MSG, EX);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(logger.getLogRecords().get(0) instanceof ExceptionLogRecord);
		assertTrue(logger.getLogRecords().get(0).getLoggerName() == TESTLOG);
		assertTrue(logger.getLogRecords().get(0).getThrown() == EX);
		assertTrue(logger.getLogRecords().get(0).getMessage().equals(MSG));
		assertTrue(logger.getLogRecords().get(0).getLevel() == Level.SEVERE);
	}

	public void testEntering() {
		logger.entering(CLASS, METHOD, O1, O2);
		assertTrue(logger.getLogRecords().isEmpty());
		
		logger.setLevel(Level.FINER);
		logger.entering(CLASS, METHOD, O1, O2);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(logger.getLogRecords().get(0) instanceof TrackingLogRecord);
		assertTrue(((TrackingLogRecord)logger.getLogRecords().get(0)).getAction() == Action.ENTERING);
	}

	public void testExiting() {
		logger.exiting(CLASS, METHOD, O3);
		assertTrue(logger.getLogRecords().isEmpty());
		
		logger.setLevel(Level.FINER);
		logger.exiting(CLASS, METHOD, O3);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(logger.getLogRecords().get(0) instanceof TrackingLogRecord);
		assertTrue(((TrackingLogRecord)logger.getLogRecords().get(0)).getAction() == Action.LEAVING);
	}

	public void testThrowing() {
		logger.setLevel(Level.FINER);
		assertTrue(logger.getLogRecords().isEmpty());
		
		logger.setLevel(Level.FINER);
		logger.throwing(CLASS, METHOD, EX);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(logger.getLogRecords().get(0) instanceof TrackingLogRecord);
		assertTrue(((TrackingLogRecord)logger.getLogRecords().get(0)).getAction() == Action.THROWING);
	}



	public void testLogLogRecord() {
		logger.setLevel(Level.ALL);
		logger.log(finestRecord);
		assertTrue(logger.getLogRecords().size() == 1);
		logger.getLogRecords().clear();

		logger.setLevel(Level.FINER);
		logger.log(finestRecord);
		assertTrue(logger.getLogRecords().isEmpty());
	}

	public void testGetLoggers() {
		CougarLogger classBased = CougarLoggingUtils.getLogger(String.class);
		CougarLogger nameBased = CougarLoggingUtils.getLogger("java.lang.String");
		assertEquals(classBased, nameBased);
		assertEquals(classBased.getLogName(), "java.lang.String");
	}

	public void testPatternFormatterStandardEvent() {
		PatternFormatter fmt = new PatternFormatter("%4$s");
		LogRecord r = new LogRecord(Level.INFO, "I am a message %s");
		r.setParameters(new Object[] {"arg"});
		String msg = fmt.format(r);
		
		// No formatting is done on Standard Log Messages.
		assertTrue(("I am a message %s"+SEPARATOR).equals(msg)); 
	}
	
	public void testPatternFormatterCougarEvent() {
		PatternFormatter fmt = new PatternFormatter("%4$s");
		LogRecord r = new SimpleLogRecord(null, Level.INFO, "I am a message %s");
		r.setParameters(new Object[] {"arg"});
		String msg = fmt.format(r);
		
		// Formatting is done on Cougar Log events.
		assertTrue(("I am a message arg"+SEPARATOR).equals(msg)); 
	}
	
	public void testPatternFormatterCougarException() {
		PatternFormatter fmt = new PatternFormatter("%4$s");
		LogRecord r = new ExceptionLogRecord(null, Level.INFO, "I am an exception", new RuntimeException());
		r.setParameters(new Object[] {"arg"});
		String msg = fmt.format(r);
		
		// Formatting is done on Cougar Log events.
		assertTrue(msg.startsWith("I am an exception"));
		assertTrue(msg.contains("RuntimeException"));
		assertTrue(msg.contains("\tat "));
		
	}

}

