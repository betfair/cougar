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

package com.betfair.cougar.tests.updatedcomponenttests.authentication.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


public class RESTAuthenticationErrorCodeTest {
    
    @Test
    public void UnidentifiedCaller_Test() throws Exception {
        doTest("INVALID-UnidentifiedCaller", "<fault><faultcode>Client</faultcode><faultstring>DSC-0033</faultstring><detail/></fault>", 400, "BadRequest", "Bad Request");
    }

    @Test
    public void UnknownCaller_Test() throws Exception {
        doTest("INVALID-UnknownCaller", "<fault><faultcode>Client</faultcode><faultstring>DSC-0034</faultstring><detail/></fault>", 400, "BadRequest", "Bad Request");
    }

    @Test
    public void UnrecognisedCredentials_Test() throws Exception {
        doTest("INVALID-UnrecognisedCredentials", "<fault><faultcode>Client</faultcode><faultstring>DSC-0035</faultstring><detail/></fault>", 400, "BadRequest", "Bad Request");
    }

    @Test
    public void InvalidCredentials_Test() throws Exception {
        doTest("INVALID-InvalidCredentials", "<fault><faultcode>Client</faultcode><faultstring>DSC-0036</faultstring><detail/></fault>", 400, "BadRequest", "Bad Request");
    }

    @Test
    public void SubscriptionRequired_Test() throws Exception {
        doTest("INVALID-SubscriptionRequired", "<fault><faultcode>Client</faultcode><faultstring>DSC-0037</faultstring><detail/></fault>", 403, "Forbidden", "Forbidden");
    }

    @Test
    public void OperationForbidden_Test() throws Exception {
        doTest("INVALID-OperationForbidden", "<fault><faultcode>Client</faultcode><faultstring>DSC-0038</faultstring><detail/></fault>", 403, "Forbidden", "Forbidden");
    }

    @Test
    public void Generic_Test() throws Exception {
        doTest("INVALID", "<fault><faultcode>Client</faultcode><faultstring>DSC-0015</faultstring><detail/></fault>", 403, "Forbidden", "Forbidden");
    }

    private void doTest(String username, String expectedResponseString, int httpErrorCode, String statusText, String responseText) throws Exception {

        CougarManager manager = CougarManager.getInstance();
        HttpCallBean callBean = manager.getNewHttpCallBean("87.248.113.14");
        // Set Cougar Fault Controller attributes
        manager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

        callBean.setOperationName("testSimpleGet", "simple");

        callBean.setServiceName("baseline", "cougarBaseline");

        callBean.setVersion("v2");

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("message","foo");
        callBean.setQueryParams(queryParams);

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("Username", username);
        callBean.setAuthCredentials(credentials);


        Timestamp requestTime = new Timestamp(System.currentTimeMillis());

        manager.makeRestCougarHTTPCalls(callBean);

        XMLHelpers xMLHelpers = new XMLHelpers();
        Document expectedResponse = xMLHelpers.getXMLObjectFromString(expectedResponseString);

        Map<CougarMessageProtocolRequestTypeEnum, Object> expectedResponses = manager.convertResponseToRestTypes(expectedResponse, callBean);

        HttpResponseBean getResponseObjectsByEnum12 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(expectedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), getResponseObjectsByEnum12.getResponseObject());
        AssertionUtils.multiAssertEquals(httpErrorCode, getResponseObjectsByEnum12.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(responseText, getResponseObjectsByEnum12.getHttpStatusText());

        HttpResponseBean getResponseObjectsByEnum14 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(expectedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), getResponseObjectsByEnum14.getResponseObject());
        AssertionUtils.multiAssertEquals(httpErrorCode, getResponseObjectsByEnum14.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(responseText, getResponseObjectsByEnum14.getHttpStatusText());

        HttpResponseBean getResponseObjectsByEnum16 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(expectedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), getResponseObjectsByEnum16.getResponseObject());
        AssertionUtils.multiAssertEquals(httpErrorCode, getResponseObjectsByEnum16.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(responseText, getResponseObjectsByEnum16.getHttpStatusText());

        HttpResponseBean getResponseObjectsByEnum18 = callBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(expectedResponses.get(CougarMessageProtocolRequestTypeEnum.RESTXML), getResponseObjectsByEnum18.getResponseObject());
        AssertionUtils.multiAssertEquals(httpErrorCode, getResponseObjectsByEnum18.getHttpStatusCode());
        AssertionUtils.multiAssertEquals(responseText, getResponseObjectsByEnum18.getHttpStatusText());

        manager.verifyServiceLogEntriesAfterDate(requestTime, 1000);

        manager.verifyRequestLogEntriesAfterDate(requestTime, 1000);

        manager.verifyAccessLogEntriesAfterDate(requestTime,
                new AccessLogRequirement(null, null, statusText),
                new AccessLogRequirement(null, null, statusText),
                new AccessLogRequirement(null, null, statusText),
                new AccessLogRequirement(null, null, statusText));
    }
}
