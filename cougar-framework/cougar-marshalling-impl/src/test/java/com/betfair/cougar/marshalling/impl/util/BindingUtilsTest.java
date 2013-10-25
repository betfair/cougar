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

package com.betfair.cougar.marshalling.impl.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.test.CougarTestCase;

public class BindingUtilsTest extends CougarTestCase {
	
	public enum TestEnum { SUNDAY, MONDAY }; 


	public void testConvertToSimpleTypeEnum() {
		assertEquals(TestEnum.SUNDAY, BindingUtils.convertToSimpleType(TestEnum.class, null, "foo", "SUNDAY", false, true));
	}

	public void testConvertToSimpleTypeEnumInvalid() {
		try {
			assertEquals(TestEnum.SUNDAY, BindingUtils.convertToSimpleType(TestEnum.class, null, "foo", "sunday", false, true));
			fail();
		} catch (CougarValidationException e) {
			assertException(e, "foo", "sunday", EnumDerialisationException.class);
		}
	}

	
	public void testConvertToSimpleTypeDate() {
		final String testDate = "2010-11-10T13:37:00";
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date sourceDate = null;


		try {
			sourceDate = sdf.parse(testDate);
		} catch (ParseException e) {
			e.printStackTrace();
			// It's a hard coded date, if it works it works.
		}
		assertTrue(BindingUtils.convertToSimpleType(java.util.Date.class, null, "foo", testDate, true, true).equals(sourceDate));
	}

    public void testConvertToSimpleTypeDateInvalid() {
		final String testDate = "the day the Earth stood stupid";
		try {
			BindingUtils.convertToSimpleType(java.util.Date.class, null,  "dateParameter", testDate,false, true);
		} catch (CougarValidationException e) {
			assertException(e, "dateParameter", testDate, IllegalArgumentException.class);
		}
	}

	public void testConvertToSimpleTypePrimitiveByte() {
		assertTrue(BindingUtils.convertToSimpleType(Byte.class, null, "foo", "1", true, true).equals((byte) 1));
	}

	public void testConvertToSimpleTypePrimitiveShort() {
		assertTrue(BindingUtils.convertToSimpleType(Short.class, null,  "foo", "1", true, true).equals((short) 1));
	}

	public void testConvertToSimpleTypePrimitiveInteger() {
		assertTrue(BindingUtils.convertToSimpleType(Integer.class, null,  "foo", "1", true, true).equals(1));
	}

	public void testConvertToSimpleTypePrimitiveLong() {
		assertTrue(BindingUtils.convertToSimpleType(Long.class, null,  "foo", "1", true, true).equals((long) 1));
	}

	public void testConvertToSimpleTypePrimitiveFloat() {
		assertTrue(BindingUtils.convertToSimpleType(Float.class, null,  "foo", "1", true, true).equals((float) 1));
	}

	public void testConvertToSimpleTypePrimitiveFloatNonNumeric() {
		assertTrue(BindingUtils.convertToSimpleType(Float.class, null,  "foo", "NaN", true, true).equals(Float.NaN));
		assertTrue(BindingUtils.convertToSimpleType(Float.class, null,  "foo", "INF", true, true).equals(Float.POSITIVE_INFINITY));
		assertTrue(BindingUtils.convertToSimpleType(Float.class, null,  "foo", "Infinity", true, true).equals(Float.POSITIVE_INFINITY));
		assertTrue(BindingUtils.convertToSimpleType(Float.class, null,  "foo", "-INF", true, true).equals(Float.NEGATIVE_INFINITY));
		assertTrue(BindingUtils.convertToSimpleType(Float.class, null,  "foo", "-Infinity", true, true).equals(Float.NEGATIVE_INFINITY));
	}

	public void testConvertToSimpleTypePrimitiveDouble() {
		assertTrue(BindingUtils.convertToSimpleType(Double.class, null,  "foo", "1", true, true).equals((double) 1));
	}

	public void testConvertToSimpleTypePrimitiveDoubleNonNumeric() {
		assertTrue(BindingUtils.convertToSimpleType(Double.class, null,  "foo", "NaN", true, true).equals(Double.NaN));
		assertTrue(BindingUtils.convertToSimpleType(Double.class, null,  "foo", "INF", true, true).equals(Double.POSITIVE_INFINITY));
		assertTrue(BindingUtils.convertToSimpleType(Double.class, null,  "foo", "Infinity", true, true).equals(Double.POSITIVE_INFINITY));
		assertTrue(BindingUtils.convertToSimpleType(Double.class, null,  "foo", "-INF", true, true).equals(Double.NEGATIVE_INFINITY));
		assertTrue(BindingUtils.convertToSimpleType(Double.class, null,  "foo", "-Infinity", true, true).equals(Double.NEGATIVE_INFINITY));
	}

	public void testConvertToSimpleTypePrimitiveBooleanTrue() {
		assertTrue(BindingUtils.convertToSimpleType(Boolean.class, null,  "foo", "true", true, true).equals(Boolean.TRUE));
	}

