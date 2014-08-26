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

package com.betfair.cougar.transport.jms;

import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.transport.api.protocol.events.jms.JMSDestinationResolver;

/**
 * This simple destination resolver constructs a JMS destination name as a string
 * from the destinationBase constructor argument and the event class name
 * @see JMSDestinationResolver
 */
public class EventNameDestinationResolver implements JMSDestinationResolver<String> {
    private String destinationBase;

    public EventNameDestinationResolver(String destinationBase) {
        this.destinationBase = destinationBase;
    }
    @Override
    public String resolveDestination(Class<? extends Event> eventClass, Object[] args) {
        if (eventClass != null) {
            return destinationBase + "." + eventClass.getSimpleName();
        }
        return destinationBase;
    }
}
