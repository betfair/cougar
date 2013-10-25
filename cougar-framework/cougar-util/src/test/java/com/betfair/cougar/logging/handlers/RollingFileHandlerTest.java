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
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.betfair.cougar.CougarUtilTestCase;

/**
 * RollingFileHandler Tester.
 */
public class RollingFileHandlerTest extends CougarUtilTestCase {

    private static Formatter SIMPLE_FMT = new Formatter() {
			public String format(LogRecord record) {
				return record.getMessage() + SEPARATOR;
			}
    };

    private static final String FILENAME = "RollingFileHandlerTest.log";

    public void setUp() throws Exception {
        super.setUp();
        new File(FILENAME).delete();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        Thread.sleep(10);
        new File(FILENAME).delete();
    }

    public void testConstructorMinutePolicy() throws Exception {
        RollingFileHandler rfh = new RollingFileHandler(FILENAME, false, true, "MINUTE", SIMPLE_FMT);
        try {
            rfh.publish(new LogRecord(Level.INFO, "foo"));
            validateFileContents(FILENAME, "foo", false);
        } finally {
            rfh.close();
        }
    }

    public void testConstructorHourPolicy() throws Exception {
        RollingFileHandler rfh = new RollingFileHandler(FILENAME, false, true, "HOUR", SIMPLE_FMT);
        try {
            rfh.publish(new LogRecord(Level.INFO, "foo"));
            validateFileContents(FILENAME, "foo", false);
        } finally {
            rfh.close();
        }
    }

    public void testConstructorDayPolicy() throws Exception {
        RollingFileHandler rfh = new RollingFileHandler(FILENAME, false, true, "DAY", SIMPLE_FMT);
        try {
            rfh.publish(new LogRecord(Level.INFO, "foo"));
            validateFileContents(FILENAME, "foo", false);
        } finally {
            rfh.close();
        }
    }

    public void testConstructorMonthPolicy() throws Exception {
        RollingFileHandler rfh = new RollingFileHandler(FILENAME, false, true, "MONTH", SIMPLE_FMT);
        try {
            rfh.publish(new LogRecord(Level.INFO, "foo"));
            validateFileContents(FILENAME, "foo", false);
        } finally {
            rfh.close();
        }
    }

    public void testConstructorNoFile() throws Exception {
        try {
            new RollingFileHandler(null, true, true, "MONTH", SIMPLE_FMT);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testConstructorBadFile() throws Exception {
        try {
            new RollingFileHandler("", true, true, "MONTH", SIMPLE_FMT);
            fail();
        } catch (IOException e) {}
    }

    public void testConstructorNoPolicy() throws Exception {
        try {
            new RollingFileHandler("foo", true, true, null, SIMPLE_FMT);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testConstructorInvalidPolicy() throws Exception {
        try {
            new RollingFileHandler("foo", true, true, "FOO", SIMPLE_FMT);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testConstructorNoFormatter() throws Exception {
        try {
            new RollingFileHandler("foo", true, true, "MONTH", null);
            fail();
        } catch (IllegalArgumentException e) {}
    }
}
