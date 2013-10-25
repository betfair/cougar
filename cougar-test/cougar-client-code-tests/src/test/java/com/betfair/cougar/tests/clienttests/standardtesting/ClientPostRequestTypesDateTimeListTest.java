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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_DateTimeList.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamDateTimeListObject;
import com.betfair.baseline.v2.to.DateTimeListOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a dateTimeList is passed in a body parameter to cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesDateTimeListTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Create date to be put in list
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Date dateParam1 = cougarClientResponseTypeUtils2.createDateFromString("2009-06-01T13:50:00.0Z");
        CougarHelpers helper = new CougarHelpers();
        Date convertedDate1 = helper.convertToSystemTimeZone("2009-06-01T13:50:00.0Z");

        // Create date to be put in list
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils3 = new CougarClientResponseTypeUtils();
        Date dateParam2 = cougarClientResponseTypeUtils3.createDateFromString("2009-06-01T14:50:00.0Z");
         Date convertedDate2 = helper.convertToSystemTimeZone("2009-06-01T14:50:00.0Z");

        // Create date list object to pass as parameter (using previously created dates)
        BodyParamDateTimeListObject bodyParamDateTimeListObject4 = new BodyParamDateTimeListObject();
        bodyParamDateTimeListObject4.setDateTimeList(Arrays.asList(convertedDate1, convertedDate2));
        BodyParamDateTimeListObject bodyParam = bodyParamDateTimeListObject4;
        // Make call to the method via client and store the result
        DateTimeListOperationResponseObject response6 = client.dateTimeListOperation(context, bodyParam);
        Date localTime = response6.getResponseList().get(0);
        Date localTime2 = response6.getResponseList().get(1);
        // Validate the response is as expected
        assertEquals(cougarClientResponseTypeUtils2.formatDateToString(localTime),cougarClientResponseTypeUtils2.formatDateToString(localTime));
        assertEquals(cougarClientResponseTypeUtils2.formatDateToString(localTime2) ,cougarClientResponseTypeUtils2.formatDateToString(localTime2));
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
