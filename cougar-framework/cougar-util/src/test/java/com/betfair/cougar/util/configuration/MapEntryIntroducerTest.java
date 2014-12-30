/*
 * Copyright 2014, Simon Matić Langford
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

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;


/**
 *
 */
public class MapEntryIntroducerTest {
    @Test
    public void singleEntry() throws Exception {
        Map<String,String> map = new HashMap<>();
        new MapEntryIntroducer<>(map, "key", "value");

        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
    }
    @Test
    public void multiEntry() throws Exception {
        Map<String,String> map = new HashMap<>();
        new MapEntryIntroducer<>(map, "key1", "value1");
        new MapEntryIntroducer<>(map, "key2", "value2");

        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }
}
