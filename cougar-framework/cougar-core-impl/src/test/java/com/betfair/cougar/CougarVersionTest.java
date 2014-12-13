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

package com.betfair.cougar;

import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for @See CougarServerVersion
 */
public class CougarVersionTest {

    @Test
    public void test() {
        CougarVersion.init("/version/CougarVersionHappy.properties");
        String v = CougarVersion.getVersion();
        assertEquals("Versions do not agree", "2.3.4", v);
    }

    @Test
    public void testMajorMinor() {
        CougarVersion.init("/version/CougarVersionHappy.properties");
        String v = CougarVersion.getMajorMinorVersion();
        assertEquals("Versions do not agree", "2.3", v);
    }

    @Test
    public void testMajorMinorWithSnapshot() {
        CougarVersion.init("/version/CougarVersionSnapshot.properties");
        String v = CougarVersion.getMajorMinorVersion();
        assertEquals("Versions do not agree", "2.3", v);
    }

    @Test(expected = CougarFrameworkException.class)
    public void testMajorMinorNoPropertiesFile() {
        CougarVersion.init("/version/wibble.properties");
        CougarVersion.getMajorMinorVersion();
    }


}
