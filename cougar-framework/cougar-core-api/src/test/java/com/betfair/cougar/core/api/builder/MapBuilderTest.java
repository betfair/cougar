/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.core.api.builder;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class MapBuilderTest {
    @Test
    public void works() {
        Map<String, String> map = new MapBuilder<String, String>()
                .put("SomeKey","SomeValue")
                .put("SomeOtherKey", "SomeOtherValue")
                .build();
        HashMap<String,String> toCompare = new HashMap<>();
        toCompare.put("SomeKey","SomeValue");
        toCompare.put("SomeOtherKey","SomeOtherValue");
        assertEquals(toCompare, map);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutableByDefault() {
        Map<String, String> map = new MapBuilder<String, String>().build();
        map.put("SomeKey","SomeValue");
    }

    @Test
    public void mutableByChoice() {
        Map<String, String> map = new MapBuilder<String, String>().leaveModifiable().build();
        map.put("SomeKey","SomeValue");
        assertTrue(map instanceof HashMap);
    }

    @Test
    public void hashtable() {
        Map<String, String> map = new MapBuilder<String, String>().toHashtable().leaveModifiable().build();
        map.put("SomeKey","SomeValue");
        assertTrue(map instanceof Hashtable);
    }

    @Test
    public void identityHashMap() {
        Map<String, String> map = new MapBuilder<String, String>().toIdentityHashMap().leaveModifiable().build();
        map.put("SomeKey","SomeValue");
        assertTrue(map instanceof IdentityHashMap);
    }

}
