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

// Originally from UpdatedComponentTests/IPBlacklist/REST/Rest_BlacklistedIP.xls;
package com.betfair.cougar.tests.updatedcomponenttests.ipblacklist;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


/**
 * Test that the Cougar service forbids the access for the X-Forwarded-For  in the blacklist (REST)
 */
public class RestBlacklistedIPTest {
    String blackListedIP = "192.168.0.1";

    @Test
    public void doTest_Single_IP() throws Exception {
        String testIPAddress = blackListedIP;

        doTest_ExpectedResponse(testIPAddress, false);
    }

    @Test
    public void doTest_FirstItem_IPList() throws Exception {
        String testIPAddress = blackListedIP + ";1.2.3.4;5.6.7.8";

        doTest_ExpectedResponse(testIPAddress, false);
    }

    @Test
    public void doTest_MiddleItem_IPList() throws Exception {
        String testIPAddress = "1.2.3.4;" + blackListedIP + ";5.6.7.8";

        doTest_ExpectedResponse(testIPAddress, true);
    }

    @Test
    public void doTest_LastItem_IPList() throws Exception {
        String testIPAddress = "1.2.3.4;5.6.7.8;9.0.1.2;" + blackListedIP;

        doTest_ExpectedResponse(testIPAddress, true);
    }


    private void doTest_ExpectedResponse(String testIPAddress, boolean ok) throws Exception
    {
        // Get an HTTPCallBean
        CougarManager cougarManager = CougarManager.getInstance();
        HttpCallBean HTTPCallBean = cougarManager.getNewHttpCallBean(testIPAddress.replace(";",","));
        // Set Cougar Fault Controller attributes
        cougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set operation  name
        HTTPCallBean.setOperationName("stringSimpleTypeEcho", "stringEcho");
        // Set service name to call
        HTTPCallBean.setServiceName("baseline", "cougarBaseline");
        // Set service version to call
        HTTPCallBean.setVersion("v2");
        // Set Query parameter
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("msg","foo");
        HTTPCallBean.setQueryParams(map2);
        // Get current time

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        // Make Rest calls (makes 4 calls with different content/accept combinations of XML and JSON)
        cougarManager.makeRestCougarHTTPCalls(HTTPCallBean);
        // Create a REST response structure as a Document object
        XMLHelpers xMLHelpers4 = new XMLHelpers();

        int expectedHttpResponseCode = ok ? 200 : 403;
        String expectedHttpResponseText = ok ? "OK" : "Forbidden";
        String expectedResponseText = ok ? "Ok" : "Forbidden";

        // Get the 4 results from the Rest calls and compare to the expected XML and JSON responses
        HttpResponseBean response5 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        HttpResponseBean response6 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        HttpResponseBean response7 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        HttpResponseBean response8 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        if (!ok) {
            Document responseDocument = xMLHelpers4.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0015</faultstring><detail/></fault>");
            // Convert the response document into Rest (XML and JSON) representations
            Map<CougarMessageProtocolRequestTypeEnum, Object> convertedResponses = cougarManager.convertResponseToRestTypes(responseDocument, HTTPCallBean);
            AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response5.getResponseObject());
            AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response6.getResponseObject());
            AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response7.getResponseObject());
            AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response8.getResponseObject());
        }

        AssertionUtils.multiAssertEquals(expectedHttpResponseCode, response5.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(expectedHttpResponseText, response5.getHttpStatusText());

        AssertionUtils.multiAssertEquals(expectedHttpResponseCode, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(expectedHttpResponseText, response6.getHttpStatusText());

        AssertionUtils.multiAssertEquals(expectedHttpResponseCode, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(expectedHttpResponseText, response7.getHttpStatusText());

        AssertionUtils.multiAssertEquals(expectedHttpResponseCode, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(expectedHttpResponseText, response8.getHttpStatusText());

        cougarManager.verifyAccessLogEntriesAfterDate(timeStamp,
                new AccessLogRequirement(testIPAddress, "/cougarBaseline/v2/stringEcho", expectedResponseText),
                new AccessLogRequirement(testIPAddress, "/cougarBaseline/v2/stringEcho", expectedResponseText),
                new AccessLogRequirement(testIPAddress, "/cougarBaseline/v2/stringEcho", expectedResponseText),
                new AccessLogRequirement(testIPAddress, "/cougarBaseline/v2/stringEcho", expectedResponseText));
    }
}
