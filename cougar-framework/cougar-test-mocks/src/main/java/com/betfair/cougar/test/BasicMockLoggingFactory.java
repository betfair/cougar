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

package com.betfair.cougar.test;

import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingFactory;
import com.betfair.cougar.logging.CougarLoggingUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import java.util.HashMap;
import java.util.Map;

/**
 * This Logging factory implementation is designed to persist a map
 * of loggers, and will function like the default in that a new @see MockCapturingLogger
 * will be created if you request one that does not exist
 */
public class BasicMockLoggingFactory implements CougarLoggingFactory {
    private Map<String, CougarLogger> logMap = new HashMap<String, CougarLogger>();

    @Override
    public CougarLogger getLogger(String loggerName) {
        if (logMap.containsKey(loggerName)) {
            return logMap.get(loggerName);
        } else {
            MockCapturingLogger logger = new MockCapturingLogger(loggerName);
            logMap.put(loggerName, logger);
            return logger;
        }
    }

    @Override
    public void suppressAllRootLoggerOutput() {
        //kill log4j as well - libraries that use log4j this outside of slf4j demand this approach
        Logger.getRootLogger().addAppender(new NullAppender());
    }
}

