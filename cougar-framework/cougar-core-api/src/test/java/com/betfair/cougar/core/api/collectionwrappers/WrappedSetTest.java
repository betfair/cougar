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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import com.betfair.cougar.api.Result;

public class WrappedSetTest extends TestCase {
	WrappedSet<String> ws = new WrappedSet<String>(new HashSet<String>());
	Set<String> anotherSet = new HashSet<String>();


	@Override
	protected void setUp() throws Exception {
		anotherSet.add("foo");
		anotherSet.add("bar");
	}

	public void testAddAndRemove() {
		assertTrue(ws.add("foo"));
		assertTrue(ws.contains("foo"));
		assertFalse(ws.add("foo"));

		assertFalse(ws.containsAll(anotherSet));
		assertTrue(ws.addAll(anotherSet));

		assertFalse(ws.isEmpty());
		ws.clear();
		assertTrue(ws.isEmpty());
		assertEquals(0, ws.size());

		assertTrue(ws.addAll(anotherSet));
		assertTrue(ws.add("pie"));
		assertTrue(ws.removeAll(anotherSet));
		assertEquals(1, ws.size());
		assertTrue(ws.remove("pie"));
		assertTrue(ws.isEmpty());

		assertTrue(ws.addAll(anotherSet));
		assertTrue(ws.add("pie"));
		ws.retainAll(anotherSet);
		assertFalse(ws.contains("pie"));
		assertEquals(2, ws.size());

		Iterator<String> it = ws.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());

		Object[] o = ws.toArray();
		String[] s = ws.toArray(new String[ws.size()]);
		assertEquals(o.length, s.length);

		for (int i = 0; i < s.length; i++) {
			assertTrue(o[i] == s[i]);
		}
	}

	public void testIsResult() {
		assertTrue(ws instanceof Result);
	}
}
