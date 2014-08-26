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

package com.betfair.cougar.core.impl.transports;

import com.betfair.cougar.core.api.transports.AbstractRegisterableTransport;
import com.betfair.cougar.core.api.transports.TransportRegistry;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Implements @TransportRegistry to provide a simple threadsafe transport registry
 */
public class TransportRegistryImpl implements TransportRegistry {
    private Set<AbstractRegisterableTransport> transports = new CopyOnWriteArraySet<AbstractRegisterableTransport>();

    public void registerTransport(AbstractRegisterableTransport transport) {
        transports.add(transport);
    }

    public void unregisterTransport(AbstractRegisterableTransport transport) {
        transports.remove(transport);
    }

    public Iterator<AbstractRegisterableTransport> getTransports() {
        return transports.iterator();
    }
}