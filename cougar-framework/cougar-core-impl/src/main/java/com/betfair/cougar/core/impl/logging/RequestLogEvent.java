/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

import java.util.Date;

import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.OperationKey;

public class RequestLogEvent implements LoggableEvent {
    private final String logName;
	private final String faultCode;
	private final Date receivedTime;
	private final RequestUUID uuid;
	private final long operationTime;
	private final OperationKey operationKey;


	public RequestLogEvent(String logName, String faultCode, Date receivedTime, OperationKey operationKey,
			RequestUUID uuid, long operationTime) {
		super();
		this.logName = logName;
		this.faultCode = faultCode;
		this.receivedTime = receivedTime;
		this.uuid = uuid;
		this.operationTime = operationTime;
		this.operationKey = operationKey;
	}


	@Override
	public Object[] getFieldsToLog() {
		return new Object[] {
		        receivedTime,
				uuid.toCougarLogString(),
				String.format("%1$d.%2$d", operationKey.getVersion().getMajor(), operationKey.getVersion().getMinor()),
				operationKey.getOperationName(),
				faultCode,
				operationTime
			};
	}


    @Override
    public String getLogName() {
        return logName;
    }

}
