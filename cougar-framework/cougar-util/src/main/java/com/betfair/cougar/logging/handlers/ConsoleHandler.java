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

/*
 * Borrowed from Sun's implementation as it logs to system.err not System.out
 * and cannot be changed. See bug:
 * 
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4827381
 */

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;


public class ConsoleHandler extends StreamHandler {
    public ConsoleHandler() {
    	setOutputStream(System.out);
    }

    public void publish(LogRecord record) {
    	super.publish(record);	
    	flush();
    }

    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not
     * to close the output stream.  That is, we do <b>not</b>
     * close <tt>System.out</tt>.
     */
    public void close() {
    	flush();
    }
}

