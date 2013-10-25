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

// Originally from ClientTests/Transport/ImportSheets/CreateLargeRequest.xls;
package com.betfair.cougar.tests.clienttests.importsheets;

import com.betfair.baseline.v2.to.ComplexObject;
import com.betfair.baseline.v2.to.LargeRequest;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;


/**
 * Import Sheet to construct a large request object
 */
public class CreateLargeRequestTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Create a complex object
        ComplexObject complexObject1 = new ComplexObject();
        complexObject1.setName("ssasdf");
        ComplexObject complex1 = complexObject1;
        
        complex1.setValue1((int) 23);
        
        complex1.setValue2((int) 42);
        // Create another complex object
        ComplexObject complexObject2 = new ComplexObject();
        complexObject2.setName("ssasff");
        ComplexObject complex2 = complexObject2;
        
        complex2.setValue1((int) 26);
        
        complex2.setValue2((int) 45);
        // Create a large request object
        LargeRequest largeRequest3 = new LargeRequest();
        largeRequest3.setSize((int) 1);
        LargeRequest largeRequest = largeRequest3;
        
        largeRequest.setOddOrEven(com.betfair.baseline.v2.enumerations.LargeRequestOddOrEvenEnum.ODD);
        
        largeRequest.setObjects(Arrays.asList(complex1, complex2));
        // Create a complex object
        ComplexObject complexObject4 = new ComplexObject();
        complexObject4.setName("ssasdf");
        complex1 = complexObject4;
        
        complex1.setValue1((int) 23);
        
        complex1.setValue2((int) 42);
        // Create another complex object
        ComplexObject complexObject5 = new ComplexObject();
        complexObject5.setName("ssasff");
        complex2 = complexObject5;
        
        complex2.setValue1((int) 26);
        
        complex2.setValue2((int) 45);
        // Create a large request object
        LargeRequest largeRequest6 = new LargeRequest();
        largeRequest6.setSize((int) 1);
        largeRequest = largeRequest6;
        
        largeRequest.setOddOrEven(com.betfair.baseline.v2.enumerations.LargeRequestOddOrEvenEnum.ODD);
        
        largeRequest.setObjects(Arrays.asList(complex1, complex2));
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
