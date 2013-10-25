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

// Originally from UpdatedComponentTests/Geolocate/RPC/RPC_Geolocate_ByIPAddress_InvalidIP.xls;
package com.betfair.cougar.tests.updatedcomponenttests.geolocate.rpc;

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
 * Ensure that when a Batched JSON request is received by Cougar, passing an invalid ipaddress (in this case, multiple Comma Seperated Addresses), the local IP address is used as default
 */
public class RPCGeolocateByIPAddressInvalidIPTest {
    String localIPAddress = "127.0.0.1";
    String invalidIPAddress = "NOT AN IP Address";

    @Test
    public void doTest_Single_IP() throws Exception {
        String testIPAddress = invalidIPAddress;

        doTest(testIPAddress, localIPAddress);
    }

    @Test
    public void doTest_FirstItem_IPList() throws Exception {
        String testIPAddress = invalidIPAddress + ",87.248.113.14,1.2.3.4,5.6.7.8";
        String expectedIPAddress = "87.248.113.14;1.2.3.4;5.6.7.8";

        doTest(testIPAddress, expectedIPAddress);
    }

    @Test
    public void doTest_MiddleItem_IPList() throws Exception {
        String testIPAddress = "87.248.113.14,1.2.3.4," + invalidIPAddress + ",5.6.7.8";
        String expectedIPAddress = "87.248.113.14;1.2.3.4;5.6.7.8";

        doTest(testIPAddress, expectedIPAddress);
    }

    @Test
    public void doTest_LastItem_IPList() throws Exception {
        String testIPAddress = "87.248.113.14,1.2.3.4,5.6.7.8," + invalidIPAddress;
        String expectedIPAddress = "87.248.113.14;1.2.3.4;5.6.7.8";

        doTest(testIPAddress, expectedIPAddress);
    }


    @Test
    public void doTest_AllItemInvalid_IPList() throws Exception {
        String testIPAddress =  "Another Random One" +  "," + invalidIPAddress + "," + "And Another Random Bit" ;

        doTest(testIPAddress, localIPAddress);
    }

    @Test
    public void doTest_SingleValidItem_IPList() throws Exception {
        String validIPAddress = "87.248.113.14";
        String testIPAddress =  "Another Random One" +  "," + invalidIPAddress + "," + "And Another Random Bit" + "," + validIPAddress + "," + "And More Random Bit" ;

        doTest(testIPAddress, validIPAddress);
    }


    private void doTest(String testIPAddress, String expectedIPAddress) throws Exception
    {
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean callBean = cougarManager1.getNewHttpCallBean(testIPAddress);
         CougarManager cougarManager = cougarManager1;
        // Set the call bean to use JSON batching
        callBean.setJSONRPC(true);
        // Set the list of requests to make a batched call to
        Map[] mapArray2 = new Map[2];
        mapArray2[0] = new HashMap();
        mapArray2[0].put("method","testSimpleGet");
        mapArray2[0].put("params","[\"foo\"]");
        mapArray2[0].put("id","1");
        mapArray2[1] = new HashMap();
        mapArray2[1].put("method","testSimpleGet");
        mapArray2[1].put("params","[\"foo\"]");
        mapArray2[1].put("id","2");
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
        AssertionUtils.multiAssertEquals("{\"id\":1,\"result\":{\"message\":\"foo\"},\"jsonrpc\":\"2.0\"}", map5.get("response1"));
        AssertionUtils.multiAssertEquals("{\"id\":2,\"result\":{\"message\":\"foo\"},\"jsonrpc\":\"2.0\"}", map5.get("response2"));
        AssertionUtils.multiAssertEquals("200", map5.get("httpStatusCode"));
        AssertionUtils.multiAssertEquals("OK", map5.get("httpStatusText"));

        // Check the log entries are as expected
        cougarManager.verifyRequestLogEntriesAfterDate(timeStamp, new RequestLogRequirement("2.8", "testSimpleGet"), new RequestLogRequirement("2.8", "testSimpleGet"));

        // In particular check the IPLocation field of the Access log to check that it defaulted to use the local host address / Or Expected IP Address Excluding Invalid IP
        String expectedIPLocation = expectedIPAddress.equals(localIPAddress) ? "--" : "GB";
        cougarManager.verifyAccessLogEntriesAfterDate(timeStamp, new AccessLogRequirement(expectedIPAddress, expectedIPLocation, "/json-rpc", "Ok"));
    }
}
