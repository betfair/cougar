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

// Originally from UpdatedComponentTests/Logging/Logging_InvalidIdentityChain_IPAdressLogged.xls;
package com.betfair.cougar.tests.updatedcomponenttests.logging;

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
 * Ensure that when a cougar request is made, using an invalid identity chain, the IP address is still recorded in the access log
 */
public class LoggingInvalidIdentityChainIPAdressLoggedTest {
    String validIPaddress = "87.248.113.14";

    @Test
    public void doTest_Single_IP() throws Exception {
        doTest(validIPaddress, validIPaddress);
    }

    @Test
    public void doTest_IPList() throws Exception {
        String testIPAddress = validIPaddress + ",192.168.0.1,2.3.4.5";
        String expectedIPAddress = testIPAddress.replace(",",";");

        doTest(testIPAddress, expectedIPAddress);
    }

    @Test
    public void doTest_IPList_WithInvalidIP() throws Exception {
        String testIPAddress = validIPaddress + ",192.168.0.1,INVALID IP ADDRESS,2.3.4.5";
        String expectedIPAddress = validIPaddress +";192.168.0.1;2.3.4.5";

        doTest(testIPAddress, expectedIPAddress);
    }

    public void doTest(String testIPAddress, String expectedIPAddress) throws Exception {
        CougarManager cougarManager = CougarManager.getInstance();
        // Turn off detailed faults
        cougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

        // Set up the Http Call Bean to make the request
        HttpCallBean callBean = cougarManager.getNewHttpCallBean(testIPAddress);
        callBean.setOperationName("testSimpleGet", "simple");
        callBean.setServiceName("baseline", "cougarBaseline");
        callBean.setVersion("v2");
        Map<String,String> map2 = new HashMap<String,String>();
        map2.put("message","foo");
        callBean.setQueryParams(map2);

        // Set the token-username header to be INVALID to force an exception to be thrown when resolving the identity chain
        Map<String,String> map3 = new HashMap<String,String>();
        map3.put("X-Token-Username","INVALID");
        callBean.setHeaderParams(map3);

        // Get current time for getting log entries later

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

        // Make the 4 REST calls to the operation
        cougarManager.makeRestCougarHTTPCalls(callBean);

        // Create the expected response as an XML document (Fault)
        XMLHelpers xMLHelpers5 = new XMLHelpers();
        Document xmlDocument = xMLHelpers5.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0015</faultstring><detail/></fault>");

        // Convert the expected response to REST types for comparison with actual responses
        Map<CougarMessageProtocolRequestTypeEnum, Object> restResponses = cougarManager.convertResponseToRestTypes(xmlDocument, callBean);

        // Check the 4 responses are as expected (access was forbidden as the identity chain was invalid)
        HttpResponseBean response6 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(restResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response6.getResponseObject());
        AssertionUtils.multiAssertEquals(403, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Forbidden", response6.getHttpStatusText());
        
        HttpResponseBean response7 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(restResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response7.getResponseObject());
        AssertionUtils.multiAssertEquals(403, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Forbidden", response7.getHttpStatusText());
        
        HttpResponseBean response8 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(restResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response8.getResponseObject());
        AssertionUtils.multiAssertEquals(403, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Forbidden", response8.getHttpStatusText());
        
        HttpResponseBean response9 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(restResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response9.getResponseObject());
        AssertionUtils.multiAssertEquals(403, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Forbidden", response9.getHttpStatusText());
        
        // Check that the IP address has still be logged in the access log
        cougarManager.verifyAccessLogEntriesAfterDate(timeStamp
                , new AccessLogRequirement(expectedIPAddress, null, null)
                , new AccessLogRequirement(expectedIPAddress, null, null)
                , new AccessLogRequirement(expectedIPAddress, null, null)
                , new AccessLogRequirement(expectedIPAddress, null, null)
        );
        
    }

}
