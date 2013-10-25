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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_DateTimeSet.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamDateTimeSetObject;
import com.betfair.baseline.v2.to.DateTimeSetOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a dateTimeSet is passed in a body parameter to cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesDateTimeSetTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Create date to be put in set
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Date dateParam1 = cougarClientResponseTypeUtils2.createDateFromString("2009-06-01T13:50:00.0Z");
        // Create date to be put in set
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils3 = new CougarClientResponseTypeUtils();
        Date dateParam2 = cougarClientResponseTypeUtils3.createDateFromString("2009-06-01T14:50:00.0Z");
        // Create set of previously created dates
        CougarHelpers helper = new CougarHelpers();
        Date convertedDate1 = helper.convertToSystemTimeZone("2009-06-01T13:50:00.0Z");
        Date convertedDate2 = helper.convertToSystemTimeZone("2009-06-01T14:50:00.0Z");
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils4 = new CougarClientResponseTypeUtils();
        Set<Date> dateSet = cougarClientResponseTypeUtils4.createSetOfDates(Arrays.asList(convertedDate1, convertedDate2));
        // Create date set object to pass as parameter (using previously created set of dates)
        BodyParamDateTimeSetObject bodyParamDateTimeSetObject5 = new BodyParamDateTimeSetObject();
        bodyParamDateTimeSetObject5.setDateTimeSet(dateSet);
        BodyParamDateTimeSetObject bodyParam = bodyParamDateTimeSetObject5;
        // Make call to the method via client and store the result
        DateTimeSetOperationResponseObject response6 = client.dateTimeSetOperation(context, bodyParam);
        Set returnSet = response6.getResponseSet();
        // Validate the response is as expected
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils7 = new CougarClientResponseTypeUtils();
        String dates = cougarClientResponseTypeUtils7.formatSetOfDatesToString(returnSet);

        assertEquals(dates, dates);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
