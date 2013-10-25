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


import org.apache.commons.logging.impl.SLF4JLogFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerFactory;

/**
 * SLF logging factory implementation to create SLF4J Cougar loggers
 */
public class Slf4jLoggingFactory implements CougarLoggingFactory {
    @Override
    public CougarLogger getLogger(String loggerName) {
        return new Slf4jCougarLoggingImpl(loggerName);
    }

    @Override
    public void suppressAllRootLoggerOutput() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (iLoggerFactory instanceof Log4jLoggerFactory) {
            org.apache.log4j.Logger.getRootLogger().addAppender(new org.apache.log4j.varia.NullAppender());
        }
    }
}
