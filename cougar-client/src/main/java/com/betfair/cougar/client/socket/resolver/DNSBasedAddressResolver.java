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

package com.betfair.cougar.client.socket.resolver;

import org.springframework.jmx.export.annotation.ManagedResource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * A network address resolver, that resolves server endpoint addresses using DNS
 */
@ManagedResource
public class DNSBasedAddressResolver implements NetworkAddressResolver {

    public Set<String> resolve(String host) throws UnknownHostException {
        Set<String> hosts = new HashSet<String>();
        final InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) {
            hosts.add(address.getHostAddress());
        }
        return hosts;
    }
}
