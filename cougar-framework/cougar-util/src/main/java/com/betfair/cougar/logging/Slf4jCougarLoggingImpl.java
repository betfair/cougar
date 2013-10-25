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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * SLF4J CougarLogging Implementation. Will map calls to the SLF4j log levels
 * and publish log messages through its API
 */
public class Slf4jCougarLoggingImpl extends AbstractCougarLoggerImpl {

    private Logger logger;
    private static final int offValue = Level.OFF.intValue();

    public static CougarLogger getLogger(String loggerName) {
        return new Slf4jCougarLoggingImpl(loggerName);
    }

    public Slf4jCougarLoggingImpl(String loggerName) {
        this(loggerName.equals("") ? LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) : LoggerFactory.getLogger(loggerName), loggerName);
    }

    Slf4jCougarLoggingImpl(Logger logger, String loggerName) {
        super(loggerName);
        this.logger = logger;
    }


    @Override
    protected void logInternal(LogRecord logRecord) {
        final Level level = logRecord.getLevel();
        if (isLoggable(level)) {
            if (level.intValue() >= Level.SEVERE.intValue()) {
                logger.error(logRecord.getMessage(), logRecord.getThrown());
            } else if (level.intValue() >= Level.WARNING.intValue()) {
                logger.warn(logRecord.getMessage(), logRecord.getThrown());
            } else if (level.intValue() >= Level.INFO.intValue()) {
                logger.info(logRecord.getMessage(), logRecord.getThrown());
            } else if (level.intValue() >= Level.FINER.intValue()) {
                //Please note that FINE(500) is also caught by FINER(400)
                logger.debug(logRecord.getMessage(), logRecord.getThrown());
            } else if (logger.isTraceEnabled()) {
                logger.trace(logRecord.getMessage(), logRecord.getThrown());
            }
        }
    }

    @Override
    public boolean isLoggable(Level level) {
      	int loggerLevel = getLevel().intValue();
        if (level.intValue() < loggerLevel || loggerLevel == offValue) {
            return false;
        }
        return true;
    }

    @Override
    public Level getLevel() {
        if (logger.isTraceEnabled()) {
            return Level.FINEST;
        } else if (logger.isDebugEnabled()) {
            return Level.FINER;
        } else if (logger.isInfoEnabled()) {
            return Level.INFO;
        } else if (logger.isWarnEnabled()) {
            return Level.WARNING;
        } else {
            return Level.SEVERE;
        }
    }

    @Override
    public void setEventForwarding(boolean forward) {
    }

    @Override
    public int removeHandlers() {
        return 0;
    }

    @Override
    public Handler[] getHandlers() {
        return new Handler[0];
    }

    @Override
    public void addHandler(Handler handler) {
    }

    @Override
    public void flush() {

    }
}
