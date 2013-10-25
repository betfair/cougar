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

package com.betfair.cougar.tests.clienttests.standardtesting;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.enumerations.EnumHandling3BodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumHandling3WrappedValueEnum;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 *
 */
public class ClientEnumAsLocalTypeHandlingModesTest {

    @Test(dataProvider = "TransportType")
    public void clientSoftModeRecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, false);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumWrapper<EnumHandling3WrappedValueEnum> response = client.enumHandling3(cougarClientWrapper.getCtx(), EnumHandling3BodyParameterEnum.ClientServer, false);

        assertEquals(EnumHandling3WrappedValueEnum.ClientServer, response.getValue());
        assertEquals("ClientServer", response.getRawValue());
    }

    @Test(dataProvider = "TransportType")
    public void clientSoftModeUnrecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, false);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumWrapper<EnumHandling3WrappedValueEnum> response = client.enumHandling3(cougarClientWrapper.getCtx(), EnumHandling3BodyParameterEnum.ClientServer, true);

        assertEquals(EnumHandling3WrappedValueEnum.UNRECOGNIZED_VALUE, response.getValue());
        assertEquals("ServerOnly", response.getRawValue());

    }

    @Test(dataProvider = "TransportType")
    public void clientHardModeRecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, true);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumWrapper<EnumHandling3WrappedValueEnum> response = client.enumHandling3(cougarClientWrapper.getCtx(), EnumHandling3BodyParameterEnum.ClientServer, false);

        assertEquals(EnumHandling3WrappedValueEnum.ClientServer, response.getValue());
        assertEquals("ClientServer", response.getRawValue());
    }

    @Test(dataProvider = "TransportType")
    public void clientHardModeUnrecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, true);


        BaselineSyncClient client = cougarClientWrapper.getClient();
        try {
            client.enumHandling3(cougarClientWrapper.getCtx(), EnumHandling3BodyParameterEnum.ClientServer, true);
            fail("Expected an exception here");
        }
        catch (CougarServiceException cfe) {
            ServerFaultCode expected;
            switch (tt.getUnderlyingTransport()) {
                case HTTP:
                    expected = ServerFaultCode.JSONDeserialisationParseFailure;
                    break;
                case Socket:
                    expected = ServerFaultCode.BinDeserialisationParseFailure;
                    break;
                default:
                    throw new IllegalStateException("Unrecognised transport "+tt.getUnderlyingTransport());
            }
            assertEquals(toString(cfe), expected, cfe.getServerFaultCode());
        }
    }

    @Test(dataProvider = "TransportType")
    public void serverHardModeRecognizedClientValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt);


        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumWrapper<EnumHandling3WrappedValueEnum> response = client.enumHandling3(cougarClientWrapper.getCtx(), EnumHandling3BodyParameterEnum.ClientServer, false);

        assertEquals(EnumHandling3WrappedValueEnum.ClientServer, response.getValue());
        assertEquals("ClientServer", response.getRawValue());
    }

    @Test(dataProvider = "TransportType")
    public void serverHardModeUnrecognizedClientValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        try {
            client.enumHandling3(cougarClientWrapper.getCtx(), EnumHandling3BodyParameterEnum.ClientOnly, false);
        }
        catch (CougarServiceException cfe) {
            ServerFaultCode expected;
            switch (tt.getUnderlyingTransport()) {
                case HTTP:
                    expected = ServerFaultCode.JSONDeserialisationParseFailure;
                    break;
                case Socket:
                    expected = ServerFaultCode.BinDeserialisationParseFailure;
                    break;
                default:
                    throw new IllegalStateException("Unrecognised transport "+tt.getUnderlyingTransport());
            }
            assertEquals(toString(cfe), expected, cfe.getServerFaultCode());
        }
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }
    
    private String toString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
