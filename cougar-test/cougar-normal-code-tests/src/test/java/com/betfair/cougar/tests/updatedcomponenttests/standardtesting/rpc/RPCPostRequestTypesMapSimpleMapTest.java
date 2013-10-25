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

// Originally from UpdatedComponentTests/StandardTesting/RPC/RPC_Post_RequestTypes_Map_SimpleMap.xls;
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
 * Ensure that Cougar can handle a SimpleMap in the post body of an RPC request
 */
public class RPCPostRequestTypesMapSimpleMapTest {
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
        Map[] mapArray2 = new Map[6];
        mapArray2[0] = new HashMap();
        mapArray2[0].put("method","simpleMapOperation");
        mapArray2[0].put("params","[{\"simpleMap\":{\"bbb\":\"Value for bbb\",\"AAA\":\"Value for AAA\"}}]");
        mapArray2[0].put("id","\"SimpleMap\"");
        mapArray2[1] = new HashMap();
        mapArray2[1].put("method","simpleMapOperation");
        mapArray2[1].put("params","[{\"simpleMap\":{\"\":\"Value for blank\",\"AAA\":\"Value for AAA\"}}]");
        mapArray2[1].put("id","\"SimpleMapBlankKey\"");
        mapArray2[2] = new HashMap();
        mapArray2[2].put("method","simpleMapOperation");
        mapArray2[2].put("params","[{\"simpleMap\":{\"bbb\":\"\",\"AAA\":\"Value for AAA\"}}]");
        mapArray2[2].put("id","\"SimpleMapBlankValue\"");
        mapArray2[3] = new HashMap();
        mapArray2[3].put("method","simpleMapOperation");
        mapArray2[3].put("params","[{\"simpleMap\":{\"bbb\":\"Value for bbb\",\"bbb\":\"Value for zzz\"}}]");
        mapArray2[3].put("id","\"SimpleMapDupKey\"");
        mapArray2[4] = new HashMap();
        mapArray2[4].put("method","simpleMapOperation");
        mapArray2[4].put("params","[{\"simpleMap\":{}}]");
        mapArray2[4].put("id","\"SimpleMapEmpty\"");
        mapArray2[5] = new HashMap();
        mapArray2[5].put("method","simpleMapOperation");
        mapArray2[5].put("params","[{\"simpleMap\":{\"bbb\":\"Value for bbb\",\"aaa\":null}}]");
        mapArray2[5].put("id","\"SimpleMapNullValue\"");
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
        AssertionUtils.multiAssertEquals("{\"id\":\"SimpleMap\",\"result\":{\"responseMap\":{\"AAA\":\"Value for AAA\",\"bbb\":\"Value for bbb\"}},\"jsonrpc\":\"2.0\"}", map5.get("responseSimpleMap"));
        AssertionUtils.multiAssertEquals("{\"id\":\"SimpleMapBlankKey\",\"result\":{\"responseMap\":{\"\":\"Value for blank\",\"AAA\":\"Value for AAA\"}},\"jsonrpc\":\"2.0\"}", map5.get("responseSimpleMapBlankKey"));
        AssertionUtils.multiAssertEquals("{\"id\":\"SimpleMapBlankValue\",\"result\":{\"responseMap\":{\"AAA\":\"Value for AAA\",\"bbb\":\"\"}},\"jsonrpc\":\"2.0\"}", map5.get("responseSimpleMapBlankValue"));
        AssertionUtils.multiAssertEquals("{\"id\":\"SimpleMapDupKey\",\"result\":{\"responseMap\":{\"bbb\":\"Value for zzz\"}},\"jsonrpc\":\"2.0\"}", map5.get("responseSimpleMapDupKey"));
        AssertionUtils.multiAssertEquals("{\"id\":\"SimpleMapEmpty\",\"result\":{\"responseMap\":{}},\"jsonrpc\":\"2.0\"}", map5.get("responseSimpleMapEmpty"));
        AssertionUtils.multiAssertEquals("{\"id\":\"SimpleMapNullValue\",\"result\":{\"responseMap\":{\"aaa\":null,\"bbb\":\"Value for bbb\"}},\"jsonrpc\":\"2.0\"}", map5.get("responseSimpleMapNullValue"));
        AssertionUtils.multiAssertEquals(200, map5.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map5.get("httpStatusText"));
        // Pause the test to allow the logs to be filled
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "simpleMapOperation"),new RequestLogRequirement("2.8", "simpleMapOperation"),new RequestLogRequirement("2.8", "simpleMapOperation"),new RequestLogRequirement("2.8", "simpleMapOperation"),new RequestLogRequirement("2.8", "simpleMapOperation"),new RequestLogRequirement("2.8", "simpleMapOperation") );
        
        CougarManager cougarManager9 = CougarManager.getInstance();
        cougarManager9.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement("87.248.113.14", "/json-rpc", "Ok") );
    }

}
