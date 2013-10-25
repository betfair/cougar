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

package com.betfair.cougar.tests.clienttests;

import org.testng.AssertJUnit;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

/**
 *
 */
public class ClientTestsHelper {
    public static Object[][] clientsToTest() {
        return new Object[][]{
                {CougarClientWrapper.TransportType.RESCRIPT},
                {CougarClientWrapper.TransportType.SECURE_RESCRIPT},
                {CougarClientWrapper.TransportType.CLIENT_AUTH_RESCRIPT},
                {CougarClientWrapper.TransportType.ASYNC_RESCRIPT},
                {CougarClientWrapper.TransportType.SECURE_ASYNC_RESCRIPT},
                {CougarClientWrapper.TransportType.CLIENT_AUTH_ASYNC_RESCRIPT},
                {CougarClientWrapper.TransportType.SOCKET},
                {CougarClientWrapper.TransportType.SECURE_SOCKET},
                {CougarClientWrapper.TransportType.CLIENT_AUTH_SOCKET}
        };
    }

    public static void assertEquals(Timestamp expected, Timestamp actual, long tolerance) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || actual == null) {
            AssertJUnit.assertEquals(expected, actual);
        }
        long actualTime = actual.getTime();
        long expectedTime = expected.getTime();
        long diff = Math.abs(actualTime - expectedTime);
        if (diff <= tolerance) {
            return;
        }
        AssertJUnit.assertEquals(expected, actual);

    }
}
