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

package com.betfair.cougar;

import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.logging.LogDefinition;
import com.betfair.cougar.logging.MockCapturingLogger;
import junit.framework.TestCase;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogRecord;


public abstract class CougarUtilTestCase extends TestCase {
	public  static final String SEPARATOR = System.getProperty("line.separator");

    private Class classUnderTest;

    protected CougarUtilTestCase(Class classUnderTest) {
        super();
        this.classUnderTest = classUnderTest;
    }

	protected CougarUtilTestCase() {
		super();
	}

	public CougarUtilTestCase(String name) {
		super(name);
	}

    @Before
    public void setUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		((MockCapturingLogger)CougarLoggingUtils.getLogger("")).getLogRecords().clear();
		LogDefinition hnd = new LogDefinition();
        hnd.setLogName("");
        hnd.register();
    }
	
	public List<LogRecord> getMessageLog() {
        if (classUnderTest != null) {
            return ((MockCapturingLogger)CougarLoggingUtils.getLogger(classUnderTest)).getLogRecords();
        } else {
		    return ((MockCapturingLogger)CougarLoggingUtils.getLogger("")).getLogRecords();
        }
	}
	public void validateFileContents(String filename, String expected, boolean containsAnywhere) throws Exception {

        assertTrue(new File(filename).exists());
		String lastGoodLine = null;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			String thisLine;
			while ((thisLine = br.readLine()) != null) {
				lastGoodLine = thisLine;
				// If containsAnywhere is set, this string may be anywhere in the file
				// if not, it must be the last line.
				if (containsAnywhere) {
					if (lastGoodLine.contains(expected)) {
						return;
					}
				}
			}
		}
		finally {
			if (br != null) br.close();
		}
		assertEquals(expected, lastGoodLine);
	}
}
