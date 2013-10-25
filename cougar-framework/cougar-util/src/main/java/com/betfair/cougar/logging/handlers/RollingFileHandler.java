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

package com.betfair.cougar.logging.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import com.betfair.cougar.logging.rolling.RollingFileManager;
import com.betfair.cougar.logging.rolling.StreamInterceptor;

/**
 * Create a Handler for Rolling a file over on a time basis.
 */
public class RollingFileHandler extends StreamHandler implements StreamInterceptor {
    private long nextCheckForRollover = 0;
    private boolean flushAfterPublish;
    
    private RollingFileManager roller;

    public RollingFileHandler(String fileName,
                              boolean append,
                              boolean flush,
                              String policy,
                              Formatter formatter) throws IOException {
        if (formatter == null) {
            throw new IllegalArgumentException("formatter must not be null");
        }
        roller = new RollingFileManager(fileName, append, policy, this, getErrorManager());
        flushAfterPublish = flush;
        setFormatter(formatter);
        setLevel(Level.ALL);
    }

    public synchronized void publish(LogRecord record) {
        long n = record.getMillis();
        if (n >= nextCheckForRollover) {
        	nextCheckForRollover = roller.rolloverIfRequired(n);
        }
        super.publish(record);

        if (flushAfterPublish) {
            flush();
        }
    }

	@Override
	public void setStream(OutputStream os) {
		setOutputStream(os);
	}
	
	@Override
	public void closeStream() {
		close();
	}
}
