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

import java.net.UnknownHostException;
import java.util.Set;

/**
 * A custom network address resolver, that resolves a given server
 * endpoint, to one or more network addresses
 */
public interface NetworkAddressResolver {

    public Set<String> resolve(String host) throws UnknownHostException;
}
