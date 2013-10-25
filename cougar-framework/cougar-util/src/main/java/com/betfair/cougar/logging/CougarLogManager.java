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

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.ErrorManager;

/**
 * Keep a buffer of the last reported logging errors (ie. errors arising from logging, NOT application
 * errors). Print some of them to Std err, but not all, as we don't want a flood. Once a second seems ok
 */
@ManagedResource
public class CougarLogManager extends ErrorManager {

    private static final long NANO_TO_MILLI=1000000;

    private static final CougarLogManager instance = new CougarLogManager();
    private static final String SEPARATOR = System.getProperty("line.separator");

	private final Object lock = new Object();

    private volatile long messageInterval = 1000;
    private volatile long maxStoredErrors = 100;

    private long nextMessage = 0;

    /**
     * Independent of other logic, just for reporting.
     */
    private final AtomicLong numErrors = new AtomicLong(0);

    private final LinkedList<StoredError> storedErrors = new LinkedList<StoredError>();

    private String baseLogDirectory;


	CougarLogManager() {
        // Prevent instantiation
    }

    public static CougarLogManager getInstance() {
        return instance;
    }

    @ManagedAttribute
    public String getBaseLogDirectory() {
		return baseLogDirectory;
	}

    public void setBaseLogDirectory(String logBaseDirectory) {
		this.baseLogDirectory = new File(logBaseDirectory).getAbsolutePath();
	}

	@ManagedAttribute
    public void setMessageInterval(long messageInterval) {
        this.messageInterval = messageInterval;
    }

    @ManagedAttribute
    public void setMaxStoredErrors(long maxStoredErrors) {
        this.maxStoredErrors = maxStoredErrors;
    }

    @ManagedAttribute
    public long getMessageInterval() {
		return messageInterval;
	}

    @ManagedAttribute
	public long getMaxStoredErrors() {
		return maxStoredErrors;
	}

	@ManagedAttribute
    public String[] getStoredErrors() {

		List<StoredError> errors;
		synchronized (lock) {
	         errors = new ArrayList<StoredError>(storedErrors);
		}

        String[] result = new String[errors.size()];
        for (int i = 0; i < errors.size(); ++i) {
            result[i] = errors.get(i).toString();
        }
        return result;
    }

    @ManagedAttribute
    public long getNumErrors() {
        return numErrors.longValue();
    }

    @ManagedOperation
    public void clear() {
    	synchronized (lock) {
            storedErrors.clear();
    	}
        numErrors.set(0);
    }

    @Override
    public void error(String msg, Exception ex, int code) {

    	numErrors.incrementAndGet();

        StoredError se = new StoredError(code, msg, ex);
    	boolean printMsg = false;

    	synchronized (lock) {

    		// update list of errors
            storedErrors.offer(se);
            while (storedErrors.size() > maxStoredErrors) {
                storedErrors.poll();
    		}

            // update times
            long nanoTime = nanoTime();
            if (nanoTime >= nextMessage) {
            	printMsg = true;
            	nextMessage = nanoTime + (messageInterval * NANO_TO_MILLI);
            }
    	}

        if (printMsg) {
            System.err.println("Logging Error: "+se);
        }
    }


    protected long nanoTime() {
        return System.nanoTime();
    }

    private static class StoredError {
        private final int code;
        private final String message;
        private final Exception exception;

        private StoredError(int code, String message, Exception exception) {
            this.code = code;
            this.message = message;
            this.exception = exception;
        }

        @Override
        public String toString() {
            String result = code +"- "+message;
            if (exception != null) {
               StringWriter sw = new StringWriter();
			   exception.printStackTrace(new PrintWriter(sw));
               result += SEPARATOR + sw.toString();
            }
            return result;
        }
    }
}
