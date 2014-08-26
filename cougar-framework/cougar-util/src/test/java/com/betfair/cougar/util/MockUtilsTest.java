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

package com.betfair.cougar.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.Result;
import com.betfair.cougar.api.fault.FaultCode;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class MockUtilsTest {

    @Test
	public void testMockSimple() {
		MyResult result = MockUtils.generateMockResponse(MyResult.class, mock(RequestContext.class));
		checkResult(result);
	}

    @Test
	public void testMockCollections() {
		MyResultWithCollections resultList = MockUtils.generateMockResponse(MyResultWithCollections.class, mock(RequestContext.class));
		assertEquals("String", resultList.foo.substring(0, 6));
		for (MyResult r: resultList.list) {
			checkResult(r);
		}
		for (String s: resultList.set) {
			assertEquals("String", s.substring(0, 6));
		}

		for (Map.Entry<String, MyResult> me: resultList.map.entrySet()) {
			assertEquals("String", me.getKey().substring(0, 6));
			checkResult(me.getValue());
		}

	}

	private void checkResult(MyResult result) {
		assertTrue(result.byteVar.intValue() > 0);
		assertTrue(result.shortVar.intValue() > 0);
		assertTrue(result.charVar.charValue() > 0);
		assertTrue(result.intVar.intValue() > 0);
		assertTrue(result.longVar.intValue() > 0);
		assertTrue(result.floatVar.intValue() > 0);
		assertTrue(result.doubleVar.intValue() > 0);
		assertEquals("String", result.stringVar.substring(0, 6));
		assertNotNull(result.enumVal);
		assertNotNull(result.booleanVar);
	}
    @Test
	public void testMockBadField() {
		String foo = MockUtils.generateMockResponse(String.class, mock(RequestContext.class));
		assertEquals("String-1", foo);
	}
	public static class MyResult implements Result {
		Byte byteVar;
		Short shortVar;
		Character charVar;
		Integer intVar;
		Long longVar;
		Float floatVar;
		Double doubleVar;
		Boolean booleanVar;
		String stringVar;
		FaultCode enumVal;
		public void setByteVar(Byte byteVar) {
			this.byteVar = byteVar;
		}
		public void setShortVar(Short shortVar) {
			this.shortVar = shortVar;
		}
		public void setCharVar(Character charVar) {
			this.charVar = charVar;
		}
		public void setIntVar(Integer intVar) {
			this.intVar = intVar;
		}
		public void setLongVar(Long longVar) {
			this.longVar = longVar;
		}
		public void setFloatVar(Float floatVar) {
			this.floatVar = floatVar;
		}
		public void setDoubleVar(Double doubleVar) {
			this.doubleVar = doubleVar;
		}
		public void setBooleanVar(Boolean booleanVar) {
			this.booleanVar = booleanVar;
		}
		public void setStringVar(String stringVar) {
			this.stringVar = stringVar;
		}
		public void setEnumVal(FaultCode enumVal) {
			this.enumVal = enumVal;
		}
	}

	public static class MyResultWithCollections implements Result {
		String foo;
		List<MyResult> list;
		Set<String> set;
		Map<String, MyResult> map;

		public void setSet(Set<String> set) {
			this.set = set;
		}
		public void setMap(Map<String, MyResult> map) {
			this.map = map;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
		public void setList(List<MyResult> list) {
			this.list = list;
		}
		public void setBar(MyResult bar) {
			this.bar = bar;
		}
		MyResult bar;
	}
}
