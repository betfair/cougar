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

// Originally from UpdatedComponentTests/Geolocate/Rest/Rest_Geolocate_ByIPAddress_Italy.xls;
package com.betfair.cougar.tests.updatedcomponenttests.geolocate.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that when a Rest JSON request is received by Cougar, the ipaddress is used to correctly identify where the request originated, in this case: Italy
 */
public class RestGeolocateByIPAddressItalyTest {
    String italianIPAddress = "212.162.85.36";

    @Test
    public void doTest_Single_IP() throws Exception {
        doTest(italianIPAddress, italianIPAddress);
    }

    @Test
    public void doTest_IP_List() throws Exception {
        String testIPAddress = italianIPAddress + ",1.2.3.4,5.6.7.8";
        String expectedIPAddress = italianIPAddress + ";1.2.3.4;5.6.7.8";
        doTest(testIPAddress, expectedIPAddress);
    }

    private void doTest(String testIPAddress, String expectedIPAddress) throws Exception{
        String expectedIPLocation = "";

        if(expectedIPAddress.contains(italianIPAddress))
        {
            expectedIPLocation = "IT";
        }

        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean(testIPAddress);

        getNewHttpCallBean1.setOperationName("testSimpleGet", "simple");
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        
        Map map2 = new HashMap();
        map2.put("message","foo");
        getNewHttpCallBean1.setQueryParams(map2);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the operation
        cougarManager1.makeRestCougarHTTPCalls(getNewHttpCallBean1);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<SimpleResponse><message>foo</message></SimpleResponse>");
        // Convert the expected response to REST types for comparison with actual responses
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertResponseToRestTypes11 = cougarManager1.convertResponseToRestTypes(createAsDocument10, getNewHttpCallBean1);
        // Check the 4 responses are as expected
        HttpResponseBean response5 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes11.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response5.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response5.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response5.getHttpStatusText());
        
        HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes11.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());
        
        HttpResponseBean response7 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes11.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response7.getHttpStatusText());
        
        HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes11.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response8.getHttpStatusText());
        
        // Check the log entries are as expected
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp8,
                new RequestLogRequirement("2.8", "testSimpleGet"),
                new RequestLogRequirement("2.8", "testSimpleGet"),
                new RequestLogRequirement("2.8", "testSimpleGet"),
                new RequestLogRequirement("2.8", "testSimpleGet"));
        // In particular check the IPLocation field of the Access log gives the correct location, in this case IT for Italy
        cougarManager1.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp8
                ,new AccessLogRequirement(expectedIPAddress, expectedIPLocation, "/cougarBaseline/v2/simple", "Ok")
                ,new AccessLogRequirement(expectedIPAddress, expectedIPLocation, "/cougarBaseline/v2/simple", "Ok")
                ,new AccessLogRequirement(expectedIPAddress, expectedIPLocation, "/cougarBaseline/v2/simple", "Ok")
                ,new AccessLogRequirement(expectedIPAddress, expectedIPLocation, "/cougarBaseline/v2/simple", "Ok")
        );
    }
}
