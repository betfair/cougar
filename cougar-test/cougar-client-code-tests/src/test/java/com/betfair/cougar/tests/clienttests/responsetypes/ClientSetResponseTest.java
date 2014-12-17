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

// Originally from ClientTests/Transport/ResponseTypes/Client_Rescript_SetResponse.xls;
package com.betfair.cougar.tests.clienttests.responsetypes;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure an operation performed against cougar via a cougar client can have a set as a response object
 */
public class ClientSetResponseTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up client
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Build the set object that is the expected repsonse
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Set<String> requestSet = cougarClientResponseTypeUtils2.buildSet("aaa,ccc,bbb,ddd");
        // Call method using recript transport
        Set<String> responseSet = client.testSimpleSetGet(context, requestSet);
        // Check received set response is as expected
        assertEquals(requestSet, responseSet);
    }

    @DataProvider(name = "TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
