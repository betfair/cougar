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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Get_ResponseTypes_ListsAndEnums.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.ComplexObject;
import com.betfair.baseline.v2.to.LargeRequest;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that a request that returns a response containing both lists and enums can be performed against cougar via a cougar client and the response is handled correctly
 */
public class ClientGetResponseTypesListsAndEnumsTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        BaselineSyncClient client = cougarClientWrapper1.getClient();
        ExecutionContext context = cougarClientWrapper1.getCtx();
        
        LargeRequest largeRequest = client.testLargeGet(context, (int) 5);
        assertEquals(5, (int) largeRequest.getSize());
        assertEquals(com.betfair.baseline.v2.enumerations.LargeRequestOddOrEvenEnum.ODD, largeRequest.getOddOrEven());
        // Examine the list of the response to check it is correct
        List<ComplexObject> list3 = largeRequest.getObjects();
        assertEquals("name 0", list3.get(0).getName());
        assertEquals(0, (int) list3.get(0).getValue1());
        assertEquals(1, (int) list3.get(0).getValue2());
        assertEquals("name 1", list3.get(1).getName());
        assertEquals(1, (int) list3.get(1).getValue1());
        assertEquals(2, (int) list3.get(1).getValue2());
        assertEquals("name 2", list3.get(2).getName());
        assertEquals(2, (int) list3.get(2).getValue1());
        assertEquals(3, (int) list3.get(2).getValue2());
        assertEquals("name 3", list3.get(3).getName());
        assertEquals(3, (int) list3.get(3).getValue1());
        assertEquals(4, (int) list3.get(3).getValue2());
        assertEquals("name 4", list3.get(4).getName());
        assertEquals(4, (int) list3.get(4).getValue1());
        assertEquals(5, (int) list3.get(4).getValue2());
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
