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

package com.betfair.cougar.transport.api.protocol.events.jms;

import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.transport.api.protocol.events.EventBindingDescriptor;

import java.util.List;

/**
 * Implementation of the EventBindingDescriptor interface for JMS
 */
public class JMSEventBindingDescriptor implements EventBindingDescriptor {
    private String eventName;
    private List<JMSParamBindingDescriptor> paramBindings;
    private Class<? extends Event> eventBodyClass;


    public JMSEventBindingDescriptor(String eventName,
                                     List<JMSParamBindingDescriptor> paramBindings,
                                     Class<? extends Event> eventBodyClass) {
        this.eventName = eventName;
        this.paramBindings = paramBindings;
        this.eventBodyClass = eventBodyClass;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    public List<JMSParamBindingDescriptor> getParamBindings() {
        return paramBindings;
    }

    @Override
    public Class<? extends Event> getEventClass() {
        return eventBodyClass;
    }
}
