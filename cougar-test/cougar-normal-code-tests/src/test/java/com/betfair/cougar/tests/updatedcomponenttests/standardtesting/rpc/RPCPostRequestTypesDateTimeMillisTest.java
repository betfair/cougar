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

// Originally from UpdatedComponentTests/StandardTesting/RPC/RPC_Post_RequestTypes_DateTime_Millis.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rpc;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar can handle the dateTime data type with various milliseconds in the post body of an RPC request
 */
public class RPCPostRequestTypesDateTimeMillisTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean callBean = cougarManager1.getNewHttpCallBean("87.248.113.14");
        CougarManager cougarManager = cougarManager1;
        
        cougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set the call bean to use JSON batching
        callBean.setJSONRPC(true);
        // Set the list of requests to make a batched call to
        Map[] mapArray2 = new Map[8];
        mapArray2[0] = new HashMap();
        mapArray2[0].put("method","dateTimeOperation");
        mapArray2[0].put("params","[{\"dateTimeParameter\":\"2009-06-01T13:50:00.435Z\"}]");
        mapArray2[0].put("id","\"DateTimeMillis\"");
        mapArray2[1] = new HashMap();
        mapArray2[1].put("method","dateTimeOperation");
        mapArray2[1].put("params","[{\"dateTimeParameter\":\"2009-01-01T13:50:00.435Z\"}]");
        mapArray2[1].put("id","\"DateTimeMillis2\"");
        mapArray2[2] = new HashMap();
        mapArray2[2].put("method","dateTimeOperation");
        mapArray2[2].put("params","[{\"dateTimeParameter\":\"2009-01-01T13:50:00.43Z\"}]");
        mapArray2[2].put("id","\"DateTimeMillisFill\"");
        mapArray2[3] = new HashMap();
        mapArray2[3].put("method","dateTimeOperation");
        mapArray2[3].put("params","[{\"dateTimeParameter\":\"2009-01-01T13:50:00.4Z\"}]");
        mapArray2[3].put("id","\"DateTimeMillisFill2\"");
        mapArray2[4] = new HashMap();
        mapArray2[4].put("method","dateTimeOperation");
        mapArray2[4].put("params","[{\"dateTimeParameter\":\"2009-01-01T13:50:00.4343Z\"}]");
        mapArray2[4].put("id","\"DateTimeMillisRound\"");
        mapArray2[5] = new HashMap();
        mapArray2[5].put("method","dateTimeOperation");
        mapArray2[5].put("params","[{\"dateTimeParameter\":\"2009-01-01T13:50:00.4346Z\"}]");
        mapArray2[5].put("id","\"DateTimeMillisRound2\"");
        mapArray2[6] = new HashMap();
        mapArray2[6].put("method","dateTimeOperation");
        mapArray2[6].put("params","[{\"dateTimeParameter\":\"2009-06-01T13:50:00Z\"}]");
        mapArray2[6].put("id","\"DateTimeMillisNotSpec\"");
        mapArray2[7] = new HashMap();
        mapArray2[7].put("method","dateTimeOperation");
        mapArray2[7].put("params","[{\"dateTimeParameter\":\"2009-02-01T13:50:00Z\"}]");
        mapArray2[7].put("id","\"DateTimeMillisNotSpec2\"");
        callBean.setBatchedRequests(mapArray2);
        // Get current time for getting log entries later

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        // Make JSON call to the operation requesting a JSON response
        cougarManager.makeRestCougarHTTPCall(callBean, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Get the response to the batched query (store the response for further comparison as order of batched responses cannot be relied on)
        HttpResponseBean actualResponseJSON = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        // Convert the returned json object to a map for comparison
        CougarHelpers cougarHelpers4 = new CougarHelpers();
        Map<String, Object> map5 = cougarHelpers4.convertBatchedResponseToMap(actualResponseJSON);
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillis\",\"result\":{\"localTime\":\"2009-06-01T13:50:00.435Z\",\"localTime2\":\"2009-06-01T13:50:00.435Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillis"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillis2\",\"result\":{\"localTime\":\"2009-01-01T13:50:00.435Z\",\"localTime2\":\"2009-01-01T13:50:00.435Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillis2"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillisFill\",\"result\":{\"localTime\":\"2009-01-01T13:50:00.430Z\",\"localTime2\":\"2009-01-01T13:50:00.430Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillisFill"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillisFill2\",\"result\":{\"localTime\":\"2009-01-01T13:50:00.400Z\",\"localTime2\":\"2009-01-01T13:50:00.400Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillisFill2"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillisRound\",\"result\":{\"localTime\":\"2009-01-01T13:50:00.434Z\",\"localTime2\":\"2009-01-01T13:50:00.434Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillisRound"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillisRound2\",\"result\":{\"localTime\":\"2009-01-01T13:50:00.434Z\",\"localTime2\":\"2009-01-01T13:50:00.434Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillisRound2"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillisNotSpec\",\"result\":{\"localTime\":\"2009-06-01T13:50:00.000Z\",\"localTime2\":\"2009-06-01T13:50:00.000Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillisNotSpec"));
        AssertionUtils.multiAssertEquals("{\"id\":\"DateTimeMillisNotSpec2\",\"result\":{\"localTime\":\"2009-02-01T13:50:00.000Z\",\"localTime2\":\"2009-02-01T13:50:00.000Z\"},\"jsonrpc\":\"2.0\"}", map5.get("responseDateTimeMillisNotSpec2"));
        AssertionUtils.multiAssertEquals(200, map5.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map5.get("httpStatusText"));
        // Pause the test to allow the logs to be filled
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation"),new RequestLogRequirement("2.8", "dateTimeOperation") );
        
        CougarManager cougarManager9 = CougarManager.getInstance();
        cougarManager9.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement("87.248.113.14", "/json-rpc", "Ok") );
    }

}
