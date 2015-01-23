/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

// Originally from UpdatedComponentTests/StandardTesting/SOAP/SOAP_TestParameterStyles_ENUM_VALID_2.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.soap;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Test that Cougar can correctly handle a valid ENUM. In this case HeaderParam is Bar
 */
public class SOAPTestParameterStylesENUMVALID2Test {
    @Test
    public void doTest() throws Exception {
        // Convert the time to system TimeZone
        CougarHelpers cougarHelpers1 = new CougarHelpers();
        Date convertedDate1 = cougarHelpers1.convertToSystemTimeZone("2009-06-01T13:50:00.0Z");
        // Create the SOAP request as an XML Document
        XMLHelpers xMLHelpers2 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers2.getXMLObjectFromString("<TestParameterStylesQARequest><HeaderParam>Bar</HeaderParam><queryParam>qp1</queryParam><dateQueryParam>2009-06-01T13:50:00.0Z</dateQueryParam></TestParameterStylesQARequest>");
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager3 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager3.getNewHttpCallBean("87.248.113.14");
        CougarManager cougarManager2 = cougarManager3;

        cougarManager2.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

        getNewHttpCallBean2.setServiceName("Baseline");

        getNewHttpCallBean2.setVersion("v2");
        // Set the created SOAP request as the PostObject
        getNewHttpCallBean2.setPostObjectForRequestType(createAsDocument1, "SOAP");
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
        // Make the SOAP call to the operation
        cougarManager2.makeSoapCougarHTTPCalls(getNewHttpCallBean2);
        // Create the expected response object as an XML document (A message stating if the number of items in the list was correct)
        XMLHelpers xMLHelpers5 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers5.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<response><message>headerParam=Bar,queryParam=qp1,dateQueryParam="+cougarHelpers1.dateInUTC(convertedDate1)+"</message></response>").getBytes())));
        // Check the response is as expected
        HttpResponseBean response6 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(createAsDocument10, response6.getResponseObject());

        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected

        cougarManager2.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp8, new RequestLogRequirement("2.8", "testParameterStylesQA") );
    }

}
