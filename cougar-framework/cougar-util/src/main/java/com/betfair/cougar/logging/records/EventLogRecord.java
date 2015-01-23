/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.logging.records;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

import com.betfair.cougar.api.LoggableEvent;

public final class EventLogRecord extends CougarLogRecord {

	private static final String fieldSeperator = ",";
	private static final String collectionStart = "[";
    private static String collectionSeperator = "|";
	private static final String collectionEnd = "]";
	private static final String TAB = "\t";

	private static final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void setCollectionSeperator(String collectionSeperator) {
        EventLogRecord.collectionSeperator = collectionSeperator;
    }

    private static String[] NON_LOGGABLE_STRINGS = new String[]{
    	LINE_SEPARATOR, TAB, fieldSeperator, collectionSeperator
	};

    private String messageString;
    private final LoggableEvent event;
    private final Object[] xFields;

    public EventLogRecord(LoggableEvent event, Object[] extensionFields) {
        super(event.getLogName(), Level.INFO, null);
        this.event = event;
        this.xFields = extensionFields == null ? new Object[]{} : extensionFields;
    }

    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.records.LoggableEvent#renderMessageString()
      */
    public final void renderMessageString() {
        Object[] coreFields = event.getFieldsToLog();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coreFields.length + xFields.length; ++i) {
            if (i > 0) {
                sb.append(fieldSeperator);
            }
            Object field = i < coreFields.length ? coreFields[i] : xFields[i - coreFields.length];
            renderObject(sb, field);

        }
        messageString = sb.toString();

    }

    // Do not allow the getMessage method to be overloaded so that the message
    // is correctly written from the result of the setEventRecord call. The pre
    // render is available so that the message can be rendered before the logging
    // synchronised blocking write is entered.
    public final String getMessage() {
        if (messageString == null) {
            renderMessageString();
        }
        return messageString;
    }

    // Return a byte array for binary logging. Dummy implementation
    /* (non-Javadoc)
      * @see com.betfair.cougar.logging.records.LoggableEvent#getBytes()
      */
    public final byte[] getBytes() throws IOException {
        return getMessage().getBytes();
    }

    private DateFormat getDateFormatter() {
        if (dateFormatter.get() == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormatter.set(dateFormat);
        }
        return dateFormatter.get();
    }

    private void renderObject(StringBuilder sb, Object o) {
        if (o == null) {
            return;
        }

        if (o instanceof Date) {
            sb.append(getDateFormatter().format((Date)o));
            return;
        }

        if (o instanceof Loggable) {
            ((Loggable)o).writeTo(sb);
            return;
        }

        if (o instanceof Object[]) {
            o = Arrays.asList((Object[])o);
        }
        if (o instanceof Iterable<?>) {
            sb.append(collectionStart);
            boolean first = true;
            for (Object obj : (Iterable<?>)o) {
                if (first) {
                    first = false;
                } else {
                    sb.append(collectionSeperator);
                }
                renderObject(sb, obj);
            }
            sb.append(collectionEnd);
            return;
        }

        if (o instanceof Boolean) {
            sb.append(((Boolean)o) ? "Y" : "N");
            return;
        }

        sb.append(cleanse(o));
    }

    private String cleanse(Object obj) {
    	if(obj == null)
    		return "";

    	String stringToLog = obj.toString();
    	for (String string : NON_LOGGABLE_STRINGS) {
    		stringToLog = stringToLog.replace(string, " ");
		}
    	return stringToLog;
    }
}
