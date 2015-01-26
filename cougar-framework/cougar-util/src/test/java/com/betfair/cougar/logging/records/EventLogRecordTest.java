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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import com.betfair.cougar.api.LoggableEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventLogRecordTest {

    private String LS = System.getProperty("line.separator");
    MyLoggableEvent mle;
    EventLogRecord elr;


    @Before
    public void setup() {
        mle = new MyLoggableEvent();
        elr = new EventLogRecord(mle, null);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testString() {
        mle.setFieldsToLog(new Object[]{"A string"});
        assertEquals("", "A string", elr.getMessage());
    }

    @Test
    public void testBooleanTrue() {
        mle.setFieldsToLog(new Object[]{false});
        assertEquals("", "N", elr.getMessage().toString());
    }

    @Test
    public void testBooleanFalse() {
        mle.setFieldsToLog(new Object[]{false});
        assertEquals("", "N", elr.getMessage().toString());
    }

    /*
    @Test
    public void testRecursionLoop() {

        Object[] things = new Object[1];
        things[0] = things;

        mle.setFieldsToLog(things);
        assertEquals("", elr.getMessage().toString(), "N" + LS);
    }
    */

    @Test
    public void testDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Object[] things = new Object[1];
        things[0] = sdf.parse("01/12/1970");

        mle.setFieldsToLog(things);
        assertEquals("", "1970-12-01 00:00:00.000", elr.getMessage().toString());
    }

    @Test
    public void testLoggable() throws Exception {
        MyLoggable ml = new MyLoggable();
        mle.setFieldsToLog(new Object[]{ml});
        assertEquals("", ml.getString(), elr.getMessage().toString());
    }

    @Test
    public void testArray() throws Exception {
        ArrayList data = new ArrayList();
        data.add("one");
        data.add(2);

        ArrayList data2 = new ArrayList();
        data2.add("three");
        data2.add(4);
        data.add(data2);

        mle.setFieldsToLog(new Object[]{data});
        assertEquals("", "[one|2|[three|4]]", elr.getMessage().toString());
    }

    @Test
    public void testArbitraryObject() throws Exception {
        mle.setFieldsToLog(new Object[]{new SimpleDateFormat("")});
        assertEquals("", "java.text.SimpleDateFormat@0", elr.getMessage().toString());
    }

    @Test
    public void testStringWithLineEndings() throws Exception {
    	mle.setFieldsToLog(new Object[]{"A string"+LS});
    	assertEquals("Line Endings should be removed", "A string ", elr.getMessage());
    }

    @Test
    public void testStringWithTabs() throws Exception {
    	mle.setFieldsToLog(new Object[]{"A string\t"});
    	assertEquals("Tabs should be removed", "A string ", elr.getMessage());
    }

    @Test
    public void testStringWithCommas() throws Exception {
    	mle.setFieldsToLog(new Object[]{"A string,"});
    	assertEquals("Commas should be removed", "A string ", elr.getMessage());
    }
}

class MyLoggableEvent implements LoggableEvent {

    public void setFieldsToLog(Object[] fieldsToLog) {
        this.fieldsToLog = fieldsToLog;
    }

    private Object[] fieldsToLog = null;

    @Override
    public Object[] getFieldsToLog() {
        return fieldsToLog;
    }

    @Override
    public String getLogName() {
        return "MyLoggableEvent";
    }
}

class MyLoggable implements Loggable {

    public String getString() {
        return s;
    }

    private String s = "Well done!";

    @Override
    public void writeTo(StringBuilder record) {
        record.append(s);
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        stream.write(s.getBytes());
    }
}
