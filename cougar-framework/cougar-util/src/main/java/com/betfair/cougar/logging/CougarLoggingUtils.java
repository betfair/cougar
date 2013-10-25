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

package com.betfair.cougar.logging;


import java.io.*;
import java.net.URL;
import java.util.logging.Level;


public class CougarLoggingUtils {
	
	private static final String NEWLINE = "\n";
	private static final String RETURN = "\r";
	
	private static final ThreadLocal<String> traceId =  new ThreadLocal<String>();
	private static CougarLogger TRACE_LOGGER;


    private static CougarLoggingFactory factory = new Slf4jLoggingFactory();

    public static final String LOGGING_FACTORY = "cougar.log.factory";

    private static final String LOGGING_FACTORY_PROPERTY_PATH = "CougarLoggingFactoryFQCN.txt";

    // This static initialisation block will resolve a logging factory in one of the following ways:
    // Try and find a CougarLoggingFactory class name by
    //   looking for a System property called logging factory
    //   if nothing was found, look for a classpath reference for a resource called CougarLoggingFactoryFQCN.txt
    //   if there is a class name, attempt to instantiate it
    //
    // Otherwise it will default SLF4JLoggingFactory - which is probably what you wanted anyway.
    //
    static {
        String factoryClass = System.getProperty(LOGGING_FACTORY);
        try {
            if (factoryClass == null) {
                URL file = CougarLoggingUtils.class.getClassLoader().getResource(LOGGING_FACTORY_PROPERTY_PATH);
                if (file != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(file.openStream()))) {
                        factoryClass = br.readLine();
                    }
                }
            }

            if (factoryClass != null) {
                CougarLoggingFactory clf = (CougarLoggingFactory) Class.forName(factoryClass).newInstance();
                factory = clf;
            }
        } catch (Exception e) {
            getLogger(CougarLoggingUtils.class).log(Level.WARNING, "Unable to instantiate log factory [" +
                    factoryClass + "] falling back to SLF4J logging");
        }
    }


	public static CougarLogger getLogger(String loggerName) {
		return factory.getLogger(loggerName);
	}
	
	public static CougarLogger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}
	
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
		if (TRACE_LOGGER != null) {
			TRACE_LOGGER.flush();
		}
	}
	
	public static String getTraceId() {
		return traceId.get();
	}
	
	public static CougarLogger getTraceLogger() {
		return TRACE_LOGGER;
	}

	public static void setTraceLogger(CougarLogger traceLogger) {
		if (traceLogger != null && TRACE_LOGGER != null) {
			throw new IllegalStateException("Trace logger is already defined - " + TRACE_LOGGER.getLogName());
		}
		TRACE_LOGGER = traceLogger;
	}

    public static void suppressAllRootLoggerOutput() {
        factory.suppressAllRootLoggerOutput();
    }
}
