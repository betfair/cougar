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

// Originally from UpdatedComponentTests/ErrorCodes/Rest/Rest_Get_SimpleExceptions_NULL.xls;
package com.betfair.cougar.tests.updatedcomponenttests.errorcodes.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar can correctly throw a simple exception with error code: NULL.
 */
public class RestGetSimpleExceptionsNULLTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        cougarManager1.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Get the cougar log attribute for getting log entries later
        
        getNewHttpCallBean1.setOperationName("testExceptionQA");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Set the query parameter to tell the operation which exception to throw (NULL)
        Map map2 = new HashMap();
        map2.put("message","NULL");
        getNewHttpCallBean1.setQueryParams(map2);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the operation
        cougarManager1.makeRestCougarHTTPCalls(getNewHttpCallBean1);
        // Create the expected response as an XML document (the required exception)
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>SEX-0002</faultstring><detail><exceptionname>SimpleException</exceptionname><SimpleException><errorCode>NULL</errorCode><reason>NULL</reason></SimpleException></detail></fault>");
        // Create the expected response as a JSON object (the required exception)
        JSONHelpers jSONHelpers5 = new JSONHelpers();
        JSONObject parseJSONObjectFromJSONString11 = jSONHelpers5.parseJSONObjectFromJSONString("{faultcode:\"Client\",faultstring:\"SEX-0002\",detail:{exceptionname:\"SimpleException\", SimpleException:{errorCode:\"NULL\",reason:\"NULL\"}}}");
        // Convert the expected response to the various REST types for comparison
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertResponseToRestTypes12 = cougarManager1.convertResponseToRestTypes(createAsDocument10, getNewHttpCallBean1);
        // Check the 4 responses are as expected (correct error returned)
        HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes12.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 401, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Unauthorized", response6.getHttpStatusText());
        
        HttpResponseBean response7 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(parseJSONObjectFromJSONString11, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 401, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Unauthorized", response7.getHttpStatusText());
        
        HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(parseJSONObjectFromJSONString11, response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 401, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Unauthorized", response8.getHttpStatusText());
        
        HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes12.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 401, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Unauthorized", response9.getHttpStatusText());
        // Check the log entries are as expected
    }

}
