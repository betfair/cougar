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

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a mock factory for testing log implementations.
 * It must be wired in by setting the following System parameter
 * cougar.log.factory = <this.fully.qualified.classname>
 * alternatively, add a classpath resource called CougarLoggingFactoryFQCN.txt
 * which points to this class
 */
public class MockLoggingFactory implements CougarLoggingFactory {
    private CougarLogger logger;

    private Map<String, CougarLogger> logMap = new HashMap<String, CougarLogger>();

    public MockLoggingFactory() {
    }


    @Override
    public CougarLogger getLogger(String loggerName) {
        if (logMap.containsKey(loggerName)) {
            return logMap.get(loggerName);
        } else {
            MockCapturingLogger l = new MockCapturingLogger(loggerName);
            logMap.put(loggerName, l);
            return l;
        }
    }

    @Override
    public void suppressAllRootLoggerOutput() {
        //kill log4j as well - libraries that use log4j this outside of slf4j demand this approach
        Logger.getRootLogger().addAppender(new NullAppender());
    }

}
