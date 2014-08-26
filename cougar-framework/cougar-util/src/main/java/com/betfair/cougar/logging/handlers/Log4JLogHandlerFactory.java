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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * a log4j implementation of @See LogHandlerFactory
 */
public class Log4JLogHandlerFactory implements LogHandlerFactory {

    @Override
    public AbstractLogHandler registerLogHandler(Map<String, String> logConfig) throws IOException {

        String logName = logConfig.get("logName");
        String fileName  = logConfig.get("fileName");
        boolean flush = Boolean.valueOf(logConfig.get("flush"));
        boolean append = Boolean.valueOf(logConfig.get("append"));
        String rotation =  logConfig.get("rotation");
        boolean isAbstractLogger = Boolean.valueOf(logConfig.get("abstractLogger"));

        String logFormatPattern = logConfig.get("format");


        boolean isTraceLogger = false;
        String isTraceLoggerString = logConfig.get("isTraceLogger");
        if (isTraceLoggerString != null) {
            isTraceLogger = Boolean.valueOf(isTraceLoggerString);
        }

        //If the log is abstract, then the concrete logger creation step registers
        //the appender
        //If it is a real implementation here, then we'll associate the log4j appender with
        //the logger

        Log4JLogHandler handler = null;
        if (logFormatPattern != null && !logFormatPattern.equals("")) {
            handler = new Log4JLogHandler(fileName, flush, append, rotation, logFormatPattern, isAbstractLogger);
        } else {
            handler = new Log4JLogHandler(fileName, flush, append, rotation, isAbstractLogger);
        }

        if (!isAbstractLogger) {
            if (logName == null) {
                throw new IllegalArgumentException("logName mustn't be null for concrete log implementation. " + fileName);
            }
            handler.associateAppenderWithLogger(logName);
            if (isTraceLogger) {
                Logger l = Logger.getLogger(logName);
                l.setLevel(Level.TRACE);
            }
        }
        return handler;
    }
}
