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

package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.core.api.fault.Fault;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.test.CougarTestCase;
import com.betfair.cougar.util.dates.DateTimeUtility;
import com.betfair.cougar.util.dates.XMLDateAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class XMLMarshallerTest extends CougarTestCase {

    public void testXMLMarshaller() throws Exception {
        Marshaller marshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TestClass tc = new TestClass();
        tc.message = "foo";
        marshaller.marshall(bos, tc, "utf-8", false);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><Test><message>foo</message></Test>", bos.toString());

        bos = new ByteArrayOutputStream();
        tc.setMessage("bar");
        marshaller.marshall(bos, tc, "utf-8", false);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><Test><message>bar</message></Test>", bos.toString());
    }

    public void testXMLMarshalDate() throws Exception {
        Marshaller marshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Date d = DateTimeUtility.parse("11-03-03");
        TestDateClass tc = new TestDateClass();
        tc.setDate(d);


        marshaller.marshall(bos, tc, "utf-8", false);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><TestDate><date>0011-03-03T00:00:00.000Z</date></TestDate>", bos.toString());


    }

    public void testXMLMarshalDateList() throws Exception {
        Marshaller marshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Date d = DateTimeUtility.parse("11-03-03");
        Date d2 = DateTimeUtility.parse("12-03-03");
        TestDateList tc = new TestDateList();
        tc.getDates().add(d);
        tc.getDates().add(d2);


        marshaller.marshall(bos, tc, "utf-8", false);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><TestDateList><dates><Date>0011-03-03T00:00:00.000Z</Date><Date>0012-03-03T00:00:00.000Z</Date></dates></TestDateList>", bos.toString());


    }

    public void testXMLMarshalDateSet() throws Exception {
        Marshaller marshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Date d = DateTimeUtility.parse("11-03-03");
        Date d2 = DateTimeUtility.parse("12-03-03");
        TestDateSet tc = new TestDateSet();
        tc.getDates().add(d);
        tc.getDates().add(d2);


        marshaller.marshall(bos, tc, "utf-8", false);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><TestDateSet><dates><Date>0012-03-03T00:00:00.000Z</Date><Date>0011-03-03T00:00:00.000Z</Date></dates></TestDateSet>", bos.toString());


    }

    public void testXMLMarshalFaultNoDetail() throws Exception {
        FaultController.getInstance().setDetailedFaults(false);
        try {
            FaultMarshaller marshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getFaultMarshaller();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Fault tc = new Fault(FaultCode.Server, "EX01", "detail", new TestFaultException(ResponseCode.Unauthorised, "MyMessage"));
            marshaller.marshallFault(bos, tc, "utf-8");
            String result = bos.toString();

            assertEquals("<?xml version='1.0' encoding='utf-8'?><fault><faultcode>Server</faultcode><faultstring>EX01</faultstring><detail><exceptionname>TestFaultException</exceptionname><TestFaultException><foo>foo</foo><bar>1234</bar></TestFaultException></detail></fault>", result);
        } finally {
            FaultController.getInstance().setDetailedFaults(true);
        }
    }

    public void testXMLMarshalFaultWithDetail() throws Exception {
        FaultController.getInstance().setDetailedFaults(true);
        FaultMarshaller marshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getFaultMarshaller();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fault tc = new Fault(FaultCode.Server, "EX01", "MyMessage", new TestFaultException(ResponseCode.Unauthorised, null));
        marshaller.marshallFault(bos, tc, "utf-8");
        String result = bos.toString();

        int stackTraceStart = result.indexOf("<trace>");
        int stackTraceEnd = result.indexOf("</trace>");
        assertTrue(stackTraceStart > 0);
        assertTrue(stackTraceEnd > 0);
        result = result.substring(0, stackTraceStart + 7) + "TRACE HERE" + result.substring(stackTraceEnd);
        assertEquals("<?xml version='1.0' encoding='utf-8'?><fault><faultcode>Server</faultcode><faultstring>EX01</faultstring><detail><exceptionname>TestFaultException</exceptionname><TestFaultException><foo>foo</foo><bar>1234</bar></TestFaultException><trace>TRACE HERE</trace><message>MyMessage</message></detail></fault>", result);
    }

    @XmlRootElement(name = "Test")
    public static class TestClass {
        private String message = "";

        @XmlElement
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


    @XmlRootElement(name = "TestDate")
    public static class TestDateClass {
        private Date date = new Date();

        @XmlElement
        @XmlJavaTypeAdapter(value = XMLDateAdapter.class)
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }


    }


    @XmlRootElement(name = "TestDateList")
    public static class TestDateList {
        private List<Date> dates = new ArrayList<Date>();

        @XmlElementWrapper(name="dates",nillable=true)
        @XmlElement(name="Date",nillable=true)
        @XmlJavaTypeAdapter(value = XMLDateAdapter.class)
        public List<Date> getDates() {
            return dates;
        }

        public void setDates(List<Date> date) {
            this.dates = date;
        }


    }


    @XmlRootElement(name = "TestDateSet")
    public static class TestDateSet {
        private Set<Date> dates = new HashSet<Date>();

        @XmlElementWrapper(name="dates",nillable=true)
        @XmlElement(name="Date",nillable=true)
        @XmlJavaTypeAdapter(value = XMLDateAdapter.class)
        public Set<Date> getDates() {
            return dates;
        }

        public void setDates(Set<Date> date) {
            this.dates = date;
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
            // TODO Auto-generated method stub
            return null;
        }
    }
}
