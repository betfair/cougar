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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;

import com.betfair.cougar.api.Result;


public class WrappedListTest extends TestCase {
	WrappedList<String> wl = new WrappedList<String>(new ArrayList<String>());
	List<String> anotherList = new ArrayList<String>();


	@Override
	protected void setUp() throws Exception {
		anotherList.add("foo");
		anotherList.add("bar");
	}

	public void testAddAndRemove() {
		assertTrue(wl.add("foo"));
		assertTrue(wl.contains("foo"));
		wl.add(0, "foo");

		assertFalse(wl.containsAll(anotherList));
		assertTrue(wl.addAll(anotherList));
		assertTrue(wl.addAll(2, anotherList));

		assertFalse(wl.isEmpty());
		wl.clear();
		assertTrue(wl.isEmpty());
		assertEquals(0, wl.size());

		assertTrue(wl.addAll(anotherList));
		assertTrue(wl.add("pie"));

		assertEquals(2, wl.subList(1, 3).size());

		assertTrue(wl.removeAll(anotherList));
		assertEquals(1, wl.size());
		assertTrue(wl.remove("pie"));
		assertTrue(wl.isEmpty());

		assertTrue(wl.addAll(anotherList));
		assertTrue(wl.add("pie"));
		wl.retainAll(anotherList);
		assertFalse(wl.contains("pie"));
		assertEquals(2, wl.size());

		assertEquals("foo", wl.get(0));
		assertEquals(0, wl.indexOf("foo"));
		assertTrue(wl.add("foo"));
		assertEquals(2, wl.lastIndexOf("foo"));
		wl.set(2, "pie");
		assertEquals("pie", wl.get(2));
		wl.remove(2);

		Iterator<String> it = wl.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());

		ListIterator<String> lit = wl.listIterator();
		assertTrue(lit.hasNext());
		lit.next();
		assertTrue(lit.hasNext());
		lit.next();
		assertFalse(lit.hasNext());

		lit = wl.listIterator(1);
		assertTrue(lit.hasNext());
		lit.next();
		assertFalse(lit.hasNext());

		Object[] o = wl.toArray();
		String[] s = wl.toArray(new String[wl.size()]);
		assertEquals(o.length, s.length);

		for (int i = 0; i < s.length; i++) {
			assertTrue(o[i] == s[i]);
		}
	}

	public void testIsResult() {
		assertTrue(wl instanceof Result);
	}

}
