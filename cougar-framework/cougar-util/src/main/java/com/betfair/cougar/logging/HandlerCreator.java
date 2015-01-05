/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.logging;

import com.betfair.cougar.logging.handlers.AbstractLogHandler;
import com.betfair.cougar.logging.handlers.LogHandlerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Wraps invocation of the log handler factory to register a log handler. Required as Spring
 * resolves aliases referenced in a bean definition for a factory-bean prior to resolving
 * property placeholders, so this can't just be done via a bean definition.
 */
public class HandlerCreator {
    private LogHandlerFactory factory;
    private Map<String,String> properties;

    public void setFactory(LogHandlerFactory factory) {
        this.factory = factory;
    }

    public void setProperties(Map<String,String> properties) {
        this.properties = properties;
    }

    public void create() throws IOException {
        factory.registerLogHandler(properties);
    }
}
