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
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.fault.Fault;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.test.CougarTestCase;
import com.betfair.cougar.util.dates.DateTimeUtility;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class JSONMarshallerTest extends CougarTestCase {

    public void testMarshal() {
        Marshaller jsonMarshaller = new JSONBindingFactory().getMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TestClass tc = new TestClass();
        tc.message = "foo";
        jsonMarshaller.marshall(bos, tc, "utf-8", false);

        assertEquals("{\"message\":\"foo\"}", bos.toString());
    }

    Marshaller jsonMarshaller = new JSONBindingFactory().getMarshaller();

    public void testMarshalDate() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Date d = new Date();
        jsonMarshaller.marshall(bos, d, "utf-8", true);
        String out=bos.toString();
        out=out.substring(1,out.length()-1);
        assertEquals(d, DateTimeUtility.parse(out));


        testDate("2009-07-07","2009-07-07T00:00:00.000Z");
        testDate("2009-07-05T00:00:00.001Z","2009-07-05T00:00:00.001Z");
        testDate("2009-07-05T00:00:00.001","2009-07-05T00:00:00.001Z");
        testDate("2009-07-05T00:00:01","2009-07-05T00:00:01.000Z");
    }

    private void testDate(String in,String out){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Date date=DateTimeUtility.parse(in);
        jsonMarshaller.marshall(bos, date, "utf-8", false);
        String result=bos.toString();
        assertEquals(out, result.substring(1,result.length()-1));
    }

    public void testMarshalFaultNoDetail() throws Exception {
        FaultController.getInstance().setDetailedFaults(false);
        try {
            JSONMarshaller jsonMarshaller = new JSONMarshaller(new ObjectMapper());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Fault tc = new Fault(FaultCode.Server, "EX01", "detail", new TestFaultException(ResponseCode.Unauthorised, "MyMessage"));
            jsonMarshaller.marshallFault(bos, tc, "utf-8");
            String result = bos.toString();

            ObjectMapper m = new ObjectMapper();
            JsonNode rootNode = m.readValue(new ByteArrayInputStream(bos.toByteArray()), JsonNode.class);

            assertEquals("Server", rootNode.get("faultcode").asText());
            assertEquals("EX01", rootNode.get("faultstring").asText());

            JsonNode detail = rootNode.get("detail");
            assertNotNull(detail);
            assertEquals(2, detail.size());
            JsonNode exceptionNode = detail.get("TestFaultException");
            assertNotNull(exceptionNode);
            assertEquals(2, exceptionNode.size());
            assertEquals("foo", exceptionNode.get("foo").asText());
            assertEquals("1234", exceptionNode.get("bar").asText());
        } finally {
            FaultController.getInstance().setDetailedFaults(true);
        }
    }

    public void testMarshallWithNulls() throws Exception {
        JSONMarshaller jsonMarshaller = new JSONMarshaller(new ObjectMapper());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        class Result {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        jsonMarshaller.marshall(bos, new Result(), "UTF-8", false);


        byte[] streamed = bos.toByteArray();
        assertArrayEquals(streamed, "{\"name\":null}".getBytes());

    }

    public void testMarshalFaultWithDetail() throws Exception {
        FaultController.getInstance().setDetailedFaults(true);
        JSONMarshaller jsonMarshaller = new JSONMarshaller(new ObjectMapper());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fault tc = new Fault(FaultCode.Server, "EX01", "MyMessage", new TestFaultException(ResponseCode.Unauthorised, "MyMessage"));
        jsonMarshaller.marshallFault(bos, tc, "utf-8");

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(new ByteArrayInputStream(bos.toByteArray()), JsonNode.class);

        assertEquals("Server", rootNode.get("faultcode").asText());
        assertEquals("EX01", rootNode.get("faultstring").asText());

        JsonNode detail = rootNode.get("detail");
        assertNotNull(detail);
        assertEquals(4, detail.size());
        JsonNode exceptionNode = detail.get("TestFaultException");
        assertNotNull(exceptionNode);
        assertEquals(2, exceptionNode.size());
        assertEquals("foo", exceptionNode.get("foo").asText());
        assertEquals("1234", exceptionNode.get("bar").asText());

        String trace = detail.get("trace").asText();
        assertTrue(trace.length() > 10);
        assertTrue(trace.contains("TestFaultException"));

        assertEquals("MyMessage", detail.get("message").asText());
    }

    public void testMarshalException() {
        JSONMarshaller jsonMarshaller = new JSONMarshaller(new ObjectMapper());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TestClass tc = new TestClass();
        tc.ex = new RuntimeException("ex");
        try {
            jsonMarshaller.marshall(bos, tc, "utf-8", false);
            fail();
        } catch (CougarMarshallingException dfe) {
            assertTrue(dfe.getCause() instanceof JsonMappingException);
            assertTrue(dfe.getCause().getCause() instanceof RuntimeException);
            assertEquals(tc.ex, dfe.getCause().getCause());
        }
    }

    public void testMarshalIOException() {
        JSONMarshaller jsonMarshaller = new JSONMarshaller(new ObjectMapper());
        final IOException ex = new IOException("NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        OutputStream bos = new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                throw ex;
            }
        };

        TestClass tc = new TestClass();
        tc.ex = new RuntimeException("ex");
        try {
            jsonMarshaller.marshall(bos, tc, "utf-8", false);
            fail();
        } catch (CougarMarshallingException dfe) {
            assertEquals(JsonMappingException.class, dfe.getCause().getClass());
        }
    }

    public static class TestClass {
        private String message;
        private RuntimeException ex;

        public String getMessage() {
            if (ex != null) {
                throw ex;
            }
            return message;
        }
    }

    public static class TestFaultException extends CougarApplicationException {

        public TestFaultException(ResponseCode code, String message) {
            super(code, message);
        }

        @Override
        public List<String[]> getApplicationFaultMessages() {
            List<String[]> faultMessages = new ArrayList<String[]>();
            faultMessages.add(new String[]{"foo", "foo"});
            faultMessages.add(new String[]{"bar", "1234"});
            return faultMessages;
        }

        @Override
        public String getApplicationFaultNamespace() {
            return null;
        }
    }
}
