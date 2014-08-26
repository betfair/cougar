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

package com.betfair.cougar.core.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


public class ServiceVersionTest{

	@Test
	public void testServiceVersionNull() {
		try {
			new ServiceVersion(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("empty or null"));
		}
	}

	@Test
	public void testServiceVersionEmpty() {
		try {
			new ServiceVersion("");
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("empty or null"));
		}
	}

	@Test
	public void testServiceVersionTooLong() {
		try {
			new ServiceVersion("v1.2.3");
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("too many parts"));
		}
	}

	@Test
	public void testServiceVersionOK() {
		ServiceVersion sv = new ServiceVersion("v2.3");
		assertEquals(2, sv.getMajor());
		assertEquals(3, sv.getMinor());
	}
}
