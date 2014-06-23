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

package com.betfair.cougar.testing;

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.caching.CacheFrameworkIntegration;
import com.betfair.cougar.caching.CacheFrameworkRegistry;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.testingservice.v1.TestingService;
import com.betfair.testingservice.v1.enumerations.TestingExceptionErrorCodeEnum;
import com.betfair.testingservice.v1.exception.TestingException;
import com.betfair.testingservice.v1.to.CallResponse;
import com.betfair.testingservice.v1.to.IDD;
import com.betfair.testingservice.v1.to.LogFileResponse;
import com.betfair.tornjak.monitor.*;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

@ManagedResource
public class TestingServiceImpl implements TestingService {
    final static Logger LOGGER = LoggerFactory
            .getLogger(TestingServiceImpl.class);
    private CacheFrameworkRegistry cacheFrameworkRegistry;

    private String baseLogDirectory = null;
    private boolean doSkipLogLines = false;
    private long maxMessageSize = 500;
    private long defaultMaxNumberOfResults = Long.MAX_VALUE;
    private String logDateTimeFormat = null;

    public void setCacheFrameworkRegistry(CacheFrameworkRegistry cacheFrameworkRegistry) {
        this.cacheFrameworkRegistry = cacheFrameworkRegistry;
    }

    public String getLogDateTimeFormat() {
        return logDateTimeFormat;
    }

    public void setLogDateTimeFormat(String logDateTimeFormat) {
        this.logDateTimeFormat = logDateTimeFormat;
    }

    public long getDefaultMaxNumberOfResults() {
        return defaultMaxNumberOfResults;
    }

    public void setDefaultMaxNumberOfResults(long defaultMaxNumberOfResults) {
        this.defaultMaxNumberOfResults = defaultMaxNumberOfResults;
    }

    public void setDoSkipLogLines(boolean doSkipLogLines) {
        this.doSkipLogLines = doSkipLogLines;
    }

