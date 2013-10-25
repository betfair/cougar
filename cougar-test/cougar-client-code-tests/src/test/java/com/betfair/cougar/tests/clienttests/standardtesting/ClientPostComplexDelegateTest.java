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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_ComplexDelegate.xls;
package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.BodyParamComplexMapObject;
import com.betfair.baseline.v2.to.ComplexMapOperationResponseObject;
import com.betfair.baseline.v2.to.SomeComplexObject;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that a request map can be populated using a delegate when making a request to cougar via a cougar client
 */
public class ClientPostComplexDelegateTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Create a some complex object
        SomeComplexObject someComplexObject1 = new SomeComplexObject();
        someComplexObject1.setStringParameter("delegate1");
        SomeComplexObject someComplex1 = someComplexObject1;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Date dateParam1 = cougarClientResponseTypeUtils2.createDateFromString("1970-01-01T00:01:52.233+0100");
        
        someComplex1.setDateTimeParameter(dateParam1);
        
        someComplex1.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils3 = new CougarClientResponseTypeUtils();
        List<String> list1 = cougarClientResponseTypeUtils3.buildList("item1,item2");
        
        someComplex1.setListParameter(list1);
        // Create another some complex object
        SomeComplexObject someComplexObject4 = new SomeComplexObject();
        someComplexObject4.setStringParameter("delegate2");
        SomeComplexObject someComplex2 = someComplexObject4;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils5 = new CougarClientResponseTypeUtils();
        Date dateParam2 = cougarClientResponseTypeUtils5.createDateFromString("1970-01-01T00:01:52.233+0100");
        
        someComplex2.setDateTimeParameter(dateParam2);
        
        someComplex2.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils6 = new CougarClientResponseTypeUtils();
        List<String> list2 = cougarClientResponseTypeUtils6.buildList("item1,item2");
        
        someComplex2.setListParameter(list2);
        // Create another some complex object
        SomeComplexObject someComplexObject7 = new SomeComplexObject();
        someComplexObject7.setStringParameter("delegate3");
        SomeComplexObject someComplex3 = someComplexObject7;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils8 = new CougarClientResponseTypeUtils();
        Date dateParam3 = cougarClientResponseTypeUtils8.createDateFromString("1970-01-01T00:01:52.233+0100");
        
        someComplex3.setDateTimeParameter(dateParam3);
        
        someComplex3.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils9 = new CougarClientResponseTypeUtils();
        List<String> list3 = cougarClientResponseTypeUtils9.buildList("item1,item2");
        
        someComplex3.setListParameter(list3);
        // Put created objects in a map
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils10 = new CougarClientResponseTypeUtils();
        Map<String, SomeComplexObject> expectedReturnMap = cougarClientResponseTypeUtils10.buildComplexDelegateReturnMap(Arrays.asList(someComplex1, someComplex2, someComplex3));
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper11 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper11;
        BaselineSyncClient client = cougarClientWrapper11.getClient();
        ExecutionContext context = cougarClientWrapper11.getCtx();
        // Build a complex delegate map object to be included in the body parameter
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils12 = new CougarClientResponseTypeUtils();
        Map<String, SomeComplexObject> map = cougarClientResponseTypeUtils12.buildComplexDelegateMap();
        // Create body parameter to be passed
        BodyParamComplexMapObject bodyParamComplexMapObject13 = new BodyParamComplexMapObject();
        bodyParamComplexMapObject13.setComplexMap(map);
        BodyParamComplexMapObject bodyParam = bodyParamComplexMapObject13;
        // Make call to the method via client and store the response
        ComplexMapOperationResponseObject response15 = client.complexMapOperation(context, bodyParam);
        Map responseMap = response15.getResponseMap();
        // Validate that the returned map matches the expected map created in the import sheet
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils16 = new CougarClientResponseTypeUtils();
        boolean mapsMatch = cougarClientResponseTypeUtils16.compareComplexDelegateMaps(responseMap, expectedReturnMap);
        assertEquals(true, mapsMatch);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
