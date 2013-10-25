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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Impl of logger to capture logged messages
 */
public class MockCapturingLogger extends AbstractCougarLoggerImpl {
    private final List<LogRecord> logRecords;
    private Level logLevel;

    public MockCapturingLogger(String logName, List<LogRecord> logRecords, Level logLevel) {
        super(logName);
        this.logRecords = Collections.synchronizedList(logRecords);
        this.logLevel = logLevel;

    }

    public MockCapturingLogger(String logName, List<LogRecord> logRecords) {
        this(logName, logRecords, Level.INFO);
    }

    public MockCapturingLogger(String logName) {
        this(logName, new ArrayList<LogRecord>(), Level.INFO);
    }

    public List<LogRecord> getLogRecords() {
        return this.logRecords;
    }


    @Override
    protected void logInternal(LogRecord logRecord) {
        if (isLoggable(logRecord.getLevel()))
            logRecords.add(logRecord);
    }

    /* (non-Javadoc)
    * @see com.betfair.cougar.logging.NewCougarLogger#isLoggable(java.util.logging.Level)
    */
    @Override
    public boolean isLoggable(Level level) {
        return (level.intValue() >= logLevel.intValue());
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.CougarLogger#setLevel(java.util.logging.Level)
      */
    public void setLevel(Level newLevel) {
        this.logLevel = newLevel;
    }

    @Override
    public Level getLevel() {
        return logLevel;
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
        return null;
    }

    @Override
    public void addHandler(Handler handler) {
    }

    @Override
    public void flush() {
    }
}
