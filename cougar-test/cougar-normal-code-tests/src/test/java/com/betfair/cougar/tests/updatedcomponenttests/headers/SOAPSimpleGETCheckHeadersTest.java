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

// Originally from UpdatedComponentTests/Headers/SOAP_SimpleGET_Check_Headers.xls;
package com.betfair.cougar.tests.updatedcomponenttests.headers;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Test Basic functionality of the Container, test simple requests and responses work for SOAP Protocol.
 */
public class SOAPSimpleGETCheckHeadersTest {
    @Test
    public void doTest() throws Exception {
        // Create the request as an XML document
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<TestSimpleGetRequest><message>foo</message></TestSimpleGetRequest>");
        // Set up the Http Call Bean to make the request, not setting any headers
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean("87.248.113.14");
        cougarManager2 = cougarManager2;
        // Get the cougar log attribute for getting log entries later

        cougarManager2.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

        getNewHttpCallBean2.setServiceName("Baseline");

        getNewHttpCallBean2.setVersion("v2");

        getNewHttpCallBean2.setPostObjectForRequestType(createAsDocument1, "SOAP");
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
        // Make the SOAP call to the operation
        cougarManager2.makeSoapCougarHTTPCalls(getNewHttpCallBean2);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<response><message>foo</message></response>");
        // Check the response is as expected
        HttpResponseBean getResponseObjectsByEnum12 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(createAsDocument10, getResponseObjectsByEnum12.getResponseObject());
        // Check the response headers are as expected (set to default values as no headers were set in the request)
        Map<String, String> map6 = getResponseObjectsByEnum12.getFlattenedResponseHeaders();
        AssertionUtils.multiAssertEquals("no-cache", map6.get("Cache-Control"));
        AssertionUtils.multiAssertEquals("text/xml; charset=utf-8", map6.get("Content-Type"));
        // Check the log entries are as expected

        cougarManager2.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp8, new RequestLogRequirement("2.8", "testSimpleGet") );

        cougarManager2.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp8, new AccessLogRequirement(null, null, "Ok") );
    }

}
