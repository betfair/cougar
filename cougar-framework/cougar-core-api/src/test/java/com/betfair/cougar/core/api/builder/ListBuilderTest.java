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
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.Assert.*;

/**
 *
 */
public class ListBuilderTest {
    @Test
    public void works() {
        List<String> list = new ListBuilder<String>()
                .add("SomeString")
                .add("SomeOtherString")
                .build();
        assertEquals(Arrays.asList("SomeString","SomeOtherString"), list);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutableByDefault() {
        List<String> list = new ListBuilder<String>().build();
        list.add("SomeString");
    }

    @Test
    public void mutableByChoice() {
        List<String> list = new ListBuilder<String>().leaveModifiable().build();
        list.add("SomeString");
        assertTrue(list instanceof LinkedList);
    }

    @Test
    public void arrayList() {
        List<String> list = new ListBuilder<String>().toArrayList().leaveModifiable().build();
        assertTrue(list instanceof ArrayList);
    }

    @Test
    public void copyOnWriteArrayList() {
        List<String> list = new ListBuilder<String>().toCopyOnWriteArrayList().leaveModifiable().build();
        assertTrue(list instanceof CopyOnWriteArrayList);
    }

    @Test
    public void vector() {
        List<String> list = new ListBuilder<String>().toVector().leaveModifiable().build();
        assertTrue(list instanceof Vector);
    }
}
