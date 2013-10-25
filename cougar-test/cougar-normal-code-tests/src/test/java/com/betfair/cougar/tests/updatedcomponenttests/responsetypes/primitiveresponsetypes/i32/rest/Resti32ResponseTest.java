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

// Originally from UpdatedComponentTests/ResponseTypes/PrimitiveResponseTypes/i32/REST/Rest_i32Response.xls;
package com.betfair.cougar.tests.updatedcomponenttests.responsetypes.primitiveresponsetypes.i32.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Test that the Cougar service allows to use i32 primitive type in the response (REST)
 */
public class Resti32ResponseTest {
    @Test
    public void doTest() throws Exception {
        // Get an HTTPCallBean
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean HTTPCallBean = cougarManager1.getNewHttpCallBean("87.248.113.14");
        CougarManager CougarManager = cougarManager1;
        // Get LogManager JMX Attribute
        // Set Cougar Fault Controller attributes
        CougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set operation  name 
        HTTPCallBean.setOperationName("I32SimpleTypeEcho", "i32Echo");
        // Set service name to call
        HTTPCallBean.setServiceName("baseline", "cougarBaseline");
        // Set service version to call
        HTTPCallBean.setVersion("v2");
        // Set Query parameter (?msg=true)
        Map map2 = new HashMap();
        map2.put("msg","-2147483647");
        HTTPCallBean.setQueryParams(map2);
        // Get current time

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        // Make Rest calls (makes 4 calls with different content/accept combinations of XML and JSON)
        CougarManager.makeRestCougarHTTPCalls(HTTPCallBean);
        // Create a REST response structure as a Document object
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document responseDocument = xMLHelpers4.getXMLObjectFromString("<Integer>-2147483647</Integer>");
        // Convert the response document into Rest (XML and JSON) representations
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertedResponses = CougarManager.convertResponseToRestTypes(responseDocument, HTTPCallBean);
        // Get the 4 results from the Rest calls and compare to the expected XML and JSON responses
        HttpResponseBean response5 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response5.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response5.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response5.getHttpStatusText());
        
        HttpResponseBean response6 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());
        
        HttpResponseBean response7 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response7.getHttpStatusText());
        
        HttpResponseBean response8 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(convertedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response8.getHttpStatusText());
        // Check Service log entries after the time recorded earlier in the test
        // Check Request log entries after the time recorded earlier in the test
        CougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "i32SimpleTypeEcho"),new RequestLogRequirement("2.8", "i32SimpleTypeEcho"),new RequestLogRequirement("2.8", "i32SimpleTypeEcho"),new RequestLogRequirement("2.8", "i32SimpleTypeEcho") );
    }

}