	public void testConvertToSimpleTypePrimitiveBooleanFalse() {
		assertTrue(BindingUtils.convertToSimpleType(Boolean.class, null,  "foo", "FALSE", true, true).equals(Boolean.FALSE));
	}

	public void testConvertToSimpleTypePrimitiveBooleanInvalid() {

		try {
			BindingUtils.convertToSimpleType(Boolean.class, null,  "fooParam", "eep", true, true);
			fail();
		} catch (CougarValidationException e) {
			assertException(e, "fooParam", "eep", null);
		}
	}

	public void testConvertToSimpleTypePrimitiveCharacter() {
		assertTrue(BindingUtils.convertToSimpleType(Character.class, null, "foo", "1", true, true).equals('1'));
	}

	public void testConvertToSimpleTypePrimitiveVoid() {

		try {
			BindingUtils.convertToSimpleType(Void.class, null, "foo", "1", true, true);
			fail();
		} catch (CougarValidationException e) {
			assertException(e, "foo", "1", null);
		}
	}

	public void testConvertToIntegerInvalid() {

		try {
			BindingUtils.convertToSimpleType(Integer.class, null,  "foo", "1.3", true, true);
			fail();
		} catch (CougarValidationException e) {
			assertException(e, "foo", "1.3", NumberFormatException.class);
		}
	}

	public void testConvertToDoubleInvalid() {

		try {
			BindingUtils.convertToSimpleType(Double.class, null,  "bar", "foo", true, true);
			fail();
		} catch (CougarValidationException e) {
			assertException(e, "bar", "foo", NumberFormatException.class);
		}
	}

	public void testConvertToSimpleTypeString() {
		String foo = "foo";
		assertTrue(BindingUtils.convertToSimpleType(String.class, null, "bar", foo, true, true) == foo);
	}

	public void testStringEscaping() {
		String foo = "foo+bar";
		assertEquals("foo bar", BindingUtils.convertToSimpleType(String.class, null, "bar", foo, true, true));
		assertEquals("foo+bar", BindingUtils.convertToSimpleType(String.class, null, "bar", foo, false, true));
	}

    @SuppressWarnings("unchecked")
    public void testConvertToListOfStrings() {
        List<String> strings = (List<String>)BindingUtils.convertToSimpleType(List.class, String.class,  "bar", "123,456 , 789", true, true);
        assertEquals(3, strings.size());
        assertEquals("123", strings.get(0));
        assertEquals("456", strings.get(1));
        assertEquals("789", strings.get(2));

        //Second test for sparse string list
        List<String> sparseStringList = (List<String>)BindingUtils.convertToSimpleType(List.class, String.class,  "bar", "xxx,yyy,,zzz", true, true);
        assertEquals(4, sparseStringList.size());
        assertEquals("xxx", sparseStringList.get(0));
        assertEquals("yyy",    sparseStringList.get(1));
        assertEquals("", sparseStringList.get(2));
        assertEquals("zzz", sparseStringList.get(3));
    }

    @SuppressWarnings("unchecked")
    public void testConvertToSetOfStringsWithJSONEscapedArray() {
        Set<String> strings = (Set<String>)BindingUtils.convertToSimpleType(Set.class, String.class,  "bar", "[123,456, 789]", true, true);
        assertEquals(3, strings.size());
        assertTrue(strings.contains("123"));
        assertTrue(strings.contains("456"));
        assertTrue(strings.contains("789"));
    }


    @SuppressWarnings("unchecked")
    public void testConvertToListOfInts() {
        List<Integer> integers = (List<Integer>)BindingUtils.convertToSimpleType(List.class, Integer.class,  "bar", "123,456,789", true, true);
        assertEquals(3, integers.size());
        assertEquals(Integer.valueOf(123), integers.get(0));
        assertEquals(Integer.valueOf(456), integers.get(1));
        assertEquals(Integer.valueOf(789), integers.get(2));
    }

    @SuppressWarnings("unchecked")
    public void testSparseIntSet() {
        Set<Integer> set = (Set<Integer>)BindingUtils.convertToSimpleType(Set.class, Integer.class, "bob", "1,,2,3", true, true);
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }
    


    public void testConvertToListOfIntsInvalid() {
    	try {
    		BindingUtils.convertToSimpleType(List.class, Integer.class,  "list", "12.3 , 456 ,789", true, true);
    		fail();
    	}
    	catch (CougarValidationException e) {
    		assertException(e, "list", "12.3", NumberFormatException.class);
    	}
    }
    
    
    private void assertException (CougarValidationException e, String name, String value, Class expectedCause) {
        assertTrue(e.getMessage().indexOf(value)>-1);
        assertTrue(e.getMessage().indexOf(name)>-1);
        if (expectedCause != null) {
        	assertEquals(expectedCause, e.getCause().getClass());
        }
    	
    }
}