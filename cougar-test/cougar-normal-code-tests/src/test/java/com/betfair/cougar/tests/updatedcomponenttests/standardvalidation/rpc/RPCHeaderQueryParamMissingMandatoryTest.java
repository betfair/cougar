/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

// Originally from UpdatedComponentTests/StandardValidation/RPC/RPC_HeaderQueryParamMissingMandatory.xls;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar returns the correct fault when a  RPC request is made with a mandatory header parameter missing
 */
public class RPCHeaderQueryParamMissingMandatoryTest {
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
        Map[] mapArray3 = new Map[3];
        mapArray3[0] = new HashMap();
        mapArray3[0].put("method","testParameterStylesQA");
        mapArray3[0].put("params","[\"Foo\", \"query\", \"2009-06-01\" ]");
        mapArray3[0].put("id","1");
        mapArray3[1] = new HashMap();
        mapArray3[1].put("method","testParameterStylesQA");
        mapArray3[1].put("params","[]");
        mapArray3[1].put("id","2");
        mapArray3[2] = new HashMap();
        mapArray3[2].put("method","testParameterStylesQA");
        mapArray3[2].put("params","[\"Foo\"]");
        mapArray3[2].put("id","3");
        callBean.setBatchedRequests(mapArray3);
        // Get current time for getting log entries later

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        // Make JSON call to the operation requesting a JSON response
        cougarManager.makeRestCougarHTTPCall(callBean, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Get the response to the batched query (store the response for further comparison as order of batched responses cannot be relied on)
        HttpResponseBean actualResponseJSON = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);

        CougarHelpers cougarHelpers5 = new CougarHelpers();
        Date convertedDate1 = cougarHelpers5.convertToSystemTimeZone("2009-06-01T00:00:00.0Z");
        // Convert the returned json object to a map for comparison
        CougarHelpers cougarHelpers6 = new CougarHelpers();
        Map<String, Object> map7 = cougarHelpers6.convertBatchedResponseToMap(actualResponseJSON);
        AssertionUtils.multiAssertEquals("{\"id\":1,\"result\":{\"message\":\"headerParam=Foo,queryParam=query,dateQueryParam="+cougarHelpers5.dateInUTC(convertedDate1)+"\"},\"jsonrpc\":\"2.0\"}", map7.get("response1"));
        AssertionUtils.multiAssertEquals("{\"id\":2,\"error\":{\"message\":\"DSC-0018\",\"code\":-32602},\"jsonrpc\":\"2.0\"}", map7.get("response2"));
        AssertionUtils.multiAssertEquals("{\"id\":3,\"error\":{\"message\":\"DSC-0018\",\"code\":-32602},\"jsonrpc\":\"2.0\"}", map7.get("response3"));
        AssertionUtils.multiAssertEquals(200, map7.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map7.get("httpStatusText"));
        // Pause the test to allow the logs to be filled
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected

        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "testParameterStylesQA") );

        CougarManager cougarManager11 = CougarManager.getInstance();
        cougarManager11.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement("87.248.113.14", "/json-rpc", "Ok") );
    }

}
