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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.BindingDescriptor;
import com.betfair.cougar.core.api.BindingDescriptorRegistrationListener;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.events.Event;

/**
 * This interface describes an implementation of an event based transport.  The two methods
 * exposed facilitate event emission and establishment of a subscription for consumption.
 */
public interface EventTransport extends BindingDescriptorRegistrationListener {
    /**
     * Emit an event from the transport
     * @param event
     */
    void publish(Event event);

    /**
     * Subscribes to events of the prescribed name.  The observer you supply will
     * be called back repeatedly for each event, so your observer implementation
     * must be re-entrant safe
     * @param eventName - the name of the event, which must match the event name defined in the IDD
     * @param args
     * @param observer
     */
    void subscribe(String eventName, Object[] args, ExecutionObserver observer);

    ExecutionContext getExecutionContext();

    /**
     * Used to notify an event transport of a binding. Since event transports can be used either for publishing or subscribing
     * we need to distinguish. Implementations of this interface MUST treat calls to notify(BindingDescriptor) as equivalent to
     * notify(BindingDescriptor, EventTransportMode.Publish).
     * @param bindingDescriptor
     * @param mode
     */
    void notify(BindingDescriptor bindingDescriptor, EventTransportMode mode);
}
