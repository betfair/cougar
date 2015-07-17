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

import com.betfair.cougar.util.NetworkAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Utilities for parsing list of IP addresses
 */
public class RemoteAddressUtils {

    private static final Logger log = LoggerFactory.getLogger(RemoteAddressUtils.class);

    public static final String localAddressList;
    public static final String localAddress;
    static {
        try {
            // just for testing hence not done as a cougar property
            boolean allowLoopback = "true".equals(System.getProperty("cougar.addressUtils.allowLoopBackIfNoOthers", "false"));
            StringBuilder localAddresses = new StringBuilder();
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            if (enumeration == null) {
                throw new RuntimeException("Failed to retrieve any network interfaces, consider setting system property \"cougar.addressUtils.allowLoopBackIfNoOthers\" to true");
            }
            // we only use this if there are no others and we're willing to accept the loopback
            NetworkInterface loopback = null;
            List<NetworkInterface> validInterfaces = new LinkedList<>();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface.isUp()) {
                    if (networkInterface.isLoopback()) {
                        loopback = networkInterface;
                    }
                    else {
                        validInterfaces.add(networkInterface);
                    }
                }
            }
            // fallback
            if (validInterfaces.isEmpty() && loopback != null && allowLoopback) {
                validInterfaces.add(loopback);
            }

            // now work out our addresses
            for (NetworkInterface networkInterface : validInterfaces) {
                Enumeration<InetAddress> inetAddresses =  networkInterface.getInetAddresses();
                while (inetAddresses != null && inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress() || (loopback!=null && allowLoopback)) {
                        localAddresses.append(inetAddress.getHostAddress());
                        localAddresses.append(",");
                    }
                }
            }

            if (localAddresses.length() == 0) {
                throw new RuntimeException("Failed to identify any site local address");
            }
            localAddresses.deleteCharAt(localAddresses.length() - 1);
            localAddressList = localAddresses.toString();

            // for where we only support a single address
            localAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (SocketException e) {
            log.error("unable to enumerate network interfaces");
            throw new RuntimeException("Failed to enumerate network interfaces",e);
        }
        catch (UnknownHostException e) {
            log.error("unable to find local host address");
            throw new RuntimeException("unable to find local host address");//NOSONAR
        }
    }

    /**
     * Parse a comma separated string of ip addresses into a list
     * Only valid IP address are returned in the list
     */
    public static List<String> parse(String address, String addresses) {
        List<String> result;
        // backwards compatibility - older clients only send a single address in the single address header and don't supply the multi-address header
        if (addresses == null || addresses.isEmpty()) {
            addresses = address;
        }
        if (addresses == null) {
            result = Collections.emptyList();
        }
        else {
            String[] parts = addresses.split(",");
            result = new ArrayList<String>(parts.length);
            for (String part : parts) {
                part = part.trim();
                if (NetworkAddress.isValidIPAddress(part)) {
                    result.add(part);
                }
            }
        }
        return result;
    }

    public static String externalise(List<String> ipAddresses) {
        return externalise(ipAddresses,null);
    }

    /**
     * Convert to an external form (comma separated string) after appending the local ip address
     */
    public static String externaliseWithLocalAddresses(List<String> addresses) {
        return externalise(addresses, localAddressList);
    }

    public static String getExternalisedLocalAddress() {
        return externalise(Arrays.asList(localAddress));
    }


    private static String externalise(List<String> addresses, String additional) {
        StringBuffer sb = new StringBuffer();
        if (!(addresses == null || addresses.isEmpty())) {
            for (String address : addresses) {
                if (!NetworkAddress.isValidIPAddress(address)) {
                    throw new IllegalArgumentException("address " + address + " is not a valid ip address");
                }
                sb.append(address);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
        }

        if (additional != null) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(additional);
        }
        return sb.toString();
    }

    public static int getLocalhostAsIPv4Integer() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return new BigInteger(localhost.getAddress()).intValue();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
