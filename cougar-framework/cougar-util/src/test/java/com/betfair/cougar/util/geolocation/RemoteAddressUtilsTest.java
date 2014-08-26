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

package com.betfair.cougar.util.geolocation;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class RemoteAddressUtilsTest extends TestCase {

    public void testParseNull() throws Exception {
        assertEquals(Collections.emptyList(), RemoteAddressUtils.parse(null, null));
    }


    public void testParseSingle() throws Exception {
        assertEquals(Collections.singletonList("127.0.0.1"), RemoteAddressUtils.parse("127.0.0.1", "127.0.0.1"));
    }

    public void testParseNonIPAddress() throws Exception {
        assertEquals(Collections.emptyList(), RemoteAddressUtils.parse("a", "a"));
    }

    public void testParseMultiple() throws Exception {
        assertEquals(Arrays.asList("127.0.0.1","127.0.0.2"), RemoteAddressUtils.parse("127.0.0.1", "127.0.0.1, 127.0.0.2"));
    }

    public void testParseFallback() throws Exception {
        assertEquals(Arrays.asList("127.0.0.1"), RemoteAddressUtils.parse("127.0.0.1", null));
        assertEquals(Arrays.asList("127.0.0.1"), RemoteAddressUtils.parse("127.0.0.1", ""));
    }

    public void testExternalise1() throws Exception {
        assertEquals("127.0.0.1", RemoteAddressUtils.externalise(Collections.singletonList("127.0.0.1")));
    }

    public void testExternalise2() throws Exception {
        assertEquals("127.0.0.1,127.0.0.2", RemoteAddressUtils.externalise(Arrays.asList("127.0.0.1", "127.0.0.2")));
    }

    public void testExternaliseWithLocalAddress() throws Exception {
        String external = RemoteAddressUtils.externaliseWithLocalAddresses(Collections.singletonList("127.0.0.1"));
        assertEquals("127.0.0.1,"+ RemoteAddressUtils.localAddressList, external);
    }

    public void testExternaliseNullWithLocalAddress() throws Exception {
        String external = RemoteAddressUtils.externaliseWithLocalAddresses(null);
        assertEquals(RemoteAddressUtils.localAddressList, external);
    }

    public void testExternaliseEmptyWithLocalAddress() throws Exception {
        List empty = Collections.emptyList();
        String external = RemoteAddressUtils.externaliseWithLocalAddresses(empty);
        assertEquals(RemoteAddressUtils.localAddressList, external);
    }


}
