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

package com.betfair.cougar.core.api.transcription;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

public class ParameterTypeTest {

	@Test
	public void testBasicType() {
		ParameterType type = ParameterType.create(Long.class, null);
		assertNotNull(type);
		assertEquals(ParameterType.Type.LONG, type.getType());
	}

	@Test
	public void testCollection() {
		ParameterType type = ParameterType.create(ArrayList.class, new Class[] {Double.class});
		assertNotNull(type);
		assertEquals(ParameterType.Type.LIST, type.getType());
		assertNotNull(type.getComponentTypes());
		assertEquals(1, type.getComponentTypes().length);
		assertEquals(ParameterType.Type.DOUBLE, type.getComponentTypes()[0].getType());
	}

	@Test
	public void testArray() {
		ParameterType type = ParameterType.create(byte[].class, null);
		assertNotNull(type);
		assertEquals(ParameterType.Type.LIST, type.getType());
		assertNotNull(type.getComponentTypes());
		assertEquals(1, type.getComponentTypes().length);
		assertEquals(ParameterType.Type.BYTE, type.getComponentTypes()[0].getType());
	}

    @Test
    public void testEnumClass() {
        ParameterType type = ParameterType.create(Enum.class, null);
        assertNotNull(type);
        assertEquals(ParameterType.Type.ENUM, type.getType());
    }

    @Test
    public void testEnumClassCollections() {
        ParameterType type = ParameterType.create(ArrayList.class, new Class[] {Enum.class});
        assertNotNull(type);
		assertEquals(ParameterType.Type.LIST, type.getType());
		assertNotNull(type.getComponentTypes());
		assertEquals(1, type.getComponentTypes().length);
		assertEquals(ParameterType.Type.ENUM, type.getComponentTypes()[0].getType());
    }

    public enum TestEnum{
       ENUM1,
       ENUM2
    }

    @Test
     public void testEnum() {
         ParameterType type = ParameterType.create(TestEnum.class, null);
         assertNotNull(type);
         assertEquals(ParameterType.Type.ENUM, type.getType());
     }

    @Test
    public void testEnumCollections() {
        ParameterType type = ParameterType.create(ArrayList.class, new Class[] {TestEnum.class});
        assertNotNull(type);
		assertEquals(ParameterType.Type.LIST, type.getType());
		assertNotNull(type.getComponentTypes());
		assertEquals(1, type.getComponentTypes().length);
		assertEquals(ParameterType.Type.ENUM, type.getComponentTypes()[0].getType());
    }
}
