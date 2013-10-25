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
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import java.util.HashMap;
import java.util.Map;

/**
 * Another mock logging factory implementation that consolidates all
 * loggers, except for the trace logger, into one log implementation.
 */
public class ConsolidatedMockLoggingFactory implements CougarLoggingFactory {
    private Map<String, CougarLogger> logMap = new HashMap<String, CougarLogger>();

    public static final String TRACE_LOG_NAME="TRACELOG";

    public ConsolidatedMockLoggingFactory() {
        logMap.put("", new MockCapturingLogger(""));
        logMap.put(TRACE_LOG_NAME, new MockCapturingLogger(TRACE_LOG_NAME));
    }


    @Override
    public CougarLogger getLogger(String loggerName) {
        if (logMap.containsKey(loggerName)) {
            return logMap.get(loggerName);
        } else {
            return logMap.get("");
        }
    }

    @Override
    public void suppressAllRootLoggerOutput() {
        //kill log4j as well - libraries that use this outside of slf4j demand this approach
        Logger.getRootLogger().addAppender(new NullAppender());
    }

}

