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

package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.Service;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.BindingDescriptor;
import com.betfair.cougar.core.api.BindingDescriptorRegistrationListener;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceRegistrar;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.transports.EventTransport;
import com.betfair.tornjak.monitor.*;

import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Set;

/**
 * Instantiate one of these to introduce a service to cougar
 */
public class ClientServiceRegistration extends AbstractServiceRegistration {

    @Override
    public void introduceServiceToEV(ExecutionVenue ev, ServiceRegistrar serviceRegistrar, CompoundExecutableResolver compoundExecutableResolver) {
        setService(NULL_SERVICE);
        super.introduceServiceToEV(ev, serviceRegistrar, compoundExecutableResolver);
    }

    @Override
    public void introduceServiceToTransports(Iterator<? extends BindingDescriptorRegistrationListener> transports) {
        //There is no need to introduce a client service to the transports for anything (events are handled by the resolver)
    }

    private static Service NULL_SERVICE = new Service() {

        @Override
        public void init(ContainerContext cc) {
        }
    };
}
