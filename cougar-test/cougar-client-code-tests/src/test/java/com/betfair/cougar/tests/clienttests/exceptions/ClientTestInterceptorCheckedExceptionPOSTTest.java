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

// Originally from ClientTests/Transport/Exceptions/Client_Rescript_TestInterceptorCheckedException_POST.xls;
package com.betfair.cougar.tests.clienttests.exceptions;

import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when an exception is thrown from a post operation interceptor it is thrown and handled correctly by the cougar client
 */
public class ClientTestInterceptorCheckedExceptionPOSTTest {
    @Test(dataProvider = "TransportType")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client using rescript transport
        CougarClientWrapper cougarClientWrapper1 = CougarClientWrapper.getInstance(tt);
        CougarClientWrapper wrapper = cougarClientWrapper1;
        // Call a wrapper method that will call an operation that returns a post interceptor exception, catches it and returns the exception message
        String errorMessage = wrapper.callInterceptorExceptionOperation(com.betfair.baseline.v2.enumerations.PreOrPostInterceptorException.POST);
        assertEquals("An anticipated post-execution BSIDL defined checked exception", errorMessage);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
