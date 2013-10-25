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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_DateTimeMap.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamDateTimeMapObject;
import com.betfair.baseline.v2.to.DateTimeMapOperationResponseObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when a dateTimeMap is passed in a body parameter to cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesDateTimeMapTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        // Create date to be put in map
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Date dateParam1 = cougarClientResponseTypeUtils2.createDateFromString("2009-06-01T13:50:00.0Z");
        CougarClientResponseTypeUtils utils = cougarClientResponseTypeUtils2;
        // Create date to be put in map
        Date dateParam2 = utils.createDateFromString("2009-06-01T14:50:00.0Z");
        // Create map of previously created dates
        Map<String, Date> dateMap = utils.createMapOfDates(Arrays.asList(dateParam1, dateParam2));
        // Create date map object to pass as parameter (using previously created map of dates)
        BodyParamDateTimeMapObject bodyParamDateTimeMapObject3 = new BodyParamDateTimeMapObject();
        bodyParamDateTimeMapObject3.setDateTimeMap(dateMap);
        BodyParamDateTimeMapObject bodyParam = bodyParamDateTimeMapObject3;
        // Make call to the method via client and store the result
        DateTimeMapOperationResponseObject response5 = client.dateTimeMapOperation(context, bodyParam);
        Map responseMap = response5.getResponseMap();
        // Convert the map to a string for comparison
        String dates = utils.formatMapOfDatesToString(responseMap);
        assertEquals("{(date2, 2009-06-01T 14:50:00.000Z), (date1, 2009-06-01T 13:50:00.000Z)}", dates);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
