/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.marshalling.impl.databinding.json;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import com.betfair.cougar.test.CougarTestCase;
import com.betfair.cougar.util.dates.DateTimeUtility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class JSONUnMarshallerTest extends CougarTestCase {

    UnMarshaller unMarshaller = new JSONBindingFactory().getUnMarshaller();

    private static final ParameterType TEST_DATE_CLASS_PARAMETER_TYPE = new ParameterType(TestDateClass.class, null);
    private static final ParameterType TEST_CLASS_PARAMETER_TYPE = new ParameterType(TestClass.class, null);

    private static final String UTF8 = "UTF-8";




    public void testUnMarshal() {
        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());

        TestClass test = (TestClass)unMarshaller.unmarshall(new ByteArrayInputStream("{\"message\":\"foo\"}".getBytes()), TestClass.class, UTF8, false);
        assertEquals("foo", test.getMessage());
    }

    public void testUnMarshalExtraField() {
        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());

        TestClass test = (TestClass)unMarshaller.unmarshall(new ByteArrayInputStream("{\"message\":\"foo\", \"xtra\":\"bar\"}".getBytes()), TestClass.class, UTF8, false);
        assertEquals("foo", test.getMessage());
    }

    public void testUnMarshalParseException() {
        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());

        try {
            unMarshaller.unmarshall(new ByteArrayInputStream("{\"message\":\"foo\"".getBytes()), TestClass.class, UTF8, false);
            fail();
        } catch (CougarMarshallingException dve) {
            assertEquals(ResponseCode.BadRequest, dve.getResponseCode());
        }
    }

    public void testUnMarshallDateByParam() {

        testDateByJavaType("2011-03-07T11:02:01.095Z", "2011-03-07T11:02:01.095Z", TEST_DATE_CLASS_PARAMETER_TYPE);
        testDateByJavaType("2009-07-05T00:00:00.001Z", "2009-07-05T00:00:00.001Z", TEST_DATE_CLASS_PARAMETER_TYPE);
        testDateByJavaType("2009-07-05", "2009-07-05T00:00:00.000Z", TEST_DATE_CLASS_PARAMETER_TYPE);
        testDateByJavaType("2009-07-05T00:00:01", "2009-07-05T00:00:01.000Z", TEST_DATE_CLASS_PARAMETER_TYPE);
    }

    public void testUnMarshalDate() {

        TestDateClass test = (TestDateClass)unMarshaller.unmarshall(
                new ByteArrayInputStream("{\"date\":\"2011-03-07T11:02:01.095Z\"}".getBytes()),
                TestDateClass.class,
                UTF8, false);

        Date date = DateTimeUtility.parse("2011-03-07T11:02:01.095Z");
        assertEquals("Mon Mar 07 11:02:01 UTC 2011", test.getDate().toString());
        assertEquals(date, test.getDate());

        testDate("2011-03-07T11:02:01.095Z","2011-03-07T11:02:01.095Z");
        testDate("2009-07-05T00:00:00.001Z","2009-07-05T00:00:00.001Z");
        testDate("2009-07-05","2009-07-05T00:00:00.000Z");
        testDate("2009-07-05T00:00:01","2009-07-05T00:00:01.000Z");

    }

    private void testDate(String in,String out){
               TestDateClass test = (TestDateClass)unMarshaller.unmarshall(
                new ByteArrayInputStream(("{\"date\":\""+in+"\"}").getBytes()),
                TestDateClass.class,
                UTF8, false);

        Date date=DateTimeUtility.parse(out);

        assertEquals(date, test.getDate());
    }


    public void testListMarshallingByJavaType() {
        String[] strings = {
            "West Indies",
            "England",
            "Australia"
        };

        StringBuilder sb = new StringBuilder("[");
        for (int i=0; i<strings.length; i++) {
            if (i>0) {
                sb.append(",");
            }
            sb.append("\"");
            sb.append(strings[i]);
            sb.append("\"");
        }
        sb.append("]");

        ParameterType pt = ParameterType.create(List.class, new Class[] { String.class });

        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());
        List<String> actual = (List<String>)unMarshaller.unmarshall(new ByteArrayInputStream(sb.toString().getBytes()), pt, UTF8, false);

        assertEqualsArray(strings, actual.toArray());
    }

    public void testMapMarshallingByJavaType() {
        Map<String, String> map = new HashMap<>();

        //Cricket world cup was on - teams rated
        map.put("India", "Very good");
        map.put("Australia", "Fair");
        map.put("England", "Unlikely");
        map.put("South Africa", "Fair");

        StringBuilder sb = new StringBuilder("{");
        int i=0;
        for (String s : map.keySet()) {
            if (i++ > 0) {
                sb.append(",");
            }
            sb.append("\"");
            sb.append(s);
            sb.append("\":");
            sb.append("\"");
            sb.append(map.get(s));
            sb.append("\"");
        }
        sb.append("}");

        ParameterType pt = ParameterType.create(Map.class, new Class[] { String.class, String.class });

        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());
        Map<String,String> actual = (Map<String,String>)unMarshaller.unmarshall(new ByteArrayInputStream(sb.toString().getBytes()), pt, UTF8, false);

        assertEqualsArray(map.keySet().toArray(), actual.keySet().toArray());
        assertEqualsArray(map.values().toArray(), actual.values().toArray());
        assertEquals(map.keySet().size(), actual.keySet().size());
        assertEquals(map.get("India"), actual.get("India"));
    }

    private void testDateByJavaType(String in,String out, ParameterType pt){
        TestDateClass test = (TestDateClass)unMarshaller.unmarshall(
        new ByteArrayInputStream(("{\"date\":\""+in+"\"}").getBytes()),
        pt,
        UTF8, false);

        Date date=DateTimeUtility.parse(out);

        assertEquals(date, test.getDate());
    }

    public void testUnMarshalIOExceptionByType() {
        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());
        final IOException ex = new IOException("NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        try {
            unMarshaller.unmarshall(new InputStream() {

                @Override
                public int read() throws IOException {
                    throw ex;
                }
            }, TEST_CLASS_PARAMETER_TYPE, UTF8, false);
            fail();
        } catch (CougarMarshallingException dse) {
            assertEquals(ex, dse.getCause());
        }

    }

    public void testUnMarshalIOException() {
        JSONUnMarshaller unMarshaller = new JSONUnMarshaller(new ObjectMapper());
        final IOException ex = new IOException("NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        try {
            unMarshaller.unmarshall(new InputStream() {

                @Override
                public int read() throws IOException {
                    throw ex;
                }
            }, TestClass.class, UTF8, false);
            fail();
        } catch (CougarMarshallingException dse) {
            assertEquals(ex, dse.getCause());
        }
    }

    public void testUnMarshallFault() throws IOException {
        ObjectMapper mockedOM = Mockito.mock(ObjectMapper.class);
        FaultUnMarshaller faultUnMarshaller = new JSONUnMarshaller(mockedOM);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("value1", "yellow");
        params.put("value2", (byte)100);

        Map<String, Object> detailMap = new HashMap<String, Object>();
        detailMap.put("trace", new Exception().getStackTrace());
        detailMap.put("message", "The rain in spain stays mainly on the plane");
        detailMap.put("exceptionname", "SimpleException");
        detailMap.put("SimpleException", params);

        Map<String, Object> aMap = new HashMap<String, Object>();
        aMap.put("faultcode", "Client");

        final String expectedFaultString = "DSC-0019";

        aMap.put("faultstring", expectedFaultString);
        aMap.put("detail", detailMap);

        when(mockedOM.readValue(any(Reader.class), eq(HashMap.class))).thenReturn((HashMap)aMap);

        CougarFault fault = faultUnMarshaller.unMarshallFault(new ByteArrayInputStream(new byte[]{}), "utf-8");
        assertNotNull(fault);
        assertEquals(expectedFaultString, fault.getErrorCode());
        assertEquals("Client", fault.getFaultCode().name());

        List<String[]> faultMessages = fault.getDetail().getFaultMessages();
        assertEquals(params.size(), faultMessages.size());
        for (String[] pair : faultMessages) {
            assertTrue(pair.length == 2);
            assertTrue(params.containsKey(pair[0]));
            assertEquals(params.get(pair[0]).toString(), pair[1]);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class TestClass {
        private String message;
        private RuntimeException ex;

        public String getMessage() {
            return message;
        }

        public void setMessage(String msg) {
            if (ex != null) {
                throw ex;
            }
            message = msg;
        }
    }


    public static class TestDateClass {
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        private Date date;

    }
}
