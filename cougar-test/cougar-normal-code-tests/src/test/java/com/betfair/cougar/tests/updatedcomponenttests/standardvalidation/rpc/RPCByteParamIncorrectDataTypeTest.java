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

// Originally from UpdatedComponentTests/StandardValidation/RPC/RPC_ByteParam_IncorrectDataType.xls;
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

import static org.testng.AssertJUnit.fail;

/**
 * Ensure that Cougar returns the correct fault when a  RPC request with byte param  is made with incorrect data type parameter
 */
public class RPCByteParamIncorrectDataTypeTest {

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
        mapArray3[0].put("method","byteOperation");
        mapArray3[0].put("params","[1, 2,{\"bodyParameter\":\"EYitl82RbhhPWMZKw2MNlxF4kIGuX03TWEPUBbAxaBs=\"}]");
        mapArray3[0].put("id","\"Call with correct params\"");
        mapArray3[1] = new HashMap();
        mapArray3[1].put("method","byteOperation");
        mapArray3[1].put("params","[128, 2,{\"bodyParameter\":\"EYitl82RbhhPWMZKw2MNlxF4kIGuX03TWEPUBbAxaBs=\"}]");
        mapArray3[1].put("id","\"Incorrect Header param\"");
        mapArray3[2] = new HashMap();
        mapArray3[2].put("method","byteOperation");
        mapArray3[2].put("params","[1, 128,{\"bodyParameter\":\"EYitl82RbhhPWMZKw2MNlxF4kIGuX03TWEPUBbAxaBs=\"}]");
        mapArray3[2].put("id","\"Incorrect Query param\"");
        mapArray3[3] = new HashMap();
        mapArray3[3].put("method","byteOperation");
        mapArray3[3].put("params","[1, 1,{\"bodyParameter\":128}]");
        mapArray3[3].put("id","\"Incorrect Body param\"");
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
        AssertionUtils.multiAssertEquals("{\"id\":\"Call with correct params\",\"result\":{\"headerParameter\":1,\"queryParameter\":2,\"bodyParameter\":\"EYitl82RbhhPWMZKw2MNlxF4kIGuX03TWEPUBbAxaBs=\"},\"jsonrpc\":\"2.0\"}", map6.get("responseCall with correct params"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Incorrect Header param\",\"error\":{\"message\":\"DSC-0044\",\"code\":-32602},\"jsonrpc\":\"2.0\"}", map6.get("responseIncorrect Header param"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Incorrect Query param\",\"error\":{\"message\":\"DSC-0044\",\"code\":-32602},\"jsonrpc\":\"2.0\"}", map6.get("responseIncorrect Query param"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Incorrect Body param\",\"error\":{\"message\":\"DSC-0044\",\"code\":-32602},\"jsonrpc\":\"2.0\"}", map6.get("responseIncorrect Body param"));
        AssertionUtils.multiAssertEquals(200, map6.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map6.get("httpStatusText"));
        // Pause the test to allow the logs to be filled
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected

        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "byteOperation") );

        CougarManager cougarManager10 = CougarManager.getInstance();
        cougarManager10.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement("87.248.113.14", "/json-rpc", "Ok") );
    }

}
