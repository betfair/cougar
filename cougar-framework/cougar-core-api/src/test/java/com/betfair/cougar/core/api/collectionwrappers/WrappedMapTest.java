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

package com.betfair.cougar.core.api.collectionwrappers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import junit.framework.TestCase;

import com.betfair.cougar.api.Result;
import org.junit.BeforeClass;

public class WrappedMapTest extends TestCase {
	WrappedMap<String,String> ws = new WrappedMap<String,String>(new HashMap<String,String>());
	Map<String,String> anotherMap = new HashMap<String,String>();


	@Override
	protected void setUp() throws Exception {
		anotherMap.put("foo","bar");
		anotherMap.put("bar", "foo");
	}

	public void testAddAndRemove() {
		assertNull(ws.put("foo", "bar"));
		assertTrue(ws.containsKey("foo"));
		assertTrue(ws.containsValue("bar"));
		assertEquals("bar", ws.put("foo", "foo"));
		assertEquals("foo", ws.get("foo"));
		assertEquals("foo", ws.remove("foo"));
		ws.clear();

		ws.putAll(anotherMap);
		assertFalse(ws.isEmpty());
		assertEquals(2, ws.size());
		ws.clear();
		assertTrue(ws.isEmpty());

		ws.putAll(anotherMap);
		ws.put("pie", "mmm");

		assertEquals(3, ws.keySet().size());
		assertEquals(3, ws.values().size());
		assertEquals(3, ws.entrySet().size());
	}

	public void testIsResult() {
		assertTrue(ws instanceof Result);
	}
}
