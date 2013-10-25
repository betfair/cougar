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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Get_RequestTypes_String_HeaderParam_EscapedCharacters_Hash.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.NonMandatoryParamsOperationResponseObject;
import com.betfair.baseline.v2.to.NonMandatoryParamsRequest;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when escaped characters (#) are passed in a header parameter to cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientGetRequestTypesStringHeaderParamEscapedCharactersHashTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Create body parameter to be passed
        NonMandatoryParamsRequest nonMandatoryParamsRequest2 = new NonMandatoryParamsRequest();
        nonMandatoryParamsRequest2.setBodyParameter1("bodyParam");
        NonMandatoryParamsRequest body = nonMandatoryParamsRequest2;
        // Make call to the method via client and validate the response is as expected
        NonMandatoryParamsOperationResponseObject response3 = client.nonMandatoryParamsOperation(context, "hash#", "queryParamString", body);
        assertEquals("queryParamString", response3.getQueryParameter());
        assertEquals("hash#", response3.getHeaderParameter());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
