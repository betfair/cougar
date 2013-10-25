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

import com.betfair.cougar.logging.records.ExceptionLogRecord;
import com.betfair.cougar.logging.records.SimpleLogRecord;
import com.betfair.cougar.logging.records.TraceLogRecord;
import com.betfair.cougar.logging.records.TrackingLogRecord;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Abstract logging implementation that provides most of the
 * boilerplate code necessary to produce a cougar logging implementation
 */
public abstract class AbstractCougarLoggerImpl implements CougarLogger {

    private final String loggerName;


    protected AbstractCougarLoggerImpl(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public String getLogName() {
        return loggerName;
    }

    /* (non-Javadoc)
    * @see com.betfair.cougar.logging.NewCougarLogger#log(java.util.logging.Level, java.lang.String, java.lang.Object)
    */
    @Override
    public void log(Level level, String msg, Object... args) {
        logEvent(new SimpleLogRecord(loggerName, level, msg, args));
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#log(java.lang.Throwable)
      */
    @Override
    public void log(Throwable exception) {
        logEvent(new ExceptionLogRecord(loggerName, exception));
    }


    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#log(java.util.logging.Level, java.lang.String, java.lang.Throwable)
      */
    @Override
    public void log(Level level, String msg, Throwable exception, Object... args) {
        logEvent(new ExceptionLogRecord(loggerName, level, msg, exception, args));
    }


    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#entering(java.lang.String, java.lang.String, java.lang.Object)
      */
    @Override
    public void entering(String sourceClass, String sourceMethod, Object... args) {
        logEvent(new TrackingLogRecord(loggerName, TrackingLogRecord.Action.ENTERING, sourceClass, sourceMethod, args));
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#exiting(java.lang.String, java.lang.String, java.lang.Object)
      */
    @Override
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        logEvent(new TrackingLogRecord(loggerName, TrackingLogRecord.Action.LEAVING, sourceClass, sourceMethod, result));
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#throwing(java.lang.String, java.lang.String, java.lang.Throwable)
      */
    @Override
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        logEvent(new TrackingLogRecord(loggerName, TrackingLogRecord.Action.THROWING, sourceClass, sourceMethod, thrown));
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#trace(java.lang.String, java.lang.Object)
      */
    @Override
    public void forceTrace(String traceId, String msg, Object... args) {
        logEvent(new TraceLogRecord(traceId, msg, args));
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.NewCougarLogger#log(java.util.logging.LogRecord)
      */
    @Override
    public void log(LogRecord record) {
        logEvent(record);
    }

    private void logEvent(LogRecord record) {
        // Add to the trace log if it's an explicit Trace log event or if this thread is currently tracing
        String traceId = CougarLoggingUtils.getTraceId();
        TraceLogRecord tlr = null;
        if (record instanceof TraceLogRecord) {
            tlr = (TraceLogRecord)record;
        } else if (traceId != null) {
            tlr = new TraceLogRecord(traceId, record.getMessage(), record.getThrown());
        }

        if (tlr != null) {
            CougarLogger traceLogger = CougarLoggingUtils.getTraceLogger();
            if (traceLogger != null) {
                ((AbstractCougarLoggerImpl)traceLogger).logInternal(tlr);
            }
        }

        // Don't want to log explicit trace events in the main log
        if (!(record instanceof TraceLogRecord)) {
            logInternal(record);
        }
    }

    protected abstract void logInternal(LogRecord logRecord);
}
