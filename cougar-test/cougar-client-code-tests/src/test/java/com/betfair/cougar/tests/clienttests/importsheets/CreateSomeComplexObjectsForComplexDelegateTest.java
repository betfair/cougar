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

// Originally from ClientTests/Transport/ImportSheets/CreateSomeComplexObjectsForComplexDelegate.xls;
package com.betfair.cougar.tests.clienttests.importsheets;

import com.betfair.baseline.v2.to.SomeComplexObject;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Import Sheet to construct the expected returned some complex objects from ComplexMapOperation delegate
 */
public class CreateSomeComplexObjectsForComplexDelegateTest {
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
        // Create a some complex object
        SomeComplexObject someComplexObject11 = new SomeComplexObject();
        someComplexObject11.setStringParameter("delegate1");
        someComplex1 = someComplexObject11;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils12 = new CougarClientResponseTypeUtils();
        dateParam1 = cougarClientResponseTypeUtils12.createDateFromString("1970-01-01T00:01:52.233+0100");
        
        someComplex1.setDateTimeParameter(dateParam1);
        
        someComplex1.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils13 = new CougarClientResponseTypeUtils();
        list1 = cougarClientResponseTypeUtils13.buildList("item1,item2");
        
        someComplex1.setListParameter(list1);
        // Create another some complex object
        SomeComplexObject someComplexObject14 = new SomeComplexObject();
        someComplexObject14.setStringParameter("delegate2");
        someComplex2 = someComplexObject14;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils15 = new CougarClientResponseTypeUtils();
        dateParam2 = cougarClientResponseTypeUtils15.createDateFromString("1970-01-01T00:01:52.233+0100");
        
        someComplex2.setDateTimeParameter(dateParam2);
        
        someComplex2.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils16 = new CougarClientResponseTypeUtils();
        list2 = cougarClientResponseTypeUtils16.buildList("item1,item2");
        
        someComplex2.setListParameter(list2);
        // Create another some complex object
        SomeComplexObject someComplexObject17 = new SomeComplexObject();
        someComplexObject17.setStringParameter("delegate3");
        someComplex3 = someComplexObject17;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils18 = new CougarClientResponseTypeUtils();
        dateParam3 = cougarClientResponseTypeUtils18.createDateFromString("1970-01-01T00:01:52.233+0100");
        
        someComplex3.setDateTimeParameter(dateParam3);
        
        someComplex3.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils19 = new CougarClientResponseTypeUtils();
        list3 = cougarClientResponseTypeUtils19.buildList("item1,item2");
        
        someComplex3.setListParameter(list3);
        // Put created objects in a map
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils20 = new CougarClientResponseTypeUtils();
        expectedReturnMap = cougarClientResponseTypeUtils20.buildComplexDelegateReturnMap(Arrays.asList(someComplex1, someComplex2, someComplex3));
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
