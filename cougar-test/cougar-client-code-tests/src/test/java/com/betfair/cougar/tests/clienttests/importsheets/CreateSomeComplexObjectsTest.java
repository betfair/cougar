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

// Originally from ClientTests/Transport/ImportSheets/CreateSomeComplexObjects.xls;
package com.betfair.cougar.tests.clienttests.importsheets;

import com.betfair.baseline.v2.to.SomeComplexObject;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;


/**
 * Import Sheet to construct some complex objects
 */
public class CreateSomeComplexObjectsTest {
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
        // Create a some complex object
        SomeComplexObject someComplexObject10 = new SomeComplexObject();
        someComplexObject10.setStringParameter("String value for aaa");
        someComplex1 = someComplexObject10;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils11 = new CougarClientResponseTypeUtils();
        dateParam1 = cougarClientResponseTypeUtils11.createDateFromString("2009-06-01T13:50:00.0Z");
        
        someComplex1.setDateTimeParameter(dateParam1);
        
        someComplex1.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.BAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils12 = new CougarClientResponseTypeUtils();
        list1 = cougarClientResponseTypeUtils12.buildList("aaa List Entry 1,aaa List Entry 2,aaa List Entry 3");
        
        someComplex1.setListParameter(list1);
        // Create another some complex object
        SomeComplexObject someComplexObject13 = new SomeComplexObject();
        someComplexObject13.setStringParameter("String value for bbb");
        someComplex2 = someComplexObject13;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils14 = new CougarClientResponseTypeUtils();
        dateParam2 = cougarClientResponseTypeUtils14.createDateFromString("2009-06-02T13:50:00.435Z");
        
        someComplex2.setDateTimeParameter(dateParam2);
        
        someComplex2.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.FOOBAR);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils15 = new CougarClientResponseTypeUtils();
        list2 = cougarClientResponseTypeUtils15.buildList("bbb List Entry 1,bbb List Entry 2,bbb List Entry 3");
        
        someComplex2.setListParameter(list2);
        // Create another some complex object
        SomeComplexObject someComplexObject16 = new SomeComplexObject();
        someComplexObject16.setStringParameter("String value for ccc");
        someComplex3 = someComplexObject16;
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils17 = new CougarClientResponseTypeUtils();
        dateParam3 = cougarClientResponseTypeUtils17.createDateFromString("2009-06-03T13:50:00.435Z");
        
        someComplex3.setDateTimeParameter(dateParam3);
        
        someComplex3.setEnumParameter(com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum.FOO);
        
        CougarClientResponseTypeUtils cougarClientResponseTypeUtils18 = new CougarClientResponseTypeUtils();
        list3 = cougarClientResponseTypeUtils18.buildList("ccc List Entry 1,ccc List Entry 2,ccc List Entry 3");
        
        someComplex3.setListParameter(list3);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
