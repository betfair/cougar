/*
 * Copyright 2014, Simon Matić Langford
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.tests.clienttests.features;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import com.betfair.cougar.transport.api.protocol.http.ExecutionContextFactory;
import com.betfair.cougar.util.RequestUUIDImpl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.LinkedList;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Ensures that various request uuid features are working fine (root/sub uuids)
 */
public class ClientRequestUuidTest {


    @Test(dataProvider = "TransportType")
    public void nullRequestUUid(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        BaselineSyncClient client = wrapper.getClient();

        RequestUUID uuid = null;
        ExecutionContext context = ExecutionContextFactory.resolveExecutionContext(new LinkedList<IdentityToken>(), uuid, wrapper.getCtx().getLocation(), new Date(), false, 0, new Date(), true);

        String uuidResponse = client.echoRequestUuid(context);
        assertNotNull(uuidResponse);
        assertFalse(uuidResponse.contains(":"));
    }
    @Test(dataProvider = "TransportType")
    public void withSingleRequestUUid(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        BaselineSyncClient client = wrapper.getClient();

        RequestUUID uuid = new RequestUUIDImpl("localhost123-12345678-0000000001");
        ExecutionContext context = ExecutionContextFactory.resolveExecutionContext(new LinkedList<IdentityToken>(), uuid, wrapper.getCtx().getLocation(), new Date(), false, 0, new Date(), true);

        String uuidResponse = client.echoRequestUuid(context);
        assertTrue(uuidResponse.contains(":"));
        String[] components = uuidResponse.split(":");
        assertEquals("localhost123-12345678-0000000001", components[0]);
        assertEquals("localhost123-12345678-0000000001", components[1]);
        assertNotEquals("localhost123-12345678-0000000001", components[2]);
    }

    @Test(dataProvider = "TransportType")
    public void withCompoundRequestUUid(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up the client to use rescript transport
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        BaselineSyncClient client = wrapper.getClient();

        RequestUUID uuid = new RequestUUIDImpl("root123-12345678-0000000001:parent123-12345678-0000000001:child123-12345678-0000000001");
        ExecutionContext context = ExecutionContextFactory.resolveExecutionContext(new LinkedList<IdentityToken>(), uuid, wrapper.getCtx().getLocation(), new Date(), false, 0, new Date(), true);

        String uuidResponse = client.echoRequestUuid(context);
        assertTrue(uuidResponse.contains(":"));
        String[] components = uuidResponse.split(":");
        assertEquals("root123-12345678-0000000001", components[0]);
        assertEquals("child123-12345678-0000000001", components[1]);
        assertNotEquals("child123-12345678-0000000001", components[2]);
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }
}
