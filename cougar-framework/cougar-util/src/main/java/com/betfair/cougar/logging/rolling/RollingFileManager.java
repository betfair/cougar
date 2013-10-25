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

package com.betfair.cougar.logging.rolling;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.ErrorManager;

/**
 * Create a Manager for rolling a file over on a time basis.
 */
public class RollingFileManager {

    private enum RolloverPolicy {
        MINUTE, HOUR, DAY, MONTH
    }

    Date now = new Date();
    SimpleDateFormat sdf;
    final RollingCalendar rc;

    private String destinationFilenameOnRollover;
    private String fileName;
    
    private final ErrorManager errorManager;
    private final StreamInterceptor interceptor;
    

    public RollingFileManager(String fileName,
                              boolean append,
                              String policy,
                              StreamInterceptor interceptor,
                              ErrorManager errorManager) throws IOException {
        if (fileName == null) {
            throw new IllegalArgumentException("Filenane must not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor must not be null");
        }
        if (errorManager == null) {
            throw new IllegalArgumentException("errorManager must not be null");
        }
        File logDir = new File(fileName).getParentFile();
        if (logDir != null) {
	        if (!logDir.exists() && !logDir.mkdirs()) {
	        	throw new IllegalArgumentException("Unable to create directory "+logDir.getAbsolutePath());
	        }
	        if (!logDir.canWrite()) {
	        	throw new IllegalArgumentException("Directory "+logDir.getAbsolutePath()+" is not writable");
	        }
        }        
        this.fileName = fileName;
        this.errorManager = errorManager;
        this.interceptor = interceptor;
        
        RolloverPolicy currentPolicy = (RolloverPolicy.valueOf(policy));
        rc = new RollingCalendar(currentPolicy);
        now.setTime(System.currentTimeMillis());
        sdf = new SimpleDateFormat(rc.getRolloverPattern());

        setFile(fileName, append);
        
        File file = new File(fileName);
        destinationFilenameOnRollover = fileName + sdf.format(new Date(file.lastModified()));
    }

    private void setFile(String filename, boolean append) throws IOException {
    	interceptor.setStream(new BufferedOutputStream(new FileOutputStream(filename, append)));
    }


    /**
     * Rollover the current file to a new file if necessary.  Must be called from
     * within a synchronized block
     */
    public long rolloverIfRequired(long currentTime) {
    	now.setTime(currentTime);
        String datedFilename = fileName + sdf.format(now);
        // It is too early to roll over because we are still within the
        // bounds of the current interval. Rollover will occur once the
        // next interval is reached.
        if (destinationFilenameOnRollover.equals(datedFilename)) {
            return rc.getNextCheckMillis(now);
        }

        // close current file, and rename it to datedFilename
        interceptor.closeStream();

        File target = new File(destinationFilenameOnRollover);
        if (target.exists()) {
            target.delete();
        }

        File file = new File(fileName);
        boolean result = file.renameTo(target);
        boolean appendAsNewFileCreated = false;
        if (!result) {
            errorManager.error("Failed to rename [" + fileName + "] to [" + destinationFilenameOnRollover + "].", null, ErrorManager.CLOSE_FAILURE);
            appendAsNewFileCreated = true;
        }
        try {
        	// If the rename failed, we don't want to lose the log entries, so carry on appending... 
            setFile(fileName, appendAsNewFileCreated);
        } catch (IOException e) {
        	errorManager.error("Unable to set Output Stream for RollingFileHandler", e, ErrorManager.OPEN_FAILURE);
        }

        destinationFilenameOnRollover = datedFilename;
        return rc.getNextCheckMillis(now);
    }

	private static class RollingCalendar extends GregorianCalendar {
        private RolloverPolicy policy;

        RollingCalendar(RolloverPolicy policy) {
            super();
            this.policy = policy;
        }

        public long getNextCheckMillis(Date now) {
            return getNextCheckDate(now).getTime();
        }

        public Date getNextCheckDate(Date now) {
            this.setTime(now);

            switch (policy) {
                case MINUTE:
                    this.set(Calendar.SECOND, 0);
                    this.set(Calendar.MILLISECOND, 0);
                    this.add(Calendar.MINUTE, 1);
                    break;
                case HOUR:
                    this.set(Calendar.MINUTE, 0);
                    this.set(Calendar.SECOND, 0);
                    this.set(Calendar.MILLISECOND, 0);
                    this.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                case DAY:
                    this.set(Calendar.HOUR_OF_DAY, 0);
                    this.set(Calendar.MINUTE, 0);
                    this.set(Calendar.SECOND, 0);
                    this.set(Calendar.MILLISECOND, 0);
                    this.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case MONTH:
                    this.set(Calendar.DAY_OF_MONTH, 1);
                    this.set(Calendar.HOUR_OF_DAY, 0);
                    this.set(Calendar.MINUTE, 0);
                    this.set(Calendar.SECOND, 0);
                    this.set(Calendar.MILLISECOND, 0);
                    this.add(Calendar.MONTH, 1);
                    break;

                default:
                    throw new IllegalStateException("Unknown periodicity type for calendar.");
            }
            return getTime();
        }

        public String getRolloverPattern() {
            switch (policy) {
                case MINUTE:
                    return ".yyyy-MM-dd-HH-mm";
                case HOUR:
                    return ".yyyy-MM-dd-HH";
                case DAY:
                    return ".yyyy-MM-dd";
                case MONTH:
                    return ".yyyy-MM";
                default:
                    throw new IllegalStateException("Unknown periodicity type for rollover pattern.");
            }
        }
    }
}
