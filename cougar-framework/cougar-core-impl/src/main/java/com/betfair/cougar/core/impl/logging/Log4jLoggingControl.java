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

import com.betfair.cougar.logging.CougarLoggingUtils;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.xpath.internal.jaxp.JAXPVariableStack;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.impl.Log4jLoggerAdapter;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;


/**
 * Log4j file to programmatically set log levels for log4j. Most useful feature is that this is exposed via MBEAN
 */
@ManagedResource
public class Log4jLoggingControl extends AbstractLoggingControl {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractLoggingControl.class);

    public void setLogLevel(String loggerName, String level, boolean recursive) {
        //This implementation does not support recursive loglevel changes
        setLogLevel(loggerName, level);
    }

    @ManagedOperation(description = "Sets the level of logger (p1) to level (p2)")
    public void setLogLevel(String loggerName, String level) {
        Logger l = loggerName == null? Logger.getRootLogger() : Logger.getLogger(loggerName);

        logger.info("Logger {}: level customised to {}", l.getName(), level);


        l.setLevel(Level.toLevel(level));
    }

    @ManagedOperation
    public String getLogLevel(String loggerName) {
        Logger l = loggerName == null? Logger.getRootLogger() : Logger.getLogger(loggerName);

        return l.getLevel().toString();
    }


}
