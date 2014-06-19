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


import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerFactory;


public class CougarLoggingUtils {


	private static final ThreadLocal<String> traceId =  new ThreadLocal<String>();
	private static Logger TRACE_LOGGER;


	/**
	 * This call switches trace logging on for this <b>thread</b>. Simple logs
	 * the tradeId against the thread. If a traceId is stored, then it is assumed
	 * that tracing is switched on
	 */
	public static void startTracing(String traceID) {
		if (TRACE_LOGGER != null) {
			traceId.set(traceID);
		}
	}

	public static void stopTracing() {
		traceId.remove();
	}

	public static String getTraceId() {
		return traceId.get();
	}

	public static Logger getTraceLogger() {
		return TRACE_LOGGER;
	}

	public static void setTraceLogger(Logger traceLogger) {
		if (traceLogger != null && TRACE_LOGGER != null) {
			throw new IllegalStateException("Trace logger is already defined - " + TRACE_LOGGER.getName());
		}
		TRACE_LOGGER = traceLogger;
	}

    public static void suppressAllRootLoggerOutput() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (iLoggerFactory instanceof Log4jLoggerFactory) {
            org.apache.log4j.Logger.getRootLogger().addAppender(new org.apache.log4j.varia.NullAppender());
        }
    }
}
