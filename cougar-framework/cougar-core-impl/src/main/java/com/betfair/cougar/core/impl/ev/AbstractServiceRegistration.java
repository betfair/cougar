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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.BindingDescriptor;
import com.betfair.cougar.core.api.BindingDescriptorRegistrationListener;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceRegistrar;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.core.api.transports.EventTransport;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Abstract service registerer, includes an implementation of the method that
 * introduces the Transport BindingDescriptors to the transport collection
 */
public abstract class AbstractServiceRegistration implements EVServiceRegistration {
    //This is the applications executableResolver
    private ExecutableResolver resolver;
    private ServiceDefinition serviceDefinition;
    private Service service;
    private String namespace;
    private Set<BindingDescriptor> bindingDescriptors = Collections.emptySet();

    @Override
    public void introduceServiceToEV(ExecutionVenue ev, ServiceRegistrar serviceRegistrar, CompoundExecutableResolver compoundExecutableResolver) {
        compoundExecutableResolver.registerExecutableResolver(namespace, getResolver());
        serviceRegistrar.registerService(namespace, getServiceDefinition(), getService(), compoundExecutableResolver);
    }

    public abstract void introduceServiceToTransports(Iterator<? extends BindingDescriptorRegistrationListener> transports);

    public Set<BindingDescriptor> getBindingDescriptors() {
        return bindingDescriptors;
    }

    public void setBindingDescriptors(Set<BindingDescriptor> bindingDescriptors) {
        this.bindingDescriptors = bindingDescriptors;
    }

    public ExecutableResolver getResolver() {
        return resolver;
    }

    public void setResolver(ExecutableResolver resolver) {
        this.resolver = resolver;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public ServiceDefinition getServiceDefinition() {
        return serviceDefinition;
    }

    public void setServiceDefinition(ServiceDefinition serviceDefinition) {
        this.serviceDefinition = serviceDefinition;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
