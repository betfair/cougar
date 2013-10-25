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

// Originally from UpdatedComponentTests/Concurrency/RPC/RPC_ConcurrentBatchedRequests_RapidLoad.xls;
package com.betfair.cougar.tests.updatedcomponenttests.concurrency.rpc;

import com.betfair.cougar.testing.concurrency.RPCConcurrentBatchedRequests;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Ensure that when concurrent Batched JSON requests are performed against Cougar with a rapid load of a large number of threads, each batch of requests is successfully sent and the JSON response to each is correctly handled
 */
public class RPCConcurrentBatchedRequestsRapidLoadTest {
    @Test
    public void doTest() throws Exception {
        CougarManager cougarManager1 = CougarManager.getInstance();
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp2 = new Timestamp(System.currentTimeMillis());
        // Execute the test, creating a large number of threads and making the given number of batched JSON calls per thread
        RPCConcurrentBatchedRequests.RPCConcurrentBatchedRequestsResultBean executeTest3 = new RPCConcurrentBatchedRequests().executeTest(100, 40);
        // Get the expected responses to the requests made
        Map<String, Map<String, Object>> getExpectedResponses4 = executeTest3.getExpectedResponses();
        // Check the actual responses against the expected ones (with a date tolerance of 10000ms)
        Long oldTolerance = AssertionUtils.setDateTolerance(10000L);
        try {
            AssertionUtils.multiAssertEquals(getExpectedResponses4, executeTest3.getActualResponses());
        }
        finally {
            AssertionUtils.setDateTolerance(oldTolerance);
        }
    }

}
