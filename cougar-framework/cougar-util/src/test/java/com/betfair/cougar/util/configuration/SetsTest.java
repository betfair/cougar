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

package com.betfair.cougar.util.configuration;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SetsTest {

    /**
     * Given a map and a set of keys, return a set containing the values referred-to by the keys.
     */
    @Test
    public void testFromMap() {
        Map m = new HashMap() {{
            put("A", 1);
            put("B", 2);
            put("C", 3);
        }};
        Set s = Sets.fromMap(m, "A", "C");
        assertTrue(s.contains(1));
        assertFalse(s.contains(2));
        assertTrue(s.contains(3));
        assertEquals(2, s.size());
    }

    /**
     * If the map is null or the keys are null, the EMPTY_SET is returned.
     */
    @Test
    public void testFromMap_NullMap() {
        assertEquals(Collections.EMPTY_SET, Sets.fromMap(null, "A"));
    }

    /**
     * If the map is null or the keys are null, the EMPTY_SET is returned.
     */
    @Test
    public void testFromMap_NullKeys() {
        assertEquals(Collections.EMPTY_SET, Sets.fromMap(new HashMap(), null));
    }

    /**
     * If a passed key does not exist in the map, nothing is added to the set.
     */
    @Test
    public void testFromMap_MissingKey() {
        Map m = new HashMap() {{
            put("A", 1);
        }};
        Set s = Sets.fromMap(m, "A", "D");
        assertTrue(s.contains(1));
        assertEquals(1, s.size());
    }

    /**
     * However, if the key is present and maps to null, null is added to the set.
     */
    @Test
    public void testFromMap_KeyMappedToNull() {
        Map m = new HashMap() {{
            put("A", 1);
            put("B", null);
        }};
        Set s = Sets.fromMap(m, "A", "B");
        assertTrue(s.contains(1));
        assertTrue(s.contains(null));
        assertEquals(2, s.size());
    }

    /**
     * Given a comma-separated list of values, return a set of those values.
     */
    @Test
    public void testFromCommaSeparatedValues() {
        Set s = Sets.fromCommaSeparatedValues("1,2,3");
        assertTrue(s.contains("1"));
        assertTrue(s.contains("2"));
        assertTrue(s.contains("3"));
        assertEquals(3, s.size());
    }

    /**
     * If the passed string is null, the EMPTY_SET is returned.
     */
    @Test
    public void testFromCommaSeparatedValues_NullInput() {
        assertEquals(Collections.EMPTY_SET, Sets.fromCommaSeparatedValues(null));
    }

    /**
     * If the passed string is empty, the EMPTY_SET is returned.
     */
    @Test
    public void testFromCommaSeparatedValues_EmptyInput() {
        assertEquals(Collections.EMPTY_SET, Sets.fromCommaSeparatedValues(""));
    }
}
