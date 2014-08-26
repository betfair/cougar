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

import com.betfair.cougar.core.api.ServiceVersion;
import junit.framework.TestCase;
import org.junit.Test;

public class OperationKeyTest extends TestCase {

    @Test
    public void testDefaultType() {
        assertEquals(new OperationKey(new ServiceVersion(1, 1), "b", "c").getType(), OperationKey.Type.Request);
    }


    @Test
    public void testEqualsAndHashCode() {
        new EqualsAndHashCodeTestHelper()
        .addEqual(
                new OperationKey(new ServiceVersion(1, 1), "b", "c"),
                new OperationKey(new ServiceVersion("v1.1"), "b", "c"),
                new OperationKey(new ServiceVersion("v1.1"), "b", "c")
        )
        .addNot(
                new OperationKey(new ServiceVersion(0, 1), "b", "c"),
                new OperationKey(new ServiceVersion(1, 0), "b", "c"),
                new OperationKey(new ServiceVersion(1, 1), "z", "c"),
                new OperationKey(new ServiceVersion(1, 1), "b", "z")
        )
        .testEqualsAndHashCode();
    }

    @Test
    public void testNamespacedKeys() {
        OperationKey opKey = new OperationKey(new ServiceVersion(1, 1), "b", "c");

        OperationKey fooOpKey = new OperationKey(opKey, "foo");
        OperationKey barOpKey = new OperationKey(opKey, "bar");

        assertFalse(opKey.hashCode() == fooOpKey.hashCode());
        assertFalse(opKey.equals(fooOpKey));
        assertFalse(barOpKey.hashCode() == fooOpKey.hashCode());
        assertFalse(barOpKey.equals(fooOpKey));

        assertTrue(opKey.equals(opKey));
        assertTrue(fooOpKey.equals(fooOpKey));
        assertTrue(barOpKey.equals(barOpKey));

        assertTrue(opKey.getLocalKey() == opKey);
        assertTrue(fooOpKey.getLocalKey() == opKey);
        assertTrue(barOpKey.getLocalKey() == opKey);

        OperationKey nullOpKey = new OperationKey(opKey, null);
        assertTrue(nullOpKey.getLocalKey() == opKey);
        assertEquals(nullOpKey, opKey);
        assertEquals(nullOpKey.hashCode(), opKey.hashCode());
    }


}
