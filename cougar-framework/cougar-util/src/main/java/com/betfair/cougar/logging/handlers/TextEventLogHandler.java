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

package com.betfair.cougar.logging.handlers;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.logging.records.EventLogRecord;

public class TextEventLogHandler extends AbstractEventLogHandler {
	final static CougarLogger logger = CougarLoggingUtils.getLogger(TextEventLogHandler.class);

    private static final String LS = System.getProperty("line.separator");

	private final Handler handler;
	private final String fileName;
	private final boolean append;
	private final boolean flush;
	private final String policy;

	private static Formatter NO_FORMAT = new Formatter() {
		public String format(LogRecord record) {
			// The message is pre-formatted.
			return record.getMessage() + LS;
		}
    };
	public TextEventLogHandler(String fileName, boolean flush, boolean append, String policy, boolean abstractHandler) throws IOException {
		super(abstractHandler);
		this.fileName = fileName;
		this.append = append;
		this.policy = policy;
		this.flush = flush;
		if (!abstractHandler) {
			handler = new RollingFileHandler(fileName, append, flush, policy, NO_FORMAT);
		} else {
			handler = null;
		}
	}

	@Override
	public void publishEvent(EventLogRecord event) throws IOException {
		// pre-render the string, before hitting the synchronised handler.publish() method
		event.renderMessageString(); 
		handler.publish(event);
	}

	@Override
	public void close() throws SecurityException {
		handler.close();
	}

	@Override
	public void flush() {
		handler.flush();
	}

	@Override
	protected AbstractEventLogHandler cloneHandlerToName(String namespace, String name) throws IOException {
        String substitutedName = fileName.replace("##NAMESPACE##", namespace == null ? "" : "-"+namespace);
        substitutedName = substitutedName.replace("##NAME##", name);

		return new TextEventLogHandler(substitutedName, flush, append, policy, false);
	}

}
