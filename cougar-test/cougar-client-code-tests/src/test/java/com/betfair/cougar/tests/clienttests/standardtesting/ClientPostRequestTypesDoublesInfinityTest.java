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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_Doubles_Infinity.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamDoubleObject;
import com.betfair.baseline.v2.to.DoubleOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a Double object (with value Infinity) is passed in parameters to cougar via a cougar client, the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesDoublesInfinityTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Create body parameter to be passed
        BodyParamDoubleObject bodyParamDoubleObject2 = new BodyParamDoubleObject();
        bodyParamDoubleObject2.setBodyParameter(Double.NEGATIVE_INFINITY);
        BodyParamDoubleObject bodyParam = bodyParamDoubleObject2;
        // Make call to the method (passing Infinity) via client and validate response is as expected
        DoubleOperationResponseObject response3 = client.doubleOperation(context, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, bodyParam);
        assertEquals(Double.POSITIVE_INFINITY, response3.getQueryParameter());
        assertEquals(Double.NEGATIVE_INFINITY, response3.getHeaderParameter());
        assertEquals(Double.NEGATIVE_INFINITY, response3.getBodyParameter());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
