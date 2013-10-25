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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_Floats_Infinity_JSON.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that for JSON requests Cougar can handle Floats in the post body, header params and query params that are infinity
 */
public class RestPostRequestTypesFloatsInfinityJSONTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        getNewHttpCallBean1.setOperationName("floatOperation");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Set each of the parameter types to contain a float datatype object that is infinity
        Map map2 = new HashMap();
        map2.put("HeaderParam","INF");
        getNewHttpCallBean1.setHeaderParams(map2);
        
        Map map3 = new HashMap();
        map3.put("queryParam","-INF");
        getNewHttpCallBean1.setQueryParams(map3);
        
        Map map4 = new HashMap();
        map4.put("RESTJSON","{\"message\":{\"bodyParameter\":\"-INF\"}}");
        getNewHttpCallBean1.setPostQueryObjects(map4);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
        // Make a JSON call to the operation requesting an XML response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make a JSON call to the operation requesting an JSON response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers6 = new XMLHelpers();
        Document createAsDocument12 = xMLHelpers6.getXMLObjectFromString("<FloatOperationResponse><FloatOperationResponseObject><bodyParameter>-INF</bodyParameter><headerParameter>INF</headerParameter><queryParameter>-INF</queryParameter></FloatOperationResponseObject></FloatOperationResponse>");
        // Create the expected response as a JSON object
        JSONHelpers jSONHelpers7 = new JSONHelpers();
        JSONObject createAsJSONObject13 = jSONHelpers7.createAsJSONObject(new JSONObject("{bodyParameter:\"-Infinity\",headerParameter:\"Infinity\",queryParameter:\"-Infinity\"}"));
        // Check the XML response is as expected
        HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(createAsDocument12, response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response8.getHttpStatusText());
        // Check the JSON response is as expected
        HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(createAsJSONObject13, response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response9.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp9, new RequestLogRequirement("2.8", "floatOperation"),new RequestLogRequirement("2.8", "floatOperation") );
    }

}