    public void setMaxMessageSize(long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    private boolean isDoSkipLogLines() {
        return doSkipLogLines;
    }

    private long getMaxMessageSize() {
        return maxMessageSize;
    }

    // injected via Spring

    public void setBaseLogDirectory(String logDirectory) {
        this.baseLogDirectory = logDirectory;
    }

    public String getBaseLogDirectory() {
        return this.baseLogDirectory;
    }

    @Override
    public void init(ContainerContext cc) {
    }

    @Override
    public CallResponse refreshCache(RequestContext ctx, String name, TimeConstraints timeConstraints)
            throws TestingException {
        CallResponse response = new CallResponse();
        boolean found = false;
        for (CacheFrameworkIntegration framework : cacheFrameworkRegistry.getFrameworks()) {
            if (framework.refreshNamedCache(name)) {
                found = true;
            }
        }

        if (!found) {
            throw new TestingException(ResponseCode.NotFound,
                    TestingExceptionErrorCodeEnum.NOT_FOUND);
        } else {
            response.setResult("OK");
        }
        return response;
    }

    @Override
    public IDD getIDD(RequestContext ctx, String name, TimeConstraints timeConstraints) throws TestingException {
        InputStream iddStream = getClass().getResourceAsStream("/idd/" + name);
        LOGGER.debug("Retriving IDD {}", "/idd/" + name);
        if (iddStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    iddStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                throw new TestingException(e, ResponseCode.InternalError,
                        TestingExceptionErrorCodeEnum.GENERIC);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.warn( "Failed to close input stream",
                                    e);
                    // Swallow it in case the it overrides the main exception
                }
            }
            IDD idd = new IDD();
            idd.setName(name);
            idd.setContent(sb.toString());
            return idd;
        }
        LOGGER.debug("IDD {} not found", "idd/" + name);
        throw new TestingException(ResponseCode.NotFound,
                TestingExceptionErrorCodeEnum.NOT_FOUND);

    }

    @Override
    public CallResponse refreshAllCaches(RequestContext ctx, TimeConstraints timeConstraints)
            throws TestingException {
        CallResponse response = new CallResponse();
        boolean done = false;
        for (CacheFrameworkIntegration framework : cacheFrameworkRegistry.getFrameworks()) {
            done = true;
            framework.refreshAllCaches();
        }
        if (done) {
            LOGGER.info("Refreshed all caches");
            response.setResult("OK");
            return response;
        }
        LOGGER.info("No cache frameworks found");
        throw new TestingException(ResponseCode.InternalError,
                TestingExceptionErrorCodeEnum.GENERIC);
    }

    @Override
    public LogFileResponse getLogEntriesByDateRange(RequestContext ctx,
                                                    String logFileName, String startDateTime, String endDateTime, TimeConstraints timeConstraints)
            throws TestingException {
        List<String> logLines = null;
        String physicalLogFileName = this.getBaseLogDirectory() + logFileName;
        String logMessage = "Request for " + startDateTime + " to "
                + endDateTime + " of logfile " + physicalLogFileName;
        LOGGER.info(logMessage);

        List<LogEntryCondition> conditions = new ArrayList<LogEntryCondition>();
        if (startDateTime != null) {
            StartDateTimeLogEntryCondition startCond = new StartDateTimeLogEntryCondition(
                    this.getLogDateTimeFormat());
            startCond.setCheckDate(startDateTime);
            if (startCond.getCheckDate() != null) {
                conditions.add(startCond);
            } else {
                LOGGER.info("Couldn't parse " + startDateTime
                        + " to date");
            }
        }
        if (endDateTime != null) {
            EndDateTimeLogEntryCondition endCond = new EndDateTimeLogEntryCondition(
                    this.getLogDateTimeFormat());
            endCond.setCheckDate(endDateTime);
            if (endCond.getCheckDate() != null) {
                conditions.add(endCond);
            } else {
                LOGGER.info("Couldn't parse " + endDateTime
                        + " to date");
            }
        }
        logLines = getLogFilesConditionalImpl(physicalLogFileName, conditions,
                this.getDefaultMaxNumberOfResults());
        LogFileResponse lfr = new LogFileResponse();
        lfr.setResult(logLines);
        return lfr;
    }

    // this method does a short cut implementation - it can skip much of the
    // start of the file

    @Override
    public LogFileResponse getLogEntries(RequestContext ctx, String logFileName,
                                         Integer numberOfLines, TimeConstraints timeConstraints) throws TestingException {
        long start = System.nanoTime();
        List<String> logLines = null;
        String physicalLogFileName = this.getBaseLogDirectory() + logFileName;
        StringBuilder logMessageBuilder = new StringBuilder("Request for ");
        logMessageBuilder.append(numberOfLines.intValue());
        logMessageBuilder.append(" lines of logfile ");
        logMessageBuilder.append(physicalLogFileName);
        LOGGER.info(logMessageBuilder.toString());
        LOGGER.debug("Do skip lines : " + this.isDoSkipLogLines());
        LOGGER.debug("Max entry size : " + this.getMaxMessageSize());

        logLines = getLogFilesSkippingReader(physicalLogFileName, numberOfLines
                .intValue());
        long end = System.nanoTime();
        LOGGER.info("Took " + (end - start) + " ns");

        LogFileResponse lfr = new LogFileResponse();
        lfr.setResult(logLines);
        return lfr;
    }

    /**
     * getLogFilesConditionalImpl does a generic search of log file using a set
     * of conditions Simple implementation using a rolling buffer and FileReader
     *
     * @param filename
     * @param conditions
     * @param maxEntriesReturned
     * @return List of log file entries
     * @throws TestingException
     */
    private List<String> getLogFilesConditionalImpl(String filename,
                                                    final List<LogEntryCondition> conditions, long maxEntriesReturned)
            throws TestingException {
        List<String> loglineBuffer = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            loglineBuffer = getLogFilesBufferedReaderImpl(br, conditions, maxEntriesReturned);
        } catch (IOException iox) {
            LOGGER.error("",iox);
            throw new TestingException(iox,ResponseCode.NotFound,
                    TestingExceptionErrorCodeEnum.NOT_FOUND);
        } finally {
        	if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.warn("Error while closing file "+filename, e);
				}
        	}
        }
        return loglineBuffer;
    }

    // note - if there are no conditions it automatically matches...

    private boolean allConditionsMatch(String logLine,
                                       final List<LogEntryCondition> conditions) {
        boolean allMatch = true;
        if (conditions != null) {
            Iterator<LogEntryCondition> iter = conditions.iterator();
            while (iter.hasNext()) {
                if (((LogEntryCondition)iter.next()).matchesEntry(logLine) == false) {
                    allMatch = false;
                    break;
                }
            }
        }
        return allMatch;
    }

    /**
     * Simple implementation of moving "window" buffer
     * Reads file from the start,  keeps last x entries that match conditions
     *
     * @param reader
     * @param conditions
     * @param maxEntriesReturned
     * @return
     * @throws IOException
     */
    private List<String> getLogFilesBufferedReaderImpl(BufferedReader reader,
                                                       final List<LogEntryCondition> conditions, long maxEntriesReturned)
            throws IOException {
        // rolling buffer of last #maxEntriesReturned# log entries that match
        LinkedList<String> loglineBuffer = new LinkedList<String>();
        if (reader != null) {
            int currentSize = 0;
            long excessRows = 0;
            String thisLine;
            while ((thisLine = reader.readLine()) != null) {
                if (allConditionsMatch(thisLine, conditions)) {
                    LOGGER.debug("All conditions match " + thisLine);
                    if (currentSize >= maxEntriesReturned) {
                        loglineBuffer.removeFirst();
                        excessRows++;
                    }
                    loglineBuffer.addLast(thisLine);
                    currentSize = loglineBuffer.size();
                }
            }
            LOGGER.info(excessRows
                    + " rows ejected due to size limit");
        }
        return loglineBuffer;
    }

    /**
     * getLogFilesSkippingReader Special case where it just wants last n rows of
     * the logfile So can skip a large chunk of it Simple implementation using
     * FileReader and rolling buffer
     *
     * @param filename
     * @param numLines
     * @return
     * @throws TestingException
     */
    private List<String> getLogFilesSkippingReader(String filename, int numLines)
            throws TestingException {
        List<String> loglineBuffer = null;
        BufferedReader br = null;
        try {
			br = new BufferedReader(new FileReader(filename));
            // should do initial skip if file is miles larger than wanted log
            // lines
            if (this.isDoSkipLogLines()) {
                File rawFile = new File(filename);
                long fileSize = rawFile.length();
                rawFile = null;
                long likelyLength = numLines * this.getMaxMessageSize() * 2;
                if (fileSize > likelyLength) {
                    long skipSize = fileSize - likelyLength;
                    if (skipSize < fileSize) {
                        LOGGER.info("Skipping " + skipSize
                                + " in file");
                        br.skip(skipSize);
                    }
                }
            }
            loglineBuffer = getLogFilesBufferedReaderImpl(br, null, numLines);
        } catch (IOException iox) {
            LOGGER.error("",iox);
            throw new TestingException(iox,ResponseCode.NotFound,
                    TestingExceptionErrorCodeEnum.NOT_FOUND);
        } finally {
        	if(br != null) {
        		try {
        			br.close();
        		} catch(IOException ex) {
        			LOGGER.warn("Error while closing file "+filename, ex);
        		}
        	}
        }
        return loglineBuffer;
    }

}
