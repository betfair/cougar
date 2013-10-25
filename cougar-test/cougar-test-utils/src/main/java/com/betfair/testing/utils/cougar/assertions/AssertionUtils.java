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

package com.betfair.testing.utils.cougar.assertions;


import com.betfair.testing.utils.cougar.misc.DataTypeEnum;
import com.betfair.testing.utils.cougar.misc.ObjectUtil;

import java.util.Date;

import static org.testng.AssertJUnit.*;

/**
 * Straight swap replacement for the JETT assertEquals class, but without all the JETT baggage..
 */
public class AssertionUtils {

    private static long dateTolerance = 2000;

    public static void multiAssertEquals(Object expected, Object actual) {
        jettAssertEquals(null, expected, actual);
    }

    public static void jettAssertEquals(String message, Object expected, Object actual) {
        if (actual == null) {
            assertNull(expected);
        }
        else {
            DataTypeEnum actualObjectType = ObjectUtil.resolveType(actual.getClass());
      		IAssertion asserter = AssertionProcessorFactory.getAssertionProcessor(actualObjectType);
      		asserter.execute(message, expected, actual, null);
        }
    }

    public static void actionFail(String s) {
        fail(s);
    }

    public static void actionPass(String s) {
        // nothing to do
    }

    public static void jettAssertNull(String s, Object actualValue) {
        assertNull(s, actualValue);
    }

    public static void jettAssertTrue(String s, boolean checkBehaviour) {
        assertTrue(s, checkBehaviour);
    }

    public static void jettAssertFalse(String s, boolean b) {
        assertFalse(s, b);
    }

    public static void actionException(Exception e) {
        throw new AssertionError(e);
    }

    public static void jettAssertDatesWithTolerance(String errorMessage, Date expectedValue, Date actualValue) {
        jettAssertDatesWithTolerance(errorMessage, expectedValue, actualValue, getDateTolerance());
    }

    public static void jettAssertDatesWithTolerance(String errorMessage, Date expectedValue, Date actualValue, long tolerance) {
        if (expectedValue == null) {
            assertNull(actualValue);
        }
        else {
            assertNotNull(actualValue);
            long expected = expectedValue.getTime();
            long actual = expectedValue.getTime();
            long diff = Math.abs(expected - actual);
            if (diff > tolerance) {
                if (errorMessage != null) {
                    fail(errorMessage);
                }
                else {
                    fail("Expected: "+expectedValue+" with a tolerance of "+tolerance+"ms, but got: "+actualValue);
                }
            }
        }
    }

    public static long getDateTolerance() {
        return dateTolerance;
    }

    public static long setDateTolerance(long dateTolerance) {
        long ret = AssertionUtils.dateTolerance;
        AssertionUtils.dateTolerance = dateTolerance;
        return ret;
    }
}
