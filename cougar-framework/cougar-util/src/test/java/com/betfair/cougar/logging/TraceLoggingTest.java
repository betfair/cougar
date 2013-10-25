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

import com.betfair.cougar.CougarUtilTestCase;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TraceLoggingTest extends CougarUtilTestCase {
	private static final String TESTLOG = "";
	private static final String TRACELOG = "TRACER";
	
	private static final String MSG = "message";
	private static final Object O1 = "O1";
	
	private final LogRecord severeRecord = new LogRecord(Level.SEVERE, "SEVERE");
	
	private MockCapturingLogger logger;
	private MockCapturingLogger traceLog;
	
	public void setUp() throws Exception {
		super.setUp();
        CougarLoggingUtils.setTraceLogger(null);

		logger = (MockCapturingLogger)CougarLoggingUtils.getLogger(TESTLOG);
		LogDefinition hnd = new LogDefinition();
        hnd.setLogName(TESTLOG);
        hnd.register();
        logger.setLevel(Level.INFO);
        logger.getLogRecords().clear();

        
        traceLog = (MockCapturingLogger)CougarLoggingUtils.getLogger(TRACELOG);
        traceLog.setLevel(Level.FINEST);
        traceLog.getLogRecords().clear();
		LogDefinition hndTrace = new LogDefinition();
        hndTrace.setLogName(TRACELOG);
        hndTrace.setTraceLog(true);
        hndTrace.register();

    }
	
	public void testTraceCantSetAgain() {
		// Once the trace logger is set, it cannot be overwritted, except by null
		try {
			CougarLoggingUtils.setTraceLogger(logger);
			fail();
		} catch (IllegalStateException e) {}
	}

	
	public void testTraceOff() {
		// Trace messages are not written to the main log
		logger.log(Level.FINEST, MSG, O1);
		assertTrue(logger.getLogRecords().isEmpty());
		assertTrue(traceLog.getLogRecords().isEmpty());
	}

	public void testNoTraceLogSet() {
		CougarLoggingUtils.setTraceLogger(null);
		logger.log(Level.FINEST, MSG, O1);
		CougarLoggingUtils.startTracing("UUID");
		logger.log(Level.FINEST, MSG, O1);
		
		CougarLoggingUtils.stopTracing();
		logger.log(Level.FINEST, MSG, O1);

		traceLog.forceTrace("UUID", MSG);
		logger.log(Level.FINEST, MSG, O1);
		assertTrue(logger.getLogRecords().isEmpty());
		assertTrue(traceLog.getLogRecords().isEmpty());
	}
	
	public void testTraceOn() {
		logger.log(Level.FINEST, MSG, O1);
		assertTrue(traceLog.getLogRecords().isEmpty());
		assertTrue(logger.getLogRecords().isEmpty());
		
		CougarLoggingUtils.startTracing("UUID");
		
		// Trace messages are not written to the main log
		logger.log(Level.FINEST, MSG, O1);
		assertTrue(logger.getLogRecords().isEmpty());
		assertTrue(traceLog.getLogRecords().size() == 1);
		
		// However normal message are traced:
		logger.log(severeRecord);
		assertTrue(logger.getLogRecords().size() == 1);
		assertTrue(traceLog.getLogRecords().size() == 2);
		traceLog.getLogRecords().clear();

		CougarLoggingUtils.stopTracing();
		logger.log(Level.FINEST, MSG, O1);
		assertTrue(traceLog.getLogRecords().isEmpty());
	}
	
	public void testTraceIsThreadBased() throws Exception {
		assertTrue(traceLog.getLogRecords().isEmpty());
		assertTrue(logger.getLogRecords().isEmpty());
		
		CougarLoggingUtils.startTracing("UUID");
		
		// Trace messages are not written to the main log
		logger.log(Level.FINE, MSG, O1);
		assertTrue(logger.getLogRecords().isEmpty());
		assertTrue(traceLog.getLogRecords().size() == 1);
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				logger.log(Level.INFO, "NOT WRITTEN");
				
			}});
		t.start();
		t.join();

		assertTrue(traceLog.getLogRecords().size() == 1);
		traceLog.getLogRecords().clear();
		
		CougarLoggingUtils.stopTracing();
	}

	public void testTraceThreadBasedExplicitOverwrite() throws Exception {
		assertTrue(traceLog.getLogRecords().isEmpty());
		assertTrue(logger.getLogRecords().isEmpty());
		
		CougarLoggingUtils.startTracing("UUID");
		
		// Trace messages are not written to the main log
		logger.log(Level.FINEST, MSG, O1);
		assertTrue(logger.getLogRecords().isEmpty());
		assertTrue(traceLog.getLogRecords().size() == 1);
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// If we write explicitly to the trace log, it will always trace
				traceLog.forceTrace("UUID", "IS WRITTEN");
				
			}});
		t.start();
		t.join();

		assertTrue(traceLog.getLogRecords().size() == 2);
		traceLog.getLogRecords().clear();
		
		CougarLoggingUtils.stopTracing();
	}
}
