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

/**
 * This interface details the binding of an event to its generated
 * Event class implementation
 */
public interface EventBindingDescriptor {
    /**
     * @return - returns the name of the event as defined in the IDL
     */
    String getEventName();

    /**
     * @return - returns the class the event will be marshalled to/from
     */
    Class<? extends Event> getEventClass();
}
