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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_Set_SimpleSet_DuplicateEntry.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamSimpleSetObject;
import com.betfair.baseline.v2.to.SimpleSetOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a SimpleSet object with a duplicate entry is passed in parameters to cougar via a cougar client, the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesSetSimpleSetDuplicateEntryTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Build set with a duplicate entry
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Set<String> inputSet = cougarClientResponseTypeUtils2.buildSet("aaaString,bbbString,cccString,dddString,bbbString,aaaString,dddString");
        // Create body parameter to be passed
        BodyParamSimpleSetObject bodyParamSimpleSetObject3 = new BodyParamSimpleSetObject();
        bodyParamSimpleSetObject3.setSimpleSet(inputSet);
        BodyParamSimpleSetObject bodyParam = bodyParamSimpleSetObject3;
        // Make call to the method via client and store the response
        SimpleSetOperationResponseObject result = client.simpleSetOperation(context, bodyParam);
        // Validate the received set is as expected (duplicates removed)
        assertEquals(new LinkedHashSet(Arrays.asList("cccString", "bbbString", "aaaString", "dddString")), result.getResponseSet());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
