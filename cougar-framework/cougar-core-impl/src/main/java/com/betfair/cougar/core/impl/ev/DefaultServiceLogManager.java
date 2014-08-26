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

import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ServiceLogManager;
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.JMXControl;
import com.betfair.tornjak.kpi.KPIMonitor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of ExecutionManager for managing a service.
 * Will register itself with JMX, and provides stats and status for the service.
 *
 */
@ManagedResource
public class DefaultServiceLogManager implements ServiceLogManager {

	private String loggerName;
	private Class<? extends LogExtension> logExtensionClass;
	private int numLogExtensionFields;
    private Class<? extends LogExtension> connectedObjectLogExtensionClass;
    private int numConnectedObjectLogExtensionFields;

    public DefaultServiceLogManager(String loggerName) {
		this.loggerName = loggerName;
	}

	@Override
	public Class<? extends LogExtension> getLogExtensionClass() {
		return logExtensionClass;
	}

	@Override
	public int getNumLogExtensionFields() {
		return numLogExtensionFields;
	}

	@Override
	public void registerExtensionLoggerClass(
			Class<? extends LogExtension> extensionClass, int numFieldsLogged) {
		this.logExtensionClass = extensionClass;
		this.numLogExtensionFields = numFieldsLogged;
	}

    @Override
    public Class<? extends LogExtension> getConnectedObjectLogExtensionClass() {
        return connectedObjectLogExtensionClass;
    }

    @Override
    public int getNumConnectedObjectLogExtensionFields() {
        return numConnectedObjectLogExtensionFields;
    }

    @Override
    public void registerConnectedObjectExtensionLoggerClass(Class<? extends LogExtension> clazz, int numFieldsLogged) {
        this.connectedObjectLogExtensionClass = clazz;
        this.numConnectedObjectLogExtensionFields = numFieldsLogged;
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }
}
