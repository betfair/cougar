/*
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

package com.betfair.cougar.core.api.exception;

import com.betfair.cougar.api.security.CredentialFaultCode;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServerFaultCodeTest {

    /**
     * Test that all CredentialFaultCodes map to a ServerFaultCode
     */
    @Test
    public void testCredentialFaultCodeToServerFaultCodeMapping() {
        CredentialFaultCode[] allCredentialFaultCodes = CredentialFaultCode.values();
        Set<ServerFaultCode> mappedServerFaultCodes = new HashSet<ServerFaultCode>();

        for (CredentialFaultCode cfc : allCredentialFaultCodes) {
            ServerFaultCode sfc = ServerFaultCode.getByCredentialFaultCode(cfc);
            assertNotNull(sfc); // Assert the CredentialFaultCode was successfully mapped to a ServerFaultCode
            mappedServerFaultCodes.add(sfc);
        }

        assertEquals(allCredentialFaultCodes.length, mappedServerFaultCodes.size()); // Assert there was a 1-1 mapping of SFC-CFC
    }
}
