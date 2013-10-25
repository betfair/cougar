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

// Originally from UpdatedComponentTests/StandardValidation/REST/Rest_Service_MissingPostBody.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.rest;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;

import java.sql.Timestamp;

/**
 * Ensure that Cougar returns the correct fault, when a REST POST request is made with a missing post body
 */
public class RestServiceMissingPostBodyTest {
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
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean("87.248.113.14");
        cougarManager2 = cougarManager2;
        
        cougarManager2.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        
        getNewHttpCallBean2.setOperationName("testLargePost");
        
        getNewHttpCallBean2.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean2.setVersion("v2");
        // Don't set the post body to anything
        getNewHttpCallBean2.setRestPostQueryObjects(null);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the invalid operation
        cougarManager2.makeRestCougarHTTPCalls(getNewHttpCallBean2);
        // Check the 4 responses are as expected (Not Found)
        HttpResponseBean response4 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals((int) 404, response4.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", response4.getHttpStatusText());
        
        HttpResponseBean response5 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals((int) 404, response5.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", response5.getHttpStatusText());
        
        HttpResponseBean response6 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals((int) 404, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", response6.getHttpStatusText());
        
        HttpResponseBean response7 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals((int) 404, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Not Found", response7.getHttpStatusText());
        // Check the log entries are as expected
        
        CougarManager cougarManager9 = CougarManager.getInstance();
        cougarManager9.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp9, new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/testLargePost", "NotFound"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/testLargePost", "NotFound"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/testLargePost", "NotFound"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/testLargePost", "NotFound") );
    }

}
