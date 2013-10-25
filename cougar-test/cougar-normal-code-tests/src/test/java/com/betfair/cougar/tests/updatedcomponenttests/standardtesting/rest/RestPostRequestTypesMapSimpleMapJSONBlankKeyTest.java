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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_Map_SimpleMap_JSON_BlankKey.xls;
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
 * Ensure that when a Rest (JSON) Post operation is performed, Cougar can correctly handle a simple Map with a blank key in the post body
 */
public class RestPostRequestTypesMapSimpleMapJSONBlankKeyTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        getNewHttpCallBean1.setOperationName("simpleMapOperation");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Set the post body to contain a simple map with a blank key
        Map map2 = new HashMap();
        map2.put("RESTJSON","{\"message\":\n           {\"simpleMap\": \n           {\"bbb\": \"Value for bbb\", \n            \"aaa\":\"Value for aaa\",\n            \"\"   : \"Blank key\", \n            \"ccc\": \"Value for ccc\",\n            \"AAA\": \"Value for AAA\"}\n}} ");
        getNewHttpCallBean1.setPostQueryObjects(map2);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        // Make JSON call to the operation requesting an XML response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make JSON call to the operation requesting a JSON response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<SimpleMapOperationResponse><SimpleMapOperationResponseObject><responseMap><entry key=\"\"><String>Blank key</String></entry><entry key=\"AAA\"><String>Value for AAA</String></entry><entry key=\"aaa\"><String>Value for aaa</String></entry><entry key=\"bbb\"><String>Value for bbb</String></entry><entry key=\"ccc\"><String>Value for ccc</String></entry></responseMap></SimpleMapOperationResponseObject></SimpleMapOperationResponse>");
        // Create the expected response as a JSON object
        JSONHelpers jSONHelpers5 = new JSONHelpers();
        JSONObject createAsJSONObject11 = jSONHelpers5.createAsJSONObject(new JSONObject("{\"responseMap\": {\"\": \"Blank key\",\"AAA\": \"Value for AAA\",\"aaa\": \"Value for aaa\",\"bbb\": \"Value for bbb\",\"ccc\": \"Value for ccc\"}}"));
        // Check the 2 responses are as expected
        HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(createAsDocument10, response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());
        
        HttpResponseBean response7 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(createAsJSONObject11, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response7.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "simpleMapOperation"),new RequestLogRequirement("2.8", "simpleMapOperation") );
    }

}
