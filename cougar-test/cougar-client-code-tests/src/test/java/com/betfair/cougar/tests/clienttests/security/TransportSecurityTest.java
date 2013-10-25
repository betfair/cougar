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

package com.betfair.cougar.tests.clienttests.security;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.CallSecurity;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Ensure that when the auth credentials are passed to the secure operation the request is sent and the response is handled correctly, returning the correct Identity Chain
 */
public class TransportSecurityTest {


    @Test(dataProvider = "ClientName")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up and call the method using requested transport
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);

        BaselineSyncClient client = wrapper.getClient();
        ExecutionContext context = wrapper.getCtx();

        CallSecurity security = client.checkSecurity(context);

        if (tt.isSecure()) {
            assertTrue("SSF should be greater than one", security.getSecurityStrengthFactor() > 1);
        }
        else {
            assertTrue("SSF should be less than or equal to one", security.getSecurityStrengthFactor() <= 1);
        }

        if (tt.isClientAuth()) {
            assertNotNull("Client CN shouldn't be null", security.getClientSubject());
        }
        else {
            assertNull("Client CN should be null", security.getClientSubject());
        }
    }

    @DataProvider(name = "ClientName")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
