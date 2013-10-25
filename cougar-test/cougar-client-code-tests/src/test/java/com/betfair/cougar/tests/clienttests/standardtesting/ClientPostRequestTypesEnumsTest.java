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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_Enums.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamEnumObject;
import com.betfair.baseline.v2.to.EnumOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when an ENUM object is passed in parameters to cougar via a cougar client, the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesEnumsTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Create body parameter to be passed
        BodyParamEnumObject bodyParamEnumObject2 = new BodyParamEnumObject();
        bodyParamEnumObject2.setBodyParameter(com.betfair.baseline.v2.enumerations.BodyParamEnumObjectBodyParameterEnum.FooBody);
        BodyParamEnumObject bodyParam = bodyParamEnumObject2;
        // Make call to the method via client and validate response is as expected
        EnumOperationResponseObject response3 = client.enumOperation(context, com.betfair.baseline.v2.enumerations.EnumOperationHeaderParamEnum.FooHeader, com.betfair.baseline.v2.enumerations.EnumOperationQueryParamEnum.FooQuery, bodyParam);
        assertEquals(com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectQueryParameterEnum.FooQuery, response3.getQueryParameter());
        assertEquals(com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectHeaderParameterEnum.FooHeader, response3.getHeaderParameter());
        assertEquals(com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectBodyParameterEnum.FooBody, response3.getBodyParameter());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
