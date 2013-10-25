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

// Originally from UpdatedComponentTests/Concurrency/Rest/Rest_Post_SustainedRequestsAssertedXMLJSON.xls;
package com.betfair.cougar.tests.updatedcomponenttests.concurrency.rest;

import com.betfair.cougar.testing.concurrency.RestSustainedPostRequestsJETTTest;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Ensure that when sustained Rest XML post requests are performed against Cougar, each request is successfully sent and the JSON response to each is correctly handled
 */
public class RestPostSustainedRequestsAssertedXMLJSONTest {
    @Test
    public void doTest() throws Exception {
        CougarManager cougarManager1 = CougarManager.getInstance();
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp2 = new Timestamp(System.currentTimeMillis());
        // Execute the test, making the given number of XML calls
        RestSustainedPostRequestsJETTTest.RestSustainedPostRequestsTestResultBean executeTest3 = new RestSustainedPostRequestsJETTTest().executeTest(4000, CougarMessageProtocolRequestTypeEnum.RESTXML, CougarMessageContentTypeEnum.JSON);
        // Get the expected responses to the requests made
        Map<String, HttpResponseBean> getExpectedResponses4 = executeTest3.getExpectedResponses();
        // Check the actual responses against the expected ones (with a date tolerance of 2000ms)
        Long oldTolerance = AssertionUtils.setDateTolerance(2000L);
        try {
            AssertionUtils.multiAssertEquals(getExpectedResponses4, executeTest3.getActualResponses());
        }
        finally {
            AssertionUtils.setDateTolerance(oldTolerance);
        }
    }

}
