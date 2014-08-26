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

package com.betfair.cougar.logging.handlers;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.logging.records.EventLogRecord;

/**
 * Superclass for all event logs handlers
 */
public abstract class AbstractEventLogHandler extends Handler {
	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractEventLogHandler.class);
	private final boolean abstractHandler;

	public AbstractEventLogHandler(boolean abstractHandler) {
		this.abstractHandler = abstractHandler;
	}

	@Override
	public final void publish(LogRecord record) {
		if (abstractHandler) {
			throw new IllegalArgumentException("May not log to abstract handler");
		}
		if (record instanceof EventLogRecord) {
			try {
				publishEvent((EventLogRecord) record);
			} catch (IOException e) {
				throw new IllegalStateException("Unable to log an event", e);
			}
		} else {
			LOGGER.warn("Unable to Event log an record of class: {}", record.getClass().getCanonicalName());
			throw new IllegalArgumentException("Invalid class for event log: " + record.getClass().getCanonicalName() );
		}
	}

	public AbstractEventLogHandler clone(String namespace, String name) throws IOException {
		if (abstractHandler) {
			return cloneHandlerToName(namespace, name);
		} else {
			throw new IllegalStateException("Unable to clone non abstract handler");
		}
	}
	protected abstract AbstractEventLogHandler cloneHandlerToName(String namespace, String name) throws IOException;

	public abstract void publishEvent(EventLogRecord event) throws IOException;


}
