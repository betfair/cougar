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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.betfair.cougar.CougarUtilTestCase;

/**
 * RollingFileManager Tester.
 */
public class RollingFileManagerTest extends CougarUtilTestCase {

    private static Formatter SIMPLE_FMT = new Formatter() {
			public String format(LogRecord record) {
				return record.getMessage() + SEPARATOR;
			}
    };

    private static final String FILENAME = "RollingFileManagerTest.log";
    private static final String FILENAME_BACKUP = "RollingFileManagerTest-BACKUP.log";

    public void setUp() throws Exception {
        super.setUp();
        new File(FILENAME).delete();
        new File(FILENAME_BACKUP).delete();    }

    public void tearDown() throws Exception {
        super.tearDown();
        Thread.sleep(10);
        new File(FILENAME).delete();
        new File(FILENAME_BACKUP).delete();
    }

    public void testConstructorNoFile() throws Exception{
    	try {
    		new RollingFileManager(null, true, "MONTH", new MyInterceptor(), new MyErrorManager());
    		fail();
    	} catch (IllegalArgumentException e) {
    		
    	}
    }

    public void testConstructorPolicy() throws Exception{
    	try {
    		new RollingFileManager(FILENAME, true, null, new MyInterceptor(), new MyErrorManager());
    		fail();
    	} catch (IllegalArgumentException e) {
    		
    	}
    }

    public void testConstructorNoInterceptor() throws Exception{
    	try {
    		new RollingFileManager(FILENAME, true, "MONTH", null, new MyErrorManager());
    		fail();
    	} catch (IllegalArgumentException e) {
    		
    	}
    }

    public void testConstructorNoManager() throws Exception{
    	try {
    		new RollingFileManager(FILENAME, true, "MONTH", new MyInterceptor(), null);
    		fail();
    	} catch (IllegalArgumentException e) {
    		
    	}
    }

    public void testRolloverMinute() throws Exception {
    	StreamInterceptor interceptor = new MyInterceptor();
        RollingFileManager rfm = new RollingFileManager(FILENAME, false, "MINUTE", interceptor, new MyErrorManager());
        try {
            setFileForRollover(rfm);
            rfm.rolloverIfRequired(1);
            
            assertTrue(new File(FILENAME_BACKUP).exists());
            assertEquals(0, new File(FILENAME_BACKUP).length());

        } finally {
        	interceptor.closeStream();
        }
    }


    private void setFileForRollover(RollingFileManager rfm) throws Exception{
        Field fName = RollingFileManager.class.getDeclaredField("destinationFilenameOnRollover");
        fName.setAccessible(true);
        fName.set(rfm, FILENAME_BACKUP);
    }
    
    private static class MyInterceptor implements StreamInterceptor {
    	OutputStream stream;
		@Override
		public void closeStream() {
			try {
				stream.close();
			} catch (IOException e) {
				fail();
			}
			stream = null;
		}

		@Override
		public void setStream(OutputStream os) {
			stream = os;
		}
    	
    }
    
    private static class MyErrorManager extends ErrorManager {
    	List<String> errors = new ArrayList<String>();
		public synchronized void error(String msg, Exception ex, int code) {
			errors.add(msg);
		}
    	
    }
}
