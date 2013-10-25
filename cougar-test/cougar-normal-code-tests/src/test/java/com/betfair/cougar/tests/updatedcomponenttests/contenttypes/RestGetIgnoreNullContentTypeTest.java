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

// Originally from UpdatedComponentTests/ContentTypes/Rest_Get_IgnoreNullContentType.xls;
package com.betfair.cougar.tests.updatedcomponenttests.contenttypes;

import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.json.JSONObject;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that when a Rest Get is performed against Cougar, supplying a null content type, it is correctly processed as the content type is ignored
 */
public class RestGetIgnoreNullContentTypeTest {
    @Test
    public void doTest() throws Exception {
        // Create the HttpCallBean
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean httpCallBeanBaseline = cougarManager1.getNewHttpCallBean();
        CougarManager cougarManagerBaseline = cougarManager1;
        // Get the cougar logging attribute for getting log entries later
        // Point the created HttpCallBean at the correct service
        httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");
        
        httpCallBeanBaseline.setVersion("v2");
        // Set up the Http Call Bean to make the request
        httpCallBeanBaseline.setOperationName("testSimpleGet", "simple");
        
        Map map2 = new HashMap();
        map2.put("message","foo");
        httpCallBeanBaseline.setQueryParams(map2);
        // Set the content type header, giving a null content type
        Map map3 = new HashMap();
        map3.put("Content-Type","");
        httpCallBeanBaseline.setHeaderParams(map3);
        
        Map map4 = new HashMap();
        map4.put("application/json",null);
        httpCallBeanBaseline.setAcceptProtocols(map4);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp6 = new Timestamp(System.currentTimeMillis());
        // Make the REST call to the operation
        cougarManagerBaseline.makeRestCougarHTTPCall(httpCallBeanBaseline, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.REST);
        // Create the expected response as a JSON object
        JSONHelpers jSONHelpers6 = new JSONHelpers();
        JSONObject expResponse = jSONHelpers6.createAsJSONObject(new JSONObject("{\"message\":\"foo\"}"));
        // Check the response is as expected
        HttpResponseBean response7 = httpCallBeanBaseline.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.REST);
        AssertionUtils.multiAssertEquals(expResponse, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response7.getHttpStatusText());
        // Check the log entries are as expected
        
        cougarManagerBaseline.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp6, new RequestLogRequirement("2.8", "testSimpleGet") );
        
        cougarManagerBaseline.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp6, new AccessLogRequirement(null, null, "Ok") );
    }

}
