/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.core.impl.logging;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.logging.EventLogDefinition;
import com.betfair.cougar.logging.EventLoggingRegistry;
import com.betfair.cougar.logging.records.EventLogRecord;

@ManagedResource
public class EventLoggerImpl implements EventLogger {
	private final static CougarLogger logger = CougarLoggingUtils.getLogger(EventLoggerImpl.class);
	private EventLoggingRegistry registry;
	private boolean enabled = true;
	
	public void setRegistry(EventLoggingRegistry registry) {
		this.registry = registry;
	}

	@ManagedAttribute
	public boolean isEnabled() {
		return enabled;
	}

	@ManagedAttribute
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public void logEvent(LoggableEvent event) {
        logEvent(event, null);
	}

	@Override
	public void logEvent(LoggableEvent loggableEvent, Object[] extensionFields) {
		if (enabled) {
		    EventLogRecord eventLogRecord = new EventLogRecord(loggableEvent, extensionFields);
			logEventLogRecord(eventLogRecord);
		}
	}
	
	private void logEventLogRecord(EventLogRecord eventLogRecord) {
		EventLogDefinition invokableLogger = registry.getInvokableLogger(eventLogRecord.getLoggerName());
		if (invokableLogger != null) {
			CougarLoggingUtils.getLogger(invokableLogger.getLogName()).log(eventLogRecord);
		} else {
			throw new CougarFrameworkException("Logger "+eventLogRecord.getLoggerName()+" is not an event logger");
		}
	}
		
}


