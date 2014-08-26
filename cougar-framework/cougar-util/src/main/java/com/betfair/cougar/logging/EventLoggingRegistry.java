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

package com.betfair.cougar.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EventLoggingRegistry {
	private final static Logger LOGGER = LoggerFactory.getLogger(EventLoggingRegistry.class);
	private final Map<String, EventLogDefinition> loggerRegistry = new HashMap<String, EventLogDefinition>();
	private EventLogDefinition abstractGlobalLogger;

	public EventLoggingRegistry() {
	}

	public void register(EventLogDefinition logDef) {
		LOGGER.info("Registering {} logger {}", logDef.isAbstract() ? "abstract" : "invokable", logDef.getLogName());
		if (logDef.isAbstract()) {
			if (abstractGlobalLogger == null) {
				abstractGlobalLogger = logDef;
			} else {
				throw new IllegalStateException("Global logger ("+abstractGlobalLogger.getLogName()+") already defined");
			}
		} else {
			loggerRegistry.put(logDef.getLogName(), logDef);
		}
	}
	public EventLogDefinition getInvokableLogger(String logName) {
		return loggerRegistry.get(logName);
	}

	public String registerConcreteLogger(String namespace, String serviceName) {
		if (abstractGlobalLogger == null) {
			throw new IllegalStateException("abstract logger not defined");
		}
        //Check for logger already defined here. Can happen if we have more
        //than one version of a service instantiated

        EventLogDefinition concreteLogger = loggerRegistry.get(abstractGlobalLogger.deriveConcreteLogName(namespace, serviceName));
        if (concreteLogger == null) {
            concreteLogger = abstractGlobalLogger.getConcreteLogger(serviceName, namespace);
            concreteLogger.register();
        }
        return concreteLogger.getLogName();
	}

}
