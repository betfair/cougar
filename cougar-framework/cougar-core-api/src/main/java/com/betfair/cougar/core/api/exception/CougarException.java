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

package com.betfair.cougar.core.api.exception;

import java.util.logging.Level;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.fault.Fault;
import com.betfair.cougar.logging.CougarLoggingUtils;

/**
 * An Exception that automatically logs itself
 */
@SuppressWarnings("serial")
public abstract class CougarException extends RuntimeException {
	private final ServerFaultCode serverFault;

	CougarException(Level level, ServerFaultCode serverFault) {
		super();
		this.serverFault = serverFault;
		logMe(serverFault, level);
	}

	CougarException(Level level, ServerFaultCode serverFault, Throwable e) {
		super(e);
		this.serverFault = serverFault;
		logMe(serverFault, level);
	}

	CougarException(Level level, ServerFaultCode serverFault, String cause) {
		super(cause);
		this.serverFault = serverFault;
		logMe(serverFault, level);
	}

	CougarException(Level level, ServerFaultCode serverFault, String cause, Throwable t) {
		super(cause, t);
		this.serverFault = serverFault;
		logMe(serverFault, level);
	}

	private void logMe(ServerFaultCode serverFault, Level level) {
		// If it's an internal error, then we should always log.
		// A client fault is only logged if we're on debug logging.
		if (ResponseCode.InternalError == serverFault.getResponseCode()) {
			level = Level.WARNING;
		}
		CougarLoggingUtils.getLogger(getClass()).log(level, "Exception thrown: ", this);
	}

	public Fault getFault() {
		return new Fault(getResponseCode().getFaultCode(), serverFault.getDetail(), getMessage(), getCause());
	}

	public ResponseCode getResponseCode() {
		return serverFault.getResponseCode();
	}

	// Prevent defined services overriding the exception message
	@Override
	public final String getMessage() {
		return super.getMessage();
	}

	public ServerFaultCode getServerFaultCode() {
		return serverFault;
	}
}
