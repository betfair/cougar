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

import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.logging.handlers.Log4JLogHandler;
import org.apache.log4j.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *  CougarLog4JBootstrap class is used to initialise the log4j's default appender
 *  for the RootLogger. Users can override this class, and should if they don't
 *  want to use log4j, or their implementation already has a default appender
 *  configured for the RootLogger.
 *
 *  If you want to initialise something other than log4j, then initialise
 *  <code>cougar.core.log.bootstrap.class</code> System variable to point to your implementation
 *
 */
public class CougarLog4JBootstrap implements LogBootstrap {

    public static final String DEFAULT_LOGGING_PATTERN     = "%d{yyyy-MM-dd HH:mm:ss.SSS}: %c %p - %m%n";
    public static final String DEFAULT_LOG_FILENAME_FORMAT = "{0}/{1}-{2}-server.log";
    public static final String DEFAULT_ROTATION_POLICY     = "'.'yyyy-MM-dd";
    public static final String DEFAULT_LOG_LEVEL           = "INFO";


    public static final String APP_NAME_PROPERTY               = "cougar.app.name";
    public static final String LOG_DIR_PROPERTY                = "cougar.log.dir";
    public static final String LOG_LEVEL_PROPERTY              = "cougar.log.level";
    public static final String LOGGING_PATTERN_PROPERTY        = "cougar.log.SERVER.pattern";
    public static final String FILENAME_FORMAT_PROPERTY        = "cougar.log.SERVER.filename.format";
    public static final String ROTATION_POLICY_FORMAT_PROPERTY = "cougar.log.SERVER.rotation";

    public static final String ECHO_TO_STDOUT_PROPERTY         = "cougar.log.echoToStdout";

    private String getValueOrDefault(Properties properties, String propertyName, String defaultValue) {
        if (properties.containsKey(propertyName)) {
            String propertyValue = properties.getProperty(propertyName);
            return propertyValue.isEmpty() ? defaultValue : propertyValue;
        } else {
            return defaultValue;
        }
    }


    public void init(Properties properties) throws IOException {
        String hostname = InetAddress.getLocalHost().getHostName();
        String appName = properties.getProperty(APP_NAME_PROPERTY);
        String logDir = properties.getProperty(LOG_DIR_PROPERTY);
        String pattern = getValueOrDefault(properties, LOGGING_PATTERN_PROPERTY, DEFAULT_LOGGING_PATTERN);
        String filenameFormat = getValueOrDefault(properties, FILENAME_FORMAT_PROPERTY, DEFAULT_LOG_FILENAME_FORMAT);
        String rotationPolicy = Log4JLogHandler.RolloverPolicy.valueOf(getValueOrDefault(properties,
                ROTATION_POLICY_FORMAT_PROPERTY, DEFAULT_ROTATION_POLICY)).getLog4jDatePattern();
        String log4jLogLevelString = getValueOrDefault(properties, LOG_LEVEL_PROPERTY, DEFAULT_LOG_LEVEL);

        Level logLevel = Level.toLevel(log4jLogLevelString);

        if (logDir == null) {
            throw new PanicInTheCougar("Cannot start with " + LOG_DIR_PROPERTY + " being set - value is currently null");
        }

        Boolean echoToStdOut = Boolean.parseBoolean(getValueOrDefault(properties, ECHO_TO_STDOUT_PROPERTY, "false"));

        List<Appender> logAppenders = new ArrayList<Appender>();

        Layout layout = new PatternLayout(pattern);
        Appender appender = new DailyRollingFileAppender(
                layout,
                constructLogFilename(filenameFormat, logDir, hostname, appName),
                rotationPolicy);
        appender.setName("rootAppender");
        logAppenders.add(appender);

        if (echoToStdOut) {
            Appender consoleAppender = new ConsoleAppender(layout);
            consoleAppender.setName("consoleAppender");
            logAppenders.add(consoleAppender);
        }

        Logger logger = Logger.getRootLogger();
        for (Appender a : logAppenders) {
            logger.addAppender(a);
        }
        logger.setAdditivity(false);
        logger.setLevel(logLevel);
        if (echoToStdOut) {
            LoggerFactory.getLogger(getClass()).warn("Echoing all log output to stdout");
        }
    }

    public String constructLogFilename(String format, String directory, String hostname, String appName) {
        return MessageFormat.format(format, directory, hostname, appName);
    }
}
