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

package com.betfair.cougar.test;

import com.betfair.cougar.core.api.RequestTimer;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.configuration.PropertyConfigurer;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;


public abstract class CougarTestCase extends TestCase {

    private final Map<String, String> props = new TreeMap<String, String>();

    protected CougarTestCase() {
    	super();
    }

	protected CougarTestCase(String name) {
		super(name);
	}

    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        props.put("property.one", "YES");
        props.put("property.two", "NO");
        props.put("property.overridden", "INVISIBLE");
        Field f = PropertyConfigurer.class.getDeclaredField("allLoadedProperties");
        f.setAccessible(true);
        f.set(null, props);

        System.setProperty("property.overridden", "OVERRIDE");
    }

    @After
    protected void tearDown() throws Exception {
        props.clear();
	}

    protected void assertEqualsArray(Object[] expected, Object[] actual) {
		if (expected.length != actual.length) {
			fail("Lengths differ - expected was "+expected.length+", actual "+actual.length);
		}

		for (int i=0; i<expected.length; ++i) {
			assertEquals(expected[i], actual[i]);
		}
	}

    protected void assertEqualsArray(byte[] expected, byte[] actual) {
		if (expected.length != actual.length) {
			fail("Lengths differ - expected was "+expected.length+", actual "+actual.length);
		}

		for (int i=0; i<expected.length; ++i) {
			assertEquals(expected[i], actual[i]);
		}
	}

}
