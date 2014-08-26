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

package com.betfair.cougar.core.api.exception;

import java.util.logging.Level;



@SuppressWarnings("serial")
public class CougarValidationException extends CougarException {
	private static final Level LOG_LEVEL = Level.FINE;

	public CougarValidationException(ServerFaultCode fault) {
		super(LOG_LEVEL, fault);
	}

	public CougarValidationException(ServerFaultCode fault, Throwable t) {
		super(LOG_LEVEL, fault, t);
	}

	public CougarValidationException(ServerFaultCode fault, String message) {
		super(LOG_LEVEL, fault, message);
	}

    public CougarValidationException(ServerFaultCode fault, String message,Throwable t) {
		super(LOG_LEVEL, fault, message,t);
	}

}
