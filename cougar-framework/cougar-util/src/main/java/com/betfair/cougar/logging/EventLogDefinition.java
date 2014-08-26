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

import java.io.IOException;

import com.betfair.cougar.logging.handlers.AbstractLogHandler;

public class EventLogDefinition {
	private AbstractLogHandler handler;
	private String logName;
	private EventLoggingRegistry registry;
	private boolean abstractDefinition;


	public void setHandler(AbstractLogHandler handler) {
		this.handler = handler;
	}

    public AbstractLogHandler getHandler() {
        return handler;
    }

    public void setLogName(String logName) {
		this.logName = logName;
	}
	public void setRegistry(EventLoggingRegistry registry) {
		this.registry = registry;
	}
	public void setAbstract(boolean abstractDefinition) {
		this.abstractDefinition = abstractDefinition;
	}
	public String getLogName() {
		return logName;
	}

	public boolean isAbstract() {
		return abstractDefinition;
	}
	public void register() {
		registry.register(this);
	}

    public String deriveConcreteLogName(String namespace, String serviceName) {
        return (namespace == null ? "" : namespace) + logName + serviceName;
    }

	public EventLogDefinition getConcreteLogger(String serviceName, String namespace) {
		if (!isAbstract()) {
			throw new IllegalArgumentException("Cannot create concrete version of non abstract logger");
		}

		try {
			EventLogDefinition result = new EventLogDefinition();
            String concreteLogName = deriveConcreteLogName(namespace, serviceName);
			result.setLogName(concreteLogName);
			result.registry = registry;
			result.abstractDefinition = false;
            result.setHandler(handler.clone(concreteLogName, serviceName, namespace));

			return result;
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to get concrete logger for " + serviceName, ioe);
		}
	}
}
