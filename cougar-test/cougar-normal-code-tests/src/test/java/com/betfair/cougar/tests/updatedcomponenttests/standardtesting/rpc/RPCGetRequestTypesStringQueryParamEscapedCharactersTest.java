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

// Originally from UpdatedComponentTests/StandardTesting/RPC/RPC_Get_RequestTypes_String_QueryParam_EscapedCharacters.xls;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar can correctly handle a string Query Param containing various encoded escaped characters (within a Batched JSON request)
 */
public class RPCGetRequestTypesStringQueryParamEscapedCharactersTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean callBean = cougarManager1.getNewHttpCallBean("87.248.113.14");
        CougarManager cougarManager = cougarManager1;
        // Set the call bean to use JSON batching
        callBean.setJSONRPC(true);
        // Convert the time to system TimeZone
        CougarHelpers cougarHelpers2 = new CougarHelpers();
        Date convertedDate1 = cougarHelpers2.convertToSystemTimeZone("2011-01-01T00:00:00.0Z");
        // Set the list of requests to make a batched call to
        Map[] mapArray3 = new Map[5];
        mapArray3[0] = new HashMap();
        mapArray3[0].put("method","testParameterStylesQA");
        mapArray3[0].put("params","[\"Foo\",\"this & that\",\"2011-01-01\"]");
        mapArray3[0].put("id","\"And\"");
        mapArray3[1] = new HashMap();
        mapArray3[1].put("method","testParameterStylesQA");
        mapArray3[1].put("params","[\"Foo\",\"hash#\",\"2011-01-01\"]");
        mapArray3[1].put("id","\"Hash\"");
        mapArray3[2] = new HashMap();
        mapArray3[2].put("method","testParameterStylesQA");
        mapArray3[2].put("params","[\"Foo\",\"this & that is 100%\",\"2011-01-01\"]");
        mapArray3[2].put("id","\"Combo\"");
        mapArray3[3] = new HashMap();
        mapArray3[3].put("method","testParameterStylesQA");
        mapArray3[3].put("params","[\"Foo\",\"colon:\",\"2011-01-01\"]");
        mapArray3[3].put("id","\"Colon\"");
        mapArray3[4] = new HashMap();
        mapArray3[4].put("method","testParameterStylesQA");
        mapArray3[4].put("params","[\"Foo\",\"a space\",\"2011-01-01\"]");
        mapArray3[4].put("id","\"Space\"");
        callBean.setBatchedRequests(mapArray3);
        // Get current time for getting log entries later

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        // Make JSON call to the operation requesting a JSON response
        cougarManager.makeRestCougarHTTPCall(callBean, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Get the response to the batched query (store the response for further comparison as order of batched responses cannot be relied on)
        HttpResponseBean response = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        // Convert the returned json object to a map for comparison
        CougarHelpers cougarHelpers5 = new CougarHelpers();
        Map<String, Object> map6 = cougarHelpers5.convertBatchedResponseToMap(response);
        AssertionUtils.multiAssertEquals("{\"id\":\"And\",\"result\":{\"message\":\"headerParam=Foo,queryParam=this & that,dateQueryParam="+cougarHelpers2.dateInUTC(convertedDate1)+"\"},\"jsonrpc\":\"2.0\"}", map6.get("responseAnd"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Colon\",\"result\":{\"message\":\"headerParam=Foo,queryParam=colon:,dateQueryParam="+cougarHelpers2.dateInUTC(convertedDate1)+"\"},\"jsonrpc\":\"2.0\"}", map6.get("responseColon"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Combo\",\"result\":{\"message\":\"headerParam=Foo,queryParam=this & that is 100%,dateQueryParam="+cougarHelpers2.dateInUTC(convertedDate1)+"\"},\"jsonrpc\":\"2.0\"}", map6.get("responseCombo"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Hash\",\"result\":{\"message\":\"headerParam=Foo,queryParam=hash#,dateQueryParam="+cougarHelpers2.dateInUTC(convertedDate1)+"\"},\"jsonrpc\":\"2.0\"}", map6.get("responseHash"));
        AssertionUtils.multiAssertEquals("{\"id\":\"Space\",\"result\":{\"message\":\"headerParam=Foo,queryParam=a space,dateQueryParam="+cougarHelpers2.dateInUTC(convertedDate1)+"\"},\"jsonrpc\":\"2.0\"}", map6.get("responseSpace"));
        AssertionUtils.multiAssertEquals(200, map6.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map6.get("httpStatusText"));
        // Pause the test to allow the logs to be filled
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected

        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "testParameterStylesQA"),new RequestLogRequirement("2.8", "testParameterStylesQA"),new RequestLogRequirement("2.8", "testParameterStylesQA"),new RequestLogRequirement("2.8", "testParameterStylesQA"),new RequestLogRequirement("2.8", "testParameterStylesQA") );

        cougarManager.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement("87.248.113.14", "/json-rpc", "Ok") );
    }

}
