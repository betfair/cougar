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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class is a JDK based log implementation
 */
public class CougarLoggerImpl extends AbstractCougarLoggerImpl {
    private final Logger logger;


    CougarLoggerImpl(String loggerName) {
        super(loggerName);
        logger = Logger.getLogger(loggerName);
    }

    @Override
    protected void logInternal(LogRecord logRecord) {
        logger.log(logRecord);
    }

    /* (non-Javadoc)
    * @see com.betfair.cougar.logging.NewCougarLogger#isLoggable(java.util.logging.Level)
    */
    @Override
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    public void setLevel(Level newLevel) {
        logger.setLevel(newLevel);
    }

    @Override
    public Level getLevel() {
        Level level = logger.getLevel();
        while (level == null) {
            level = logger.getParent().getLevel();
        }
        return level;

    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.CougarLogger#setEventForwarding(boolean)
      */
    @Override
    public void setEventForwarding(boolean forward) {
        logger.setUseParentHandlers(forward);
    }

    /* (non-Javadoc)
    * @see com.betfair.cougar.logging.CougarLogger#removeHandlers(java.lang.String)
    */
    @Override
    public int removeHandlers() {
        int numRemoved = 0;
        for (Handler h : logger.getHandlers()) {
            h.close();
            logger.removeHandler(h);
            ++numRemoved;
        }
        return numRemoved;
    }

    @Override
    public Handler[] getHandlers() {
        return logger.getHandlers();
    }

    @Override
    public void flush() {
        for (Handler h : logger.getHandlers()) {
            h.flush();
        }
    }

    /* (non-Javadoc)
    * @see com.betfair.cougar.logging.CougarLogger#addHandler(java.util.logging.Handler)
    */
    @Override
    public void addHandler(Handler handler) {
        logger.addHandler(handler);
    }


}
