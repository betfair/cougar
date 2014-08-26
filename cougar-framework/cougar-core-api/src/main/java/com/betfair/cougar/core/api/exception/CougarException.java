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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.fault.Fault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Exception that automatically logs itself
 */
@SuppressWarnings("serial")
public abstract class CougarException extends RuntimeException {
	private final ServerFaultCode serverFault;
    private static Map<Class<? extends CougarException>,Logger> loggers = new HashMap<>();

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

    private Logger getLogger()
    {
        Class<? extends CougarException> c = getClass();
        Logger logger = loggers.get(c);
        if (logger == null)
        {
            logger = LoggerFactory.getLogger(c);
            loggers.put(c, logger);
        }
        return logger;
    }

	private void logMe(ServerFaultCode serverFault, Level level) {
		// If it's an internal error, then we should always log.
		// A client fault is only logged if we're on debug logging.
		if (ResponseCode.InternalError == serverFault.getResponseCode()) {
			level = Level.WARNING;
		}
        Logger logger = getLogger();

        String additionalInfo = additionalInfo();
        String message = additionalInfo == null ? "Exception thrown" : "Exception thrown: " + additionalInfo;

        if (level.equals(Level.SEVERE)) {
            logger.error(message, this);
        }
        else if (level.equals(Level.WARNING)) {
            logger.warn(message, this);
        }
        else {
            logger.debug(message, this);
        }
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
        String additional = additionalInfo();
        return additional == null ? super.getMessage() : super.getMessage() + ": " + additional;
	}

    protected String additionalInfo()
    {
        return null;
    }

	public ServerFaultCode getServerFaultCode() {
		return serverFault;
	}
}
