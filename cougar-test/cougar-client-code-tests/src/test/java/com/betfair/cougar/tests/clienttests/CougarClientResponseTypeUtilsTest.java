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

import com.betfair.baseline.v2.enumerations.SimpleEnum;
import com.betfair.baseline.v2.to.BodyParamByteObject;
import com.betfair.baseline.v2.to.ByteOperationResponseObject;
import com.betfair.baseline.v2.to.ComplexObject;
import com.betfair.baseline.v2.to.SomeComplexObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CougarClientResponseTypeUtilsTest {
	
	@Test
	public void buildMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Map<String, String> returnedMap = utils.buildMap("1,2,3,4","a,b,c,d");
		
		assertEquals("a", returnedMap.get("1"));
		assertEquals("b", returnedMap.get("2"));
		assertEquals("c", returnedMap.get("3"));
		assertEquals("d", returnedMap.get("4"));		
	}
	
	@Test
	public void buildIntMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Map<Integer, Integer> returnedMap = utils.buildIntMap("1,2,3,4","11,22,33,44");
		
		assertEquals(Integer.valueOf(11), returnedMap.get(Integer.valueOf(1)));
		assertEquals(Integer.valueOf(22), returnedMap.get(Integer.valueOf(2)));
		assertEquals(Integer.valueOf(33), returnedMap.get(Integer.valueOf(3)));
		assertEquals(Integer.valueOf(44), returnedMap.get(Integer.valueOf(4)));		
	}
	
	@Test
	public void buildListTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		List<String> returnedList = utils.buildList("a,b,c,d");
		
		assertEquals("a", returnedList.get(0));
		assertEquals("b", returnedList.get(1));
		assertEquals("c", returnedList.get(2));
		assertEquals("d", returnedList.get(3));
	}
	
	@Test
	public void buildIntListTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		List<Integer> returnedList = utils.buildIntList("1,2,3,4");
		
		assertEquals(Integer.valueOf(1), returnedList.get(0));
		assertEquals(Integer.valueOf(2), returnedList.get(1));
		assertEquals(Integer.valueOf(3), returnedList.get(2));
		assertEquals(Integer.valueOf(4), returnedList.get(3));
	}
	
	@Test
	public void buildEnumListTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		List<SimpleEnum> returnedList = utils.buildEnumList("FOO,BAR,FOOBAR");
		
		assertEquals(SimpleEnum.valueOf("FOO"), returnedList.get(0));
		assertEquals(SimpleEnum.valueOf("BAR"), returnedList.get(1));
		assertEquals(SimpleEnum.valueOf("FOOBAR"), returnedList.get(2));
	}

	
	@Test
	public void buildSetTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Set<String> returnedSet = utils.buildSet("a,b,c,d");
		
		assertTrue(returnedSet.contains("a"));
		assertTrue(returnedSet.contains("b"));
		assertTrue(returnedSet.contains("c"));
		assertTrue(returnedSet.contains("d"));
	}
	
	@Test
	public void buildIntSetTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Set<Integer> returnedSet = utils.buildIntSet("1,2,3,4");
		
		assertTrue(returnedSet.contains(Integer.valueOf("1")));
		assertTrue(returnedSet.contains(Integer.valueOf("2")));
		assertTrue(returnedSet.contains(Integer.valueOf("3")));
		assertTrue(returnedSet.contains(Integer.valueOf("4")));
	}
	
	@Test
	public void buildEnumSetTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Set<SimpleEnum> returnedSet = utils.buildEnumSet("FOO,BAR,FOOBAR");
		
		assertTrue(returnedSet.contains(SimpleEnum.valueOf("FOO")));
		assertTrue(returnedSet.contains(SimpleEnum.valueOf("BAR")));
		assertTrue(returnedSet.contains(SimpleEnum.valueOf("FOOBAR")));
	}
	
	@Test
	public void buildComplexMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		SomeComplexObject obj1 = new SomeComplexObject();
		obj1.setStringParameter("obj1");
		
		SomeComplexObject obj2 = new SomeComplexObject();
		obj2.setStringParameter("obj2");
		
		List<SomeComplexObject> complexList = new ArrayList<SomeComplexObject>();
		complexList.add(obj1);
		complexList.add(obj2);
		
		Map<String,SomeComplexObject> returnedMap = utils.buildComplexMap(complexList);
		
		assertEquals(obj1, returnedMap.get(obj1.getStringParameter()));
		assertEquals(obj2, returnedMap.get(obj2.getStringParameter()));
	}
	
	@Test
	public void compareComplexMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		SomeComplexObject obj1 = new SomeComplexObject();
		obj1.setStringParameter("String value for aaa");
		
		SomeComplexObject obj2 = new SomeComplexObject();
		obj2.setStringParameter("String value for bbb");
		
		SomeComplexObject obj3 = new SomeComplexObject();
		obj3.setStringParameter("String value for ccc");
		
		List<SomeComplexObject> complexList1 = new ArrayList<SomeComplexObject>();
		complexList1.add(obj1);
		complexList1.add(obj2);
		complexList1.add(obj3);
		
		Map<String,SomeComplexObject> map = utils.buildComplexMap(complexList1);
		
		boolean response = utils.compareComplexMaps(map, map);
		
		assertTrue(response);
	}
	
	@Test
	public void buildComplexDelegateMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Map<String,SomeComplexObject> returnedMap = utils.buildComplexDelegateMap();
		
		assertTrue(returnedMap.containsKey("DELEGATE"));
	}
	
	@Test
	public void buildComplexDelegateReturnMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		SomeComplexObject obj1 = new SomeComplexObject();
		obj1.setStringParameter("obj1");
		
		SomeComplexObject obj2 = new SomeComplexObject();
		obj2.setStringParameter("obj2");
		
		SomeComplexObject obj3 = new SomeComplexObject();
		obj2.setStringParameter("obj3");
		
		List<SomeComplexObject> complexList = new ArrayList<SomeComplexObject>();
		complexList.add(obj1);
		complexList.add(obj2);
		complexList.add(obj3);
				
		Map<String,SomeComplexObject> returnedMap = utils.buildComplexDelegateReturnMap(complexList);
		
		assertEquals(obj1, returnedMap.get("object1"));
		assertEquals(obj2, returnedMap.get("object2"));
		assertEquals(obj3, returnedMap.get("object3"));
	}
	
	@Test
	public void compareComplexDelegateMapsTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		SomeComplexObject obj1 = new SomeComplexObject();
		obj1.setStringParameter("obj1");
		
		SomeComplexObject obj2 = new SomeComplexObject();
		obj2.setStringParameter("obj2");
		
		SomeComplexObject obj3 = new SomeComplexObject();
		obj2.setStringParameter("obj3");
		
		List<SomeComplexObject> complexList = new ArrayList<SomeComplexObject>();
		complexList.add(obj1);
		complexList.add(obj2);
		complexList.add(obj3);
				
		Map<String,SomeComplexObject> map = utils.buildComplexDelegateReturnMap(complexList);
		
		boolean response = utils.compareComplexDelegateMaps(map, map);
		
		assertTrue(response);
	}
	
	@Test
	public void buildComplexSetTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		SomeComplexObject obj1 = new SomeComplexObject();
		obj1.setStringParameter("obj1");
		
		SomeComplexObject obj2 = new SomeComplexObject();
		obj2.setStringParameter("obj2");
		
		List<SomeComplexObject> complexList = new ArrayList<SomeComplexObject>();
		complexList.add(obj1);
		complexList.add(obj2);
		
		Set<SomeComplexObject> returnedSet = utils.buildComplexSet(complexList);
		
		assertTrue(returnedSet.contains(obj1));
		assertTrue(returnedSet.contains(obj2));
	}
	
	@Test
	public void createEmptyComplexListTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		List<ComplexObject> returnedList = utils.createEmptyComplexList();
		
		assertTrue(returnedList.isEmpty());
	}
	
	@Test
	public void createSetOfDatesTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Date date1 = new Date();
		Date date2 = new Date();
		
		List<Date> dates = new ArrayList<Date>();
		dates.add(date1);
		dates.add(date2);
		
		Set<Date> returnedSet = utils.createSetOfDates(dates);
		
		assertTrue(returnedSet.contains(date1));
		assertTrue(returnedSet.contains(date2));
	}
	
	@Test
	public void createMapOfDatesTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Date date1 = new Date();
		Date date2 = new Date();
		
		List<Date> dates = new ArrayList<Date>();
		dates.add(date1);
		dates.add(date2);
		
		Map<String,Date> returnedMap = utils.createMapOfDates(dates);
		
		assertEquals(date1, returnedMap.get("date1"));
		assertEquals(date2, returnedMap.get("date2"));
		
	}
	
	@Test
	public void buildByteBodyParamObjectTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		String bytes = "-1,2,127";
		BodyParamByteObject responseBody = utils.buildByteBodyParamObject(bytes);
		byte[] actualByteArray = responseBody.getBodyParameter();
		
		assertEquals("-1", String.valueOf(actualByteArray[0]));
		assertEquals("2", String.valueOf(actualByteArray[1]));
		assertEquals("127", String.valueOf(actualByteArray[2]));
	}
	
	@Test
	public void compareByteArraysTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		byte[] byteArray = new byte[3];
		byteArray[0] = Byte.valueOf("1");
		byteArray[1] = Byte.valueOf("2");
		byteArray[2] = Byte.valueOf("127");
		BodyParamByteObject inputWrapper = new BodyParamByteObject();
		inputWrapper.setBodyParameter(byteArray);
		
		ByteOperationResponseObject outputWrapper = new ByteOperationResponseObject();
		outputWrapper.setBodyParameter(byteArray);
		
		boolean response = utils.compareByteArrays(inputWrapper, outputWrapper);
		
		assertTrue(response);
		
	}
	
	@Test
	public void compareSetsTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Set<String> set1 = new HashSet<String>();
		set1.add("a");
		set1.add("c");
		set1.add("b");

		Set<String> set2 = new HashSet<String>();
		set2.add("a");
		set2.add("b");
		set2.add("c");
		
		boolean response = utils.compareSets(set1, set2);
		
		assertTrue(response);
	}
	
	@Test
	public void compareMapTest(){
		
		CougarClientResponseTypeUtils utils = new CougarClientResponseTypeUtils();
		
		Map<String,String> map1 = new HashMap<String,String>();
		map1.put("a","value for a");
		map1.put("b","value for b");
		map1.put("c","value for c");

		Map<String,String> map2 = new HashMap<String,String>();
		map2.put("a","value for a");
		map2.put("c","value for c");
		map2.put("b","value for b");
		
		boolean response = utils.compareMaps(map1, map2);
		
		assertTrue(response);
		
	}

}
