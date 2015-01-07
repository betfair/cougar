/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.logging.EventLogDefinition;
import com.betfair.cougar.logging.EventLoggingRegistry;
import com.betfair.cougar.logging.records.EventLogRecord;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.HeaderUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

@ManagedResource
public class HttpRequestLogger implements RequestLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestLogger.class);

    private boolean loggingEnabled;
    private EventLoggingRegistry registry;
    private AtomicLong httpRequests = new AtomicLong();
    private List<String> headersToLog = new ArrayList<String>();

    private static ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

    public HttpRequestLogger(EventLoggingRegistry registry, boolean loggingEnabled) {
        this.registry = registry;
        this.loggingEnabled = loggingEnabled;
    }

    public static ILoggerFactory setLoggerFactory(ILoggerFactory loggerFactory) {
        ILoggerFactory ret = loggerFactory;
        HttpRequestLogger.loggerFactory = loggerFactory;
        return ret;
    }

    @ManagedAttribute
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @ManagedAttribute
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public void logAccess(final HttpCommand command,
            final ExecutionContext context, final long bytesRead,
            final long bytesWritten, final MediaType requestMediaType,
            final MediaType responseMediaType, final ResponseCode responseCode) {
        if (loggingEnabled) {

            LoggableEvent le = new LoggableEvent() {

                @Override
                public String getLogName() {
                    return "ACCESS-LOG";
                }

                @Override
                public Object[] getFieldsToLog() {

                    GeoLocationDetails location = context != null ? context.getLocation() : null;

                    // This is a bit (deeply!) ugly, but it is necessary as it is not possible to know
                    // if a response is gzipped until after it is written. Therefore the only other solution
                    // would be to pollute the command object with a getCompression() method, which
                    //   (a) seems overkill for a simple logger.
                    //   (b) just moves the problem to the jetty transport.
                    String compression = "none";
                    try {
                        if (command.getResponse().getOutputStream().getClass().getSimpleName().contains("GzipStream")) {
                            compression = "gzip";
                        }
                    } catch (Exception e) {
                        // ho hum. Let's assume it's not compressed
                    }

                    // Check the extra loggable fields.
                    List<String> extraFields;
                    if (headersToLog.isEmpty()) {
                        extraFields = Collections.emptyList();
                    } else {
                        extraFields = new ArrayList<String>();
                        for (String headerName: headersToLog) {
                            String value = HeaderUtils.cleanHeaderValue(command.getRequest().getHeader(headerName));
                            if (value != null && value.length() > 0) {
                                extraFields.add(headerName+"="+value);
                            }
                        }
                    }
                    return new Object[] {
                            command.getTimer().getReceivedTime(),
                            context != null ? context.getRequestUUID().toCougarLogString() : "",
                            command.getFullPath(),
                            compression,
                            location != null ? location.getRemoteAddr() : "",
                            location != null ? format(location.getResolvedAddresses()) : "",
                            location != null ? location.getCountry() : "",
                            responseCode,
                            command.getTimer().getProcessTimeNanos(),
                            bytesRead,
                            bytesWritten,
                            requestMediaType != null ? requestMediaType.getSubtype() : "",
                            responseMediaType != null ? responseMediaType.getSubtype() : "",
                            extraFields};
                }
            };
            EventLogRecord eventLogRecord = new EventLogRecord(le, null);
            EventLogDefinition invokableLogger = registry.getInvokableLogger(eventLogRecord.getLoggerName());
            if (invokableLogger != null) {
                loggerFactory.getLogger(invokableLogger.getLogName()).info(eventLogRecord.getMessage());
            } else {
                throw new CougarFrameworkException("Logger "+eventLogRecord.getLoggerName()+" is not an event logger");
            }
        }
        httpRequests.incrementAndGet();
    }

    private String format(List<String> resolvedAddresses) {
        String result = "";
        if (!(resolvedAddresses == null || resolvedAddresses.isEmpty())) {
            StringBuilder sb = new StringBuilder();
            for (String address : resolvedAddresses) {
                sb.append(address);
                sb.append(";");
            }
            int length = sb.length();
            sb.deleteCharAt(length-1);
            result = sb.toString();
        }
        return result;
    }

    @ManagedOperation
    public String addHeaderToLog(String header) {
        if (header != null && header.trim().length() > 0) {
            String trimmed = header.trim();
            if (!headersToLog.contains(trimmed)) {
                headersToLog.add(trimmed);
                LOGGER.info("Added loggable field '"+trimmed+"' to http access log");
            }
        }
        return headersToLog.toString();
    }

    @ManagedOperation
    public String removeHeaderToLog(String header) {
        if (header != null && header.trim().length() > 0) {
            String trimmed = header.trim();
            if (headersToLog.contains(trimmed)) {
                headersToLog.remove(trimmed);
                LOGGER.info("Removed loggable field '"+trimmed+"' from http access log");
            }
        }
        return headersToLog.toString();
    }

    public void setHeadersToLog(String headers) {
        if (headers != null) {
            String[] headerList = headers.split(",");
            headersToLog = new ArrayList<String>();
            for (String h: headerList) {
                addHeaderToLog(h);
            }
        }
    }

    @ManagedAttribute
    public String getHeadersToLog() {
        return headersToLog.toString();
    }

    @ManagedAttribute
    public long getHttpRequests() {
        return httpRequests.get();
    }
}
