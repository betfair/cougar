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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class SetBuilderTest {
    @Test
    public void works() {
        Set<String> set = new SetBuilder<String>()
                .add("SomeString")
                .add("SomeOtherString")
                .build();
        assertEquals(new HashSet<>(Arrays.asList("SomeString", "SomeOtherString")), set);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutableByDefault() {
        Set<String> set = new SetBuilder<String>().build();
        set.add("SomeString");
    }

    @Test
    public void mutableByChoice() {
        Set<String> set = new SetBuilder<String>().leaveModifiable().build();
        set.add("SomeString");
        assertTrue(set instanceof HashSet);
    }

    @Test
    public void linkedHashSet() {
        Set<String> set = new SetBuilder<String>().toLinkedHashSet().leaveModifiable().build();
        assertTrue(set instanceof LinkedHashSet);
    }

    @Test
    public void concurrentSkipListSet() {
        Set<String> set = new SetBuilder<String>().toConcurrentSkipListSet().leaveModifiable().build();
        assertTrue(set instanceof ConcurrentSkipListSet);
    }

    @Test
    public void copyOnWriteArraySet() {
        Set<String> set = new SetBuilder<String>().toCopyOnWriteArraySet().leaveModifiable().build();
        assertTrue(set instanceof CopyOnWriteArraySet);
    }
}
