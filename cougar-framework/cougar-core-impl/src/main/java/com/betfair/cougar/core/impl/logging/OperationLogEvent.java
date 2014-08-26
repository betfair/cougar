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

package com.betfair.cougar.core.impl.logging;

import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;

public class OperationLogEvent implements LoggableEvent {

	private final String logName;
	private final ServiceVersion version;
	private final String operation;
	private final RequestUUID uuid;

	public OperationLogEvent(String logName, RequestUUID uuid, OperationKey key) {
		this.logName = logName;

        this.version = key.getVersion();
		this.operation = key.getOperationName();

		this.uuid = uuid;
	}


	@Override
	public Object[] getFieldsToLog() {
		return new Object[] {
				uuid,
				version,
				operation
			};
	}


    @Override
    public String getLogName() {
        return logName;
    }

}
