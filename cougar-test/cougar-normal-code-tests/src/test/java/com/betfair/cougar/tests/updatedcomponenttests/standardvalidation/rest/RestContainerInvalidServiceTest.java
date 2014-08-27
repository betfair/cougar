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

// Originally from UpdatedComponentTests/StandardValidation/REST/Rest_Container_InvalidService.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.rest;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar returns the correct 404 html page when a  REST XML/JSON request is made to a Service that doesn't exist. Error should be "Not Found"
 */
public class RestContainerInvalidServiceTest {
    @Test
    public void doTest() throws Exception {
        // Create the HttpCallBean
        CougarManager cougarManager = CougarManager.getInstance();
        HttpCallBean httpCallBeanBaseline = cougarManager.getNewHttpCallBean();
        // Point the created HttpCallBean at the correct service
        httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");

        httpCallBeanBaseline.setVersion("v2");
        // Set up the Http Call Bean to make the request
        HttpCallBean getNewHttpCallBean = cougarManager.getNewHttpCallBean("87.248.113.14");

        cougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

        getNewHttpCallBean.setOperationName("testSimpleGet", "simple");
        // Point the request at a service that doesn't exist
        getNewHttpCallBean.setServiceName("invalidservice", "invalidExtension");

        getNewHttpCallBean.setVersion("v2");

        Map map3 = new HashMap();
        map3.put("message", "foo");
        getNewHttpCallBean.setQueryParams(map3);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the operation (store the expected HTML response as a string)
        cougarManager.makeRestCougarHTTPCalls(getNewHttpCallBean);
        // Make the 4 REST calls to the operation (store the expected HTML response as a string)
        cougarManager.makeRestCougarHTTPCalls(getNewHttpCallBean);
        String expectedHTML = "<!--  ~ Copyright 2014, The Sporting Exchange Limited  ~  ~ Licensed under the Apache License, Version 2.0 (the \"License\");  ~ you may not use this file except in compliance with the License.  ~ You may obtain a copy of the License at  ~  ~     http://www.apache.org/licenses/LICENSE-2.0  ~  ~ Unless required by applicable law or agreed to in writing, software  ~ distributed under the License is distributed on an \"AS IS\" BASIS,  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~ See the License for the specific language governing permissions and  ~ limitations under the License.  --><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Service not Found</title></head><body><b>The URL you specified did not correspond to a service.x</b></body></html>";
        // Check the 4 responses are as expected (Not Found error)
        AssertionUtils.multiAssertEquals(expectedHTML, getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLXML).getResponseObject());
        AssertionUtils.multiAssertEquals(404, getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLXML).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLXML).getHttpStatusText());
        AssertionUtils.multiAssertEquals(expectedHTML,getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getResponseObject());
        AssertionUtils.multiAssertEquals(404, getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getHttpStatusText());
        AssertionUtils.multiAssertEquals(expectedHTML,getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLJSON).getResponseObject());
        AssertionUtils.multiAssertEquals(404, getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLJSON).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLJSON).getHttpStatusText());
        AssertionUtils.multiAssertEquals(expectedHTML,getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getResponseObject());
        AssertionUtils.multiAssertEquals(404, getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", getNewHttpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getHttpStatusText());
        // Check the log entries are as expected
        cougarManager.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp9
                , new AccessLogRequirement("87.248.113.14", "/invalidExtension/v2/simple", "NotFound")
                , new AccessLogRequirement("87.248.113.14", "/invalidExtension/v2/simple", "NotFound")
                , new AccessLogRequirement("87.248.113.14", "/invalidExtension/v2/simple", "NotFound")
                , new AccessLogRequirement("87.248.113.14", "/invalidExtension/v2/simple", "NotFound")
        );
    }

}
