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

package com.betfair.testing.utils.cougar.manager;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class LogTailer<T extends LogTailer.LogRequirement> implements TailerListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String DATE_FIELD = "_DATE_FIELD";

    private final AtomicLong idSource = new AtomicLong();

    private static final long DELAY = 100;
    private static final long BLOCK_TIME = DELAY*10;
    private Tailer tailer;
    private BlockingQueue<LogLine> inputQueue = new LinkedBlockingDeque<LogLine>();
    private CountDownLatch startupLatch = new CountDownLatch(1);

    protected LogTailer(File toRead, long timeForFileToBeCreated) throws IOException {
        long requiredTime = System.currentTimeMillis() + timeForFileToBeCreated;
        while ((System.currentTimeMillis() < requiredTime) && !(toRead.exists() && toRead.canRead())) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (!toRead.exists() || !toRead.canRead()) {
            throw new IllegalStateException("Couldn't read "+toRead.getCanonicalPath()+" in the configured timeout");
        }
        logger.debug("Initialising Tailer for "+toRead.getCanonicalPath());

        tailer = new Tailer(toRead, this, DELAY, false);
    }

    public void awaitStart() throws InterruptedException {
        Thread t = new Thread(tailer, getClass().getSimpleName());
        t.setDaemon(true);
        t.start();
        startupLatch.await();
    }

    @Override
    public void init(Tailer tailer) {
        startupLatch.countDown();
//        logger.debug(System.currentTimeMillis()+": Started!");
    }

    @Override
    public void fileNotFound() {
        // should never happen
    }

    @Override
    public void fileRotated() {
//        logger.debug(getClass().getSimpleName()+": Following file rotation");
    }

    @Override
    public void handle(String s) {
        logger.debug(System.currentTimeMillis()+": Line received: "+s);
        try {
            Map<String, String> fields = getFieldsForLine(s);
            if (fields == null) {
                logger.error(System.currentTimeMillis()+": Parsing error on line: "+s);
            }
            else {
                Timestamp datetime = toDate(fields.get(DATE_FIELD));
                LogLine line = new LogLine(datetime, fields);
                inputQueue.add(line);
            }
        } catch (ParseException e) {
            logger.error("",e);
        }
        logger.debug(System.currentTimeMillis()+": End of handle");
    }

    @Override
    public void handle(Exception e) {
        // todo: are we interested??
        e.printStackTrace();
    }

    public void lookForNoLogLines(Timestamp fromDate, long timeoutMs, T[] matchers) {
        lookForLogLines(fromDate, timeoutMs, new ArrayList<T>(), true, matchers);
    }

    public LogLine[] lookForLogLines(Timestamp fromDate, long timeoutMs, T... requirements) {
        return lookForLogLines(fromDate, timeoutMs, new ArrayList<T>(Arrays.asList(requirements)), requirements.length == 0, null);
    }

    private LogLine[] lookForLogLines(Timestamp fromDate, long timeoutMs, List<T> remainingRequirements, boolean expectingNoLines, T[] matchers) {
        List<LogLine> soFar = new LinkedList<>();
        // right, we look through the queue, testing each line to see if it matches the first requirement
        // each time we match a requirement, we discard it (in order)
        LogLine line;
        // allow a little time for the first caller to catchup on the log..
        while ((line = blockingPoll(inputQueue, BLOCK_TIME)) != null) {
            // make sure we only consider lines after fromDate
            if (line.getDatetime().before(fromDate)) {
                continue;
            }
            // before and after..
            if (remainingRequirements.isEmpty()) {
                if (!expectingNoLines) {
                    return soFar.toArray(new LogTailer.LogLine[soFar.size()]);
                }
            }
            else if (matches(line, remainingRequirements.get(0))) {
                soFar.add(line);
                remainingRequirements.remove(0);
            }
            // once we run out of requirements we exit cleanly
            if (remainingRequirements.isEmpty() && !expectingNoLines) {
//                logger.debug(System.currentTimeMillis()+": Found all lines we were looking for!");
                return soFar.toArray(new LogTailer.LogLine[soFar.size()]);
            }
        }
        // if we run out of queued lines or we're not expecting any lines, then we start waiting for input to come into the queue
        long endTime = System.currentTimeMillis() + timeoutMs;
        long remainingTime = timeoutMs;
        // as each line comes in we decrement the timeout remaining
        do {
            if (line != null) {
                if (expectingNoLines) {
                    if (matchers == null || matchers.length == 0) {
                        throw new IllegalStateException(new Date()+"."+System.currentTimeMillis()%1000+" Found a log line when I was expecting none: "+line);
                    }
                    else {
                        boolean gotAMatch = false;
                        for (T matcher : matchers) {
                            if (matches(line, matcher)) {
                                gotAMatch = true;
                            }
                        }
                        if (gotAMatch) {
                            throw new IllegalStateException(new Date()+"."+System.currentTimeMillis()%1000+" Found a log line when I was expecting none: "+line);
                        }
                    }
                }
                // make sure we only consider lines after fromDate
                if (line.getDatetime().before(fromDate)) {
                    continue;
                }
                if (matches(line, remainingRequirements.get(0))) {
                    remainingRequirements.remove(0);
                    soFar.add(line);
                }
                // once we run out of requirements we exit cleanly
                if (remainingRequirements.isEmpty()) {
//                        logger.debug(System.currentTimeMillis()+": Found all lines we were looking for!");
                    return soFar.toArray(new LogTailer.LogLine[soFar.size()]);
                }
            }
            line = blockingPoll(inputQueue, remainingTime);
        } while ((remainingTime = endTime - System.currentTimeMillis()) > 0);
        if (!expectingNoLines) {
            // if we run out of time then we fail..
            throw new IllegalStateException(new Date()+"."+System.currentTimeMillis()%1000+" Failed to find all log lines in time, remaining: "+remainingRequirements+", soFar: "+soFar);
        }
        return soFar.toArray(new LogTailer.LogLine[soFar.size()]);
    }

    protected <T> T blockingPoll(BlockingQueue<T> queue, long ms) {
        try {
            return queue.poll(ms,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        return null;
    }

    protected abstract Map<String, String> getFieldsForLine(String s);

    protected abstract Timestamp toDate(String dateFieldValue) throws ParseException;

    protected abstract boolean matches(LogLine line, T requirement);

    protected static interface LogRequirement {

    }

    public class LogLine implements Comparable<LogLine> {
        private long id = idSource.incrementAndGet();
        private Timestamp datetime;
        private Map<String, String> fields;

        public LogLine(Timestamp datetime, Map<String, String> fields) {
            this.datetime = datetime;
            this.fields = fields;
        }

        public Timestamp getDatetime() {
            return datetime;
        }

        public Map<String, String> getFields() {
            return fields;
        }

        @Override
        public int compareTo(LogLine o) {
            long diff = id - o.id;
            if (diff == 0) { return 0; };
            return diff > 0 ? 1 : -1;
        }
    }
}
