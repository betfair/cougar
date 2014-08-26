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

package com.betfair.cougar.logging.handlers;

import org.apache.log4j.*;

import java.io.IOException;

/**
 * Log4j implementation of an abstract log handler. delegates to an underlying DailyRollingFileAppender
 * on the rollover schedule supplied
 */
public class Log4JLogHandler extends AbstractLogHandler {

    private Appender appender;


    public enum RolloverPolicy {
        MINUTE("'.'yyyy-MM-dd-HH-mm"), HOUR("'.'yyyy-MM-dd-HH"), DAY("'.'yyyy-MM-dd"), MONTH("'.'yyyy-MM");

        private String log4jDatePattern;
        RolloverPolicy(String log4jDatePattern) {
            this.log4jDatePattern = log4jDatePattern;
        }

        public String getLog4jDatePattern() {
            return log4jDatePattern;
        }
    }

    private static final String DEFAULT_LOG_PATTERN = "%m%n";
    private Layout layout;

    private final String fileName;
    private final boolean append;
    private final boolean flush;
    private final RolloverPolicy policy;
    private final String logPattern;

    public Log4JLogHandler(String fileName, boolean flush, boolean append, RolloverPolicy policy, String logPattern, boolean abstractHandler) throws IOException {
        super(abstractHandler);
        this.fileName = fileName;
        this.append = append;
        this.policy = policy;
        this.flush = flush;
        this.logPattern = logPattern;

        if (!abstractHandler) {
            //Create a new appender for this concrete implementation
            this.appender = new DailyRollingFileAppender(getLayout(), fileName, policy.getLog4jDatePattern());
        }
    }

    public Log4JLogHandler(String fileName, boolean flush, boolean append, String policyStr, boolean abstractHandler) throws IOException {
        this(fileName, flush, append, RolloverPolicy.valueOf(policyStr), DEFAULT_LOG_PATTERN, abstractHandler);
    }

    public Log4JLogHandler(String fileName, boolean flush, boolean append, RolloverPolicy policy, boolean abstractHandler) throws IOException {
        this(fileName, flush, append, policy, DEFAULT_LOG_PATTERN, abstractHandler);
    }

    public Log4JLogHandler(String fileName, boolean flush, boolean append, String policyStr, String logPattern, boolean abstractHandler) throws IOException {
        this(fileName, flush, append, RolloverPolicy.valueOf(policyStr), logPattern, abstractHandler);
    }

    private Layout getLayout() {
        if (layout == null) {
            layout = new PatternLayout(logPattern);
        }
        return layout;
    }


    @Override
    protected AbstractLogHandler cloneHandlerToName(String logName, String serviceName, String namespace) throws IOException {
        String substitutedName = fileName.replace("##NAMESPACE##", namespace == null ? "" : "-"+namespace);
        substitutedName = substitutedName.replace("##NAME##", serviceName);

        //Note that this clone takes the logging properties from the parent - eg the abstract logger
        Log4JLogHandler eventLogHandler = new Log4JLogHandler(substitutedName, flush, append, policy, false);

        eventLogHandler.associateAppenderWithLogger(logName);

        return eventLogHandler;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Appender getAppender() {
        return appender;
    }

    public void associateAppenderWithLogger(String logName) {
        Logger logger = Logger.getLogger(logName);
        logger.addAppender(appender);
        logger.setAdditivity(false);
    }

}
