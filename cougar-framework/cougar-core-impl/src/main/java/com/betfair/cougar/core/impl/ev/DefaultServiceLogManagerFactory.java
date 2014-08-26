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

import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ServiceLogManager;
import com.betfair.cougar.core.api.ev.ServiceLogManagerFactory;
import com.betfair.cougar.logging.EventLoggingRegistry;

/**
 *
 */
public class DefaultServiceLogManagerFactory implements ServiceLogManagerFactory {

    private final EventLoggingRegistry eventLoggingRegistry;

    public DefaultServiceLogManagerFactory(EventLoggingRegistry eventLoggingRegistry) {
        this.eventLoggingRegistry = eventLoggingRegistry;
    }

    @Override
    public ServiceLogManager create(String namespace, String serviceName, ServiceVersion version) {
        String loggerName = eventLoggingRegistry.registerConcreteLogger(namespace, serviceName);
        return new DefaultServiceLogManager(loggerName);
    }
}
