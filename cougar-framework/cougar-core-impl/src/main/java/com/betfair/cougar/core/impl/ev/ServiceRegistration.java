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

import com.betfair.cougar.core.api.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.core.api.transports.EventTransport;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;

import java.util.Iterator;
import java.util.Set;

/**
 * Instantiate one of these to introduce a service to cougar
 */
public class ServiceRegistration extends AbstractServiceRegistration {
    //These properties are only used if the application emits events
    private Set<EventTransport> eventTransports;

    @Override
    public void introduceServiceToEV(ExecutionVenue ev, ServiceRegistrar serviceRegistrar, CompoundExecutableResolver compoundExecutableResolver) {
        super.introduceServiceToEV(ev, serviceRegistrar, compoundExecutableResolver);

        //Subscribe to the app for event publication on all event transports (if present)
        if (eventTransports != null && !eventTransports.isEmpty()) {
            for (final EventTransport eventTransport : eventTransports) {
                for (OperationDefinition opDef : getServiceDefinition().getOperationDefinitions(OperationKey.Type.Event)) {
                    ev.execute(eventTransport.getExecutionContext(), opDef.getOperationKey(), new Object[0], new ExecutionObserver() {
                        @Override
                        public void onResult(ExecutionResult result) {
                            eventTransport.publish((Event)result.getResult());
                        }
                    }, DefaultTimeConstraints.NO_CONSTRAINTS);
                }
            }
        }
    }

    /**
     * Exports each binding descriptor to the supplied collection of transports
     * @param transports
     */
    public void introduceServiceToTransports(Iterator<? extends BindingDescriptorRegistrationListener> transports) {
        while (transports.hasNext()) {
            BindingDescriptorRegistrationListener t = transports.next();
            boolean eventTransport = t instanceof EventTransport;
            boolean includedEventTransport = false;
            if (eventTransport) {
                // if it's an event transport then we only want to notify if we've been told that the developer wanted to
                // bind to this particular transport (since you can have >1 instance of the event transport currently)
                if (eventTransports != null && eventTransports.contains(t)) {
                    includedEventTransport = true;
                }
            }
            if (!eventTransport || includedEventTransport) {
                for (BindingDescriptor bindingDescriptor : getBindingDescriptors()) {
                    t.notify(bindingDescriptor);
                }
            }
        }
    }

    public void setEventTransports(Set<EventTransport> eventTransportSet) {
        this.eventTransports = eventTransportSet;
    }
}
