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

package com.betfair.cougar.netutil;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;

import static com.betfair.cougar.netutil.InetSocketAddressUtils.asString;
import static com.betfair.cougar.netutil.InetSocketAddressUtils.createInetSocketAddress;

@RunWith(value = Parameterized.class)
public class InetSocketAddressUtilsTest extends TestCase {

    private String in;
    private InetSocketAddress expected;

    public InetSocketAddressUtilsTest(String in, InetSocketAddress out) {
        this.in = in;
        this.expected = out;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
//Not even an address
                {"!", new InetSocketAddress("!", 0)},
//ip4
                {"localhost", new InetSocketAddress("localhost", 0)},
                {"127.0.0.1", new InetSocketAddress("127.0.0.1", 0)},
                {"localhost:", new InetSocketAddress("localhost", 0)},
                {"localhost:9002", new InetSocketAddress("localhost", 9002)},
                {"127.0.0.1:9002", new InetSocketAddress("127.0.0.1", 9002)},
                {"[127.0.0.1]9002", new InetSocketAddress("127.0.0.1", 9002)},
                {"[127.0.0.1]:9002", new InetSocketAddress("127.0.0.1", 9002)},
//ip6
                {"[2001::7334]", new InetSocketAddress("2001::7334", 0)},
                {"[2001::7335]:9002", new InetSocketAddress("2001::7335", 9002)},

                {"[::ffff:192.0.2.128]:80", new InetSocketAddress("::ffff:192.0.2.128", 80)},
                {"[::ffff:192.0.2.128]:", new InetSocketAddress("::ffff:192.0.2.128", 0)},
                {"[::ffff:192.0.2.128]", new InetSocketAddress("::ffff:192.0.2.128", 0)},

                {"::ffff:192.0.2.128", new InetSocketAddress("::ffff:192.0.2.128", 0)},

                {"0:0:0:0:0:0:0:1", new InetSocketAddress("0:0:0:0:0:0:0:1", 0)},

                {"::1", new InetSocketAddress("::1", 0)},
                {"[::1]", new InetSocketAddress("::1", 0)},
                {"[::1]:2", new InetSocketAddress("::1", 2)},
                {"[::1]:2", new InetSocketAddress("::1", 2)},
//Arrays Of addresses....
                {"[::1]:2,[127.0.0.1]9002", new InetSocketAddress("::1", 2)},
                {"[127.0.0.1]9002,[::1]:2", new InetSocketAddress("127.0.0.1", 9002)},
                {",", null},
                {",,x,,", null}
        };
        return Arrays.asList(data);
    }

    @Test
    public void testParseAddressList() {
        InetSocketAddress[] addresses = InetSocketAddressUtils.parseAddressList(in.split(","));
        for (int x = 0; x < in.split(",").length; x++) {
            assertTrue(addresses[x].equals(createInetSocketAddress(in.split(",")[x])));
        }
        assertTrue(addresses.length == in.split(",").length);
    }

    @Test
    public void testCreateInetAddress_host() {
        if (in.split(",").length > 0) {
            InetSocketAddress actual = createInetSocketAddress(in.split(",")[0]);
            if (expected != null) {
                assertEquals(expected.getAddress(), actual.getAddress());
                assertEquals(expected.getPort(), actual.getPort());
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testAddressAsString() {
        assertEquals("127.0.0.1:9003", asString(new InetSocketAddress("localhost", 9003)));
        assertEquals("5.5.5.5:9003", asString(new InetSocketAddress("5.5.5.5", 9003)));
    }
}
