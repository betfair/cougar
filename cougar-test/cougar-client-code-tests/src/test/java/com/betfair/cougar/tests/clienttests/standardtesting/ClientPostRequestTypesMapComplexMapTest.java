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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_RequestTypes_Map_ComplexMap.xls;
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
 * Ensure that when a ComplexMap is passed in parameters to cougar via a cougar client, the request is sent and the response is handled correctly
 */
public class ClientPostRequestTypesMapComplexMapTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Create a some complex object
        SomeComplexObject someComplexObject1 = new SomeComplexObject();
        someComplexObject1.setStringParameter("String value for aaa");
        SomeComplexObject someComplex1 = someComplexObject1;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Date dateParam1 = cougarClientResponseTypeUtils2.createDateFromString("2009-06-01T13:50:00.0Z");
        
        someComplex1.setDateTimeParameter(dateParam1);
        
        someComplex1.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils3 = new CougarClientResponseTypeUtils();
        List<String> list1 = cougarClientResponseTypeUtils3.buildList("aaa List Entry 1,aaa List Entry 2,aaa List Entry 3");
        
        someComplex1.setListParameter(list1);
        // Create another some complex object
        SomeComplexObject someComplexObject4 = new SomeComplexObject();
        someComplexObject4.setStringParameter("String value for bbb");
        SomeComplexObject someComplex2 = someComplexObject4;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils5 = new CougarClientResponseTypeUtils();
        Date dateParam2 = cougarClientResponseTypeUtils5.createDateFromString("2009-06-02T13:50:00.435Z");
        
        someComplex2.setDateTimeParameter(dateParam2);
        
        someComplex2.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.FOOBAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils6 = new CougarClientResponseTypeUtils();
        List<String> list2 = cougarClientResponseTypeUtils6.buildList("bbb List Entry 1,bbb List Entry 2,bbb List Entry 3");
        
        someComplex2.setListParameter(list2);
        // Create another some complex object
        SomeComplexObject someComplexObject7 = new SomeComplexObject();
        someComplexObject7.setStringParameter("String value for ccc");
        SomeComplexObject someComplex3 = someComplexObject7;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils8 = new CougarClientResponseTypeUtils();
        Date dateParam3 = cougarClientResponseTypeUtils8.createDateFromString("2009-06-03T13:50:00.435Z");
        
        someComplex3.setDateTimeParameter(dateParam3);
        
        someComplex3.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.FOO);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils9 = new CougarClientResponseTypeUtils();
        List<String> list3 = cougarClientResponseTypeUtils9.buildList("ccc List Entry 1,ccc List Entry 2,ccc List Entry 3");
        
        someComplex3.setListParameter(list3);
        // Set up the client to use rescript transport
        CougarClientWrapper cougarClientWrapper10 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper10;
        BaselineSyncClient client = cougarClientWrapper10.getClient();
        ExecutionContext context = cougarClientWrapper10.getCtx();
        // Build complex map using objects created in import sheet
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils11 = new CougarClientResponseTypeUtils();
        Map<String, SomeComplexObject> complexMap = cougarClientResponseTypeUtils11.buildComplexMap(Arrays.asList(someComplex1, someComplex2, someComplex3));
        // Create body parameter to be passed
        BodyParamComplexMapObject bodyParamComplexMapObject12 = new BodyParamComplexMapObject();
        bodyParamComplexMapObject12.setComplexMap(complexMap);
        BodyParamComplexMapObject bodyParam = bodyParamComplexMapObject12;
        // Make call to the method via client and store response
        ComplexMapOperationResponseObject response14 = client.complexMapOperation(context, bodyParam);
        Map responseMap = response14.getResponseMap();
        // Validate the received complex map matches the one passed in
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils15 = new CougarClientResponseTypeUtils();
        boolean mapsMatch = cougarClientResponseTypeUtils15.compareComplexMaps(complexMap, responseMap);
        assertEquals(true, mapsMatch);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
