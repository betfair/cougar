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

package com.betfair.cougar.netutil.nio;

import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class NioLogger {
    public enum LoggingLevel {NONE, SESSION, TRANSPORT, PROTOCOL, ALL}

    private static final Logger logger = LoggerFactory.getLogger("SOCKET_TRANSPORT-LOG");

    private LoggingLevel logLevel;
    private JMXControl jmxControl;


    public NioLogger(String logLevel) {
        this.logLevel = LoggingLevel.valueOf(logLevel);
    }

    @ManagedAttribute
    public String getLogLevel() {
        return logLevel.name();
    }

    @ManagedAttribute
    public void setLogLevel(String logLevel) {
        this.logLevel = LoggingLevel.valueOf(logLevel);
    }

    public boolean isLogging(LoggingLevel logLevel) {
        return this.logLevel.ordinal() >= logLevel.ordinal();
    }

    public void log(LoggingLevel logLevel, IoSession session, String message, Object... args) {
        log(logLevel, NioUtils.getSessionId(session), message, args);
    }

    public void log(LoggingLevel logLevel, String sessionId, String message, Object... args) {
        if (isLogging(logLevel)) {
            logger.info(sessionId + ": " + String.format(message, args));
        }
    }

    public JMXControl getJmxControl() {
        return jmxControl;
    }

    public void setJmxControl(JMXControl jmxControl) {
        this.jmxControl = jmxControl;
    }
}