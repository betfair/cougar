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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.configuration.PropertyConfigurer;
import org.springframework.beans.factory.InitializingBean;

import java.util.Enumeration;
import java.util.Map;

/**
 * Abstract base class of a log level controller
 */
public abstract class AbstractLoggingControl implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoggingControl.class);

    private static final String CUSTOM_LOG_LEVEL = "cougar.log.level.";

    public void setLogLevels(Map<String, String> logLevels) {
        for (Map.Entry<String, String> e : logLevels.entrySet()) {
            setLogLevel(e.getKey(), e.getValue(), false);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, String> props = PropertyConfigurer.getAllLoadedProperties();
        LOGGER.info("Properties loaded from config files and system property overrides");
        for (Map.Entry<String, String> e: props.entrySet()) {
        	checkEntry(e.getKey(), e.getValue());
        }
        Enumeration e = System.getProperties().keys();
        while(e.hasMoreElements()) {
        	String key = (String)e.nextElement();
        	checkEntry(key, System.getProperty(key));
        }
    }

    private void checkEntry(String key, String value) {
        if (key.startsWith(CUSTOM_LOG_LEVEL)) {
            String loggerName = key.substring(CUSTOM_LOG_LEVEL.length());
            setLogLevel(loggerName, value, false);
        }
    }

    public abstract void setLogLevel(String loggerName, String level, boolean recursive);

    public abstract String getLogLevel(String loggerName);

}
