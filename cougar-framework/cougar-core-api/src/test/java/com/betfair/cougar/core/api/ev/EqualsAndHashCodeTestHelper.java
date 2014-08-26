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

package com.betfair.cougar.core.api.ev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;
import org.junit.Assert;

public class EqualsAndHashCodeTestHelper {

    private ArrayList<Object> equal = new ArrayList<Object>();
    private ArrayList<Object> not = new ArrayList<Object>();
    private int expectedBuckets=0;

    public EqualsAndHashCodeTestHelper addEqual(Object... obj) {
        equal.addAll(Arrays.asList(obj));
        return this;
    }

    public EqualsAndHashCodeTestHelper addNot(Object... obj) {
        not.addAll(Arrays.asList(obj));
        return this;
    }

    public EqualsAndHashCodeTestHelper() {
        not.add(null);
    }

    public void testHashCodeMethod() {
        HashSet<Integer> set = new HashSet<Integer>();
        Object oot = equal.get(0);
        for (Object other : equal) {
            Assert.assertEquals(true, oot.hashCode() == other.hashCode());
        }
        int found=0;
        for (Object other : not) {
            if (other != null) {
                found++;
                set.add(other.hashCode());
            }
        }
        if(expectedBuckets==0){
            expectedBuckets=found;
        }
        Assert.assertTrue("Expected at least "+
                expectedBuckets+" unique hashcodes, but only created "+
                set.size()+" (hashing method is  weaker than expected)", set.size() >= expectedBuckets);

    }

    public void testEqualsAndHashCode() {
        testEqualsMethod();
        testHashCodeMethod();
    }

    public void testEqualsMethod() {
        if (equal.size() > 1) {
            Object oot = equal.get(0);
            for (Object other : equal) {
                Assert.assertTrue("Found two unequal objects which should be equal [" + oot + " and " + other + "]", oot.equals(other));
                Assert.assertTrue("Found two unequal objects which should be equal [" + oot + " and " + other + "]", other.equals(other));
            }
            for (Object other : not) {
                Assert.assertFalse("Found two equal objects which should NOT be equal [" + oot + " and " + other + "]", oot.equals(other));
                if (other != null) {
                    Assert.assertFalse("Found two equal objects which should NOT be equal [" + oot + " and " + other + "]", other.equals(oot));
                }
            }
        }
    }

    public void setExpectedBuckets(int expectedBuckets) {
        this.expectedBuckets = expectedBuckets;
    }
}
