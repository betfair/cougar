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

// Originally from ClientTests/Transport/Concurrency/Client_Rescript_ConcurrentRequests.xls;
package com.betfair.cougar.tests.clienttests.concurrency;

import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientConcurrencyTestUtils;
import com.betfair.cougar.tests.clienttests.CougarClientConcurrencyTestUtils.ClientConcurrencyTestResultBean;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Ensure that when concurrent requests are made via a cougar client, the requests are handled correctly and the correct response is returned to each request
 */
public class ClientConcurrentRequestsTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up and make the concurrent calls to the cougar client
        CougarClientConcurrencyTestUtils cougarClientConcurrencyTestUtils1 = new CougarClientConcurrencyTestUtils();
        ClientConcurrencyTestResultBean responses = cougarClientConcurrencyTestUtils1.executeTest(tt);

        responses.assertOutcome(new ClientConcurrencyTestResultBean.AssertionWrapper() {
            @Override
            public void assertEquals(Object expected, Object actual) {
                AssertJUnit.assertEquals(expected != null ? expected.toString() : null, actual != null ? actual.toString() : null);
            }
        });
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
