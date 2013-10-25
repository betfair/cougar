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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test {@link CougarLogManager}.
 */
public class CougarLogManagerTest {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream ps = new PrintStream(baos);
    private CougarLogManager em;

    @Before
    public void setUp() throws Exception {
        // bypass the singleton
        Constructor<CougarLogManager> c = CougarLogManager.class.getDeclaredConstructor();
        c.setAccessible(true);
        em = c.newInstance();
        System.setErr(ps);
    }

    @After
    public void tearDown() {
        System.setErr(System.err);
    }

    private long getNumberMessageFromString(String logMsg) {
        return Long.parseLong(logMsg.substring(logMsg.indexOf("-") + 2));
    }

    @Test
    public void testSetAndGetMessageInterval() {
        em.setMessageInterval(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE,em.getMessageInterval());
    }

    /**
     * At least n millis between errors to stderr.
     */
    @Test
    public void testSetMessageInterval() {

        final long[] times = {99, 101, 150, 199, 200, 296, 297, 298, 299, 401, 499, 500, 501};

        // these will be logged. Note that the counter resets after the *last* message set so
        //	401 is logged, then *not* 499 or 500, but 501 will be
        final long[] msgs = {99, 199, 299, 401, 501};
        CougarLogManager lm = new TestCougarLogManager(times);

        lm.setMessageInterval(100);

        for (long time : times) {
            lm.error(String.valueOf(time), null, 0);
        }
        assertEquals("Not all errors were counted", times.length, lm.getNumErrors());

        ps.flush();
        String[] errors = new String(baos.toByteArray()).split(System.getProperty("line.separator"));
        assertEquals("Incorrect number of errors: ", msgs.length, errors.length);
        for (int i = 0; i < msgs.length; ++i) {
            assertEquals("Wrong message printed", msgs[i], getNumberMessageFromString(errors[i]));
        }
    }

    @Test
    public void testSetMaxStoredErrors() {
        em.setMaxStoredErrors(5);

        for (int i = 0; i < 5; i++) {
            em.error(String.valueOf(i), null, 0);
            assertEquals(1 + i, em.getStoredErrors().length);
        }

        em.error(String.valueOf(99), null, 0);

        assertEquals(5, em.getMaxStoredErrors());
        assertEquals(5, em.getStoredErrors().length);
        assertEquals(1, getNumberMessageFromString(em.getStoredErrors()[0]));
        assertEquals(99, getNumberMessageFromString(em.getStoredErrors()[4]));

        em.setMaxStoredErrors(2);
        em.error(String.valueOf(100), null, 0);
        assertEquals(2, em.getStoredErrors().length);
        assertEquals(99, getNumberMessageFromString(em.getStoredErrors()[0]));
        assertEquals(100, getNumberMessageFromString(em.getStoredErrors()[1]));
    }

    @Test
    public void testException() {
        em.error("FOO", new RuntimeException("BAR"), 0);

        assertEquals(1, em.getStoredErrors().length);
        String str = em.getStoredErrors()[0];
        assertTrue(str.contains("FOO"));
        assertTrue(str.contains("BAR"));
        assertTrue(str.contains("\tat"));
        assertTrue(str.contains("RuntimeException"));

    }

    @Test
    public void testClear() {
        for (int i = 0; i < 5; i++) {
            em.error(String.valueOf(i), null, 0);
        }
        assertEquals(5, em.getStoredErrors().length);
        assertEquals(5, em.getNumErrors());

        em.clear();
        assertEquals(0, em.getStoredErrors().length);
        assertEquals(0, em.getNumErrors());

    }

    @Test
    public void testLogDirectory() {
        em.setBaseLogDirectory("foo");
        File baseDir = new File("foo");
        assertEquals(baseDir.getAbsolutePath(), em.getBaseLogDirectory());
    }

    private class TestCougarLogManager extends CougarLogManager {
        private final long[] times;
        int i = 0;

        private TestCougarLogManager(long[] times) {
            this.times = times;
        }

        @Override
        protected long nanoTime() {
            return times[i++] * 1000000;
        }
    }
}
