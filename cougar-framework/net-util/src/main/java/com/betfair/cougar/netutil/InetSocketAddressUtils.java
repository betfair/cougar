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


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class InetSocketAddressUtils {

    private InetSocketAddressUtils() {
        //It makes no sense to instantiate this class.
    }


    /**
     * Returns a string representation of the specified socket address
     * in the form <IPADDRESS>:<PORT>
     *
     * @param socketAddress the socket address
     */
    public static String asString(SocketAddress socketAddress) {
        final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        final InetAddress inetAddress = inetSocketAddress.getAddress();
        String host = inetSocketAddress.getHostName();
        if (inetAddress != null) {
            host = inetAddress.getHostAddress();
        }
        return host + ":" + inetSocketAddress.getPort();
    }

    /**
     * <p>Creates an array of InetSocketAddress given an array of String</p>
     *
     * @param addresses the addresses, which may be a mixture of any of the formats supported by
     *                  createInetSocketAddress(String address)
     */
    public static InetSocketAddress[] parseAddressList(String[] addresses) {
        InetSocketAddress[] isa = new InetSocketAddress[addresses.length];
        for (int x = 0; x < addresses.length; x++) {
            isa[x] = createInetSocketAddress(addresses[x]);
        }
        return isa;
    }

    /**
     * <p>Creates an InetSocketAddress given a host and optional port in a single String</p>
     * <p/>
     * <p>This allows either IP4 or IP6 addresses (including port) to be provided as Strings as per rfc2732</p>
     *
     * @param address the address in one of the following formats (the braces '[]'and colon ':' are literal here):
     *                host<br>
     *                [host]<br>
     *                [host]:port<br>
     *                [host]port<br>
     *                ip4host:port<br>
     *                <p>Assumes port 0 if non is specified.</p>
     *                <p/>
     *                <p> As per java.net.InetSocketAddress
     *                A port number of zero will let the system pick up an ephemeral port in a bind operation.</p>
     */
    public static InetSocketAddress createInetSocketAddress(String address) {
        return createInetSocketAddress(address, 0);
    }

    /**
     * <p>Creates an InetSocketAddress given a host and optional port in a single String
     * <p/>
     * <p>This allows either IP4 or IP6 addresses (including port) to be provided as Strings as per rfc2732</p>
     *
     * @param address     the address in one of the following formats (the braces '[]'and colon ':' are literal here):
     *                    host<br>
     *                    [host]<br>
     *                    [host]:port<br>
     *                    [host]port<br>
     *                    ip4host:port<br>
     * @param defaultPort The default port to be used ONLY IF the string does not specify a port
     * @see java.net.InetSocketAddress
     */
    public static InetSocketAddress createInetSocketAddress(String address, int defaultPort) {
        String original = address.trim();
        String host = original;
        int port = defaultPort;

        if (host.startsWith("[")) {
            // it is an address in [host] or [host]port format
            String[] s = original.split("\\]");
            if (s.length > 1) {
                if (s[1].startsWith(":")) {
                    s[1] = s[1].substring(1);
                }
                port = computePort(s[1], 0);
            }
            host = s[0].substring(1);
        }


        if (host.indexOf(":") == host.lastIndexOf(":") && (host.indexOf(":") > -1)) {
            //There is exactly 1 ':' in the string, hence this is an IP4 address which includes a port
            String[] s = original.split("\\:");
            host = s[0];
            if (s.length > 1) {
                port = computePort(s[1], 0);
            }
        }
        return new InetSocketAddress(host, port);
    }

    private static int computePort(String s, int i) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return i;
        }
    }


}
