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

// Originally from UpdatedComponentTests/StandardValidation/RPC/RPC_InvalidService.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.rpc;

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
 * Ensure that Cougar returns the correct fault when a  RPC request is made to a Service that doesn't exist. Error should be "Not Found"
 */
public class RPCInvalidServiceTest {
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
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean callBean = cougarManager2.getNewHttpCallBean("87.248.113.14");
        CougarManager cougarManager = cougarManager2;
        
        cougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set the call bean to use JSON batching
        callBean.setJSONRPC(true);
        // Set the list of requests to make a batched call to
        Map[] mapArray3 = new Map[4];
        mapArray3[0] = new HashMap();
        mapArray3[0].put("method","testSimpleGet");
        mapArray3[0].put("params","[\"foo1\"]");
        mapArray3[0].put("id","\"OK\"");
        mapArray3[0].put("version","2.8");
        mapArray3[0].put("service","Baseline");
        mapArray3[1] = new HashMap();
        mapArray3[1].put("method","nonExistentMethod");
        mapArray3[1].put("params","[\"foo2\"]");
        mapArray3[1].put("id","\"NoMethod\"");
        mapArray3[1].put("version","2.8");
        mapArray3[1].put("service","Baseline");
        mapArray3[2] = new HashMap();
        mapArray3[2].put("method","testSimpleGet");
        mapArray3[2].put("params","[\"foo3\"]");
        mapArray3[2].put("id","\"NoVersion\"");
        mapArray3[2].put("version","3.0");
        mapArray3[2].put("service","Baseline");
        mapArray3[3] = new HashMap();
        mapArray3[3].put("method","testSimpleGet");
        mapArray3[3].put("params","[\"foo4\"]");
        mapArray3[3].put("id","\"NoService\"");
        mapArray3[3].put("version","2.8");
        mapArray3[3].put("service","nonExistantService");
        callBean.setBatchedRequests(mapArray3);
        // Get current time for getting log entries later

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        // Make JSON call to the operation requesting a JSON response
        cougarManager.makeRestCougarHTTPCall(callBean, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Get the response to the batched query (store the response for further comparison as order of batched responses cannot be relied on)
        HttpResponseBean actualResponseJSON = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        // Convert the returned json object to a map for comparison
        CougarHelpers cougarHelpers5 = new CougarHelpers();
        Map<String, Object> map6 = cougarHelpers5.convertBatchedResponseToMap(actualResponseJSON);
        AssertionUtils.multiAssertEquals("{\"id\":\"OK\",\"result\":{\"message\":\"foo1\"},\"jsonrpc\":\"2.0\"}", map6.get("responseOK"));
        AssertionUtils.multiAssertEquals("{\"id\":\"NoMethod\",\"error\":{\"message\":\"DSC-0021\",\"code\":-32601},\"jsonrpc\":\"2.0\"}", map6.get("responseNoMethod"));
        AssertionUtils.multiAssertEquals("{\"id\":\"NoVersion\",\"error\":{\"message\":\"DSC-0021\",\"code\":-32601},\"jsonrpc\":\"2.0\"}", map6.get("responseNoVersion"));
        AssertionUtils.multiAssertEquals("{\"id\":\"NoService\",\"error\":{\"message\":\"DSC-0021\",\"code\":-32601},\"jsonrpc\":\"2.0\"}", map6.get("responseNoService"));
        AssertionUtils.multiAssertEquals(200, map6.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map6.get("httpStatusText"));
        // Pause the test to allow the logs to be filled
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "testSimpleGet") );
        
        CougarManager cougarManager10 = CougarManager.getInstance();
        cougarManager10.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement("87.248.113.14", "/json-rpc", "Ok") );
    }

}
