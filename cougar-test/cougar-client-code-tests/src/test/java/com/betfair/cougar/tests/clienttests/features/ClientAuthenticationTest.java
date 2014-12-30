/*
 * Copyright 2013, Simon Matić Langford
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

package com.betfair.cougar.tests.clienttests.features;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.to.IdentChain;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientResponseTypeUtils;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Ensure that when the auth credentials are passed to the secure operation the request is sent and the response is handled correctly, returning the correct Identity Chain
 */
public class ClientAuthenticationTest {


    @Test(dataProvider = "ClientName")
    public void doTest(CougarClientWrapper.TransportType tt) throws Exception {
        // Set up and call the method using requested transport
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);

        BaselineSyncClient client = wrapper.getClient();
        ExecutionContext context = wrapper.getCtx();

        CougarClientResponseTypeUtils cougarClientResponseTypeUtils2 = new CougarClientResponseTypeUtils();
        Map<String, String> idMap = cougarClientResponseTypeUtils2.buildMap("Username,Password", "foo,bar");

        wrapper.setCtxIdentity(idMap);

        IdentChain idChain = client.testIdentityChain(context);

        String response = idChain.toString();

        if (tt.isClientAuth()) {
            assertEquals("{identities=[{principal=PRINCIPAL: X-SSL-Cert-Info,credentialName=CREDENTIAL: X-SSL-Cert-Info,credentialValue=localhost,}, {principal=PRINCIPAL: Username,credentialName=CREDENTIAL: Username,credentialValue=foo,}, {principal=PRINCIPAL: Password,credentialName=CREDENTIAL: Password,credentialValue=bar,}],}", response);
        }
        else {
            assertEquals("{identities=[{principal=PRINCIPAL: Username,credentialName=CREDENTIAL: Username,credentialValue=foo,}, {principal=PRINCIPAL: Password,credentialName=CREDENTIAL: Password,credentialValue=bar,}],}", response);
        }
    }

    @DataProvider(name = "ClientName")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
