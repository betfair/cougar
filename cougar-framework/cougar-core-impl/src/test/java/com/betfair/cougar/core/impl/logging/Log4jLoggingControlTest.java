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

package com.betfair.cougar.core.impl.logging;

import org.apache.log4j.Level;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 */
public class Log4jLoggingControlTest {
    private Log4jLoggingControl classUnderTest = new Log4jLoggingControl();

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLogMapping() {
        classUnderTest.convertJdkLevelToLog4jLevel("Foo");
    }

    @Test
    public void testValidLogMappings() {
        //there is only one debug level in log4j, so fine and finer have to map to the same thing
        Level log4JLevelFiner = classUnderTest.convertJdkLevelToLog4jLevel(java.util.logging.Level.FINER.toString());
        Level log4JLevelFine  = classUnderTest.convertJdkLevelToLog4jLevel(java.util.logging.Level.FINE.toString());
        assertEquals(log4JLevelFine, Level.DEBUG);
        assertEquals("Log4j mapping for jdk.Fine should be equivalent to Finer", log4JLevelFine, log4JLevelFiner);

        //Log 4j has no concept of CONFIG, so maps to INFO
        Level log4JLevelConfig = classUnderTest.convertJdkLevelToLog4jLevel(java.util.logging.Level.CONFIG.toString());
        Level log4JLevelInfo   = classUnderTest.convertJdkLevelToLog4jLevel(java.util.logging.Level.INFO.toString());
        assertEquals(log4JLevelInfo, Level.INFO);
        assertEquals("Log4j mapping for jdk.Config should be equivalent to jdk.Info", log4JLevelConfig, log4JLevelInfo);

        //Test one other
        assertEquals("JDK.WARNING should be equivalent to log4j.WARN", Level.WARN,
                classUnderTest.convertJdkLevelToLog4jLevel(java.util.logging.Level.WARNING.toString()));

    }
}
