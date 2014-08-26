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

package com.betfair.cougar.core.api.transports;

import com.betfair.cougar.core.api.BindingDescriptorRegistrationListener;
import org.springframework.beans.factory.annotation.Required;

/**
 * Abstract base transport class, provides property for the transport registry, and invokes
 * the registration method upon start
 */
public abstract class AbstractRegisterableTransport implements BindingDescriptorRegistrationListener {
    private TransportRegistry transportRegistry;

    public void register() {
        transportRegistry.registerTransport(this);
    }

    public void unregister() {
        transportRegistry.unregisterTransport(this);
    }

    @Required
    public void setTransportRegistry(TransportRegistry transportRegistry) {
        this.transportRegistry = transportRegistry;
    }
}
