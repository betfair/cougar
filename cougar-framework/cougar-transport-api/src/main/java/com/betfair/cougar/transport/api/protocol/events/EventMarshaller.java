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

package com.betfair.cougar.transport.api.protocol.events;

import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.core.api.exception.CougarException;

/**
 * This interface describes a marshaller for the supplied Event transport.  The marshaller
 * will turn an object that extends Event into an appropriate form to be emitted by your transport
 */
public interface EventMarshaller<T> {
    /**
     * Will marshall an event from the Event representation to type T.
     *
     * @param eventServiceBindingDescriptor
     * @param event
     * @param session - JMS, for some unfathomable reason, insists that Messages can only be created by a valid session
     * @return returns a representation of the event as T
     * @throws CougarException
     */
    public T marshallEvent(EventServiceBindingDescriptor eventServiceBindingDescriptor, Event event, Object session) throws CougarException;
}


