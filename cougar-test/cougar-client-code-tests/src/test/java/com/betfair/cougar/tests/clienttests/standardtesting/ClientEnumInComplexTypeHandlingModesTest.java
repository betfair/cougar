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
import com.betfair.baseline.v2.enumerations.ClientServerEnum;
import com.betfair.baseline.v2.enumerations.EnumHandlingParam2Enum;
import com.betfair.baseline.v2.to.EnumHandling;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 *
 */
public class ClientEnumInComplexTypeHandlingModesTest {

    @Test(dataProvider = "TransportType")
    public void clientSoftModeRecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, false);


        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientServer);
        req.setParam2(EnumHandlingParam2Enum.ClientServer);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumHandling response = client.enumHandling(cougarClientWrapper.getCtx(), req, false);

        assertEquals(ClientServerEnum.ClientServer, response.getParam1());
        assertEquals("ClientServer", response.getRawParam1Value());
        assertEquals(EnumHandlingParam2Enum.ClientServer, response.getParam2());
        assertEquals("ClientServer", response.getRawParam2Value());
    }

    @Test(dataProvider = "TransportType")
    public void clientSoftModeUnrecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, false);

        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientServer);
        req.setParam2(EnumHandlingParam2Enum.ClientServer);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumHandling response = client.enumHandling(cougarClientWrapper.getCtx(), req, true);

        assertEquals(ClientServerEnum.UNRECOGNIZED_VALUE, response.getParam1());
        assertEquals("ServerOnly", response.getRawParam1Value());
        assertEquals(EnumHandlingParam2Enum.UNRECOGNIZED_VALUE, response.getParam2());
        assertEquals("ServerOnly", response.getRawParam2Value());

    }

    @Test(dataProvider = "TransportType")
    public void clientHardModeRecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, true);

        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientServer);
        req.setParam2(EnumHandlingParam2Enum.ClientServer);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumHandling response = client.enumHandling(cougarClientWrapper.getCtx(), req, false);

        assertEquals(ClientServerEnum.ClientServer, response.getParam1());
        assertEquals("ClientServer", response.getRawParam1Value());
        assertEquals(EnumHandlingParam2Enum.ClientServer, response.getParam2());
        assertEquals("ClientServer", response.getRawParam2Value());
    }

    @Test(dataProvider = "TransportType")
    public void clientHardModeUnrecognizedServerValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt, true);


        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientServer);
        req.setParam2(EnumHandlingParam2Enum.ClientServer);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        try {
            client.enumHandling(cougarClientWrapper.getCtx(), req, true);
            fail("Expected an exception here");
        }
        catch (CougarClientException cfe) {
            assertEquals(toString(cfe), ServerFaultCode.ClientDeserialisationFailure, cfe.getServerFaultCode());
        }
    }

    @Test(dataProvider = "TransportType")
    public void serverHardModeRecognizedClientValue(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt);


        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientServer);
        req.setParam2(EnumHandlingParam2Enum.ClientServer);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        EnumHandling response = client.enumHandling(cougarClientWrapper.getCtx(), req, false);

        assertEquals(ClientServerEnum.ClientServer, response.getParam1());
        assertEquals("ClientServer", response.getRawParam1Value());
        assertEquals(EnumHandlingParam2Enum.ClientServer, response.getParam2());
        assertEquals("ClientServer", response.getRawParam2Value());
    }

    @Test(dataProvider = "TransportType")
    public void serverHardModeUnrecognizedClientValue1(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt);

        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientOnly);
        req.setParam2(EnumHandlingParam2Enum.ClientServer);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        try {
            client.enumHandling(cougarClientWrapper.getCtx(), req, false);
        }
        catch (CougarClientException cfe) {
            assertEquals(toString(cfe), ServerFaultCode.ServerDeserialisationFailure, cfe.getServerFaultCode());
        }
    }

    @Test(dataProvider = "TransportType")
    public void serverHardModeUnrecognizedClientValue2(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper cougarClientWrapper = CougarClientWrapper.getInstance(tt);

        EnumHandling req = new EnumHandling();
        req.setParam1(ClientServerEnum.ClientServer);
        req.setParam2(EnumHandlingParam2Enum.ClientOnly);

        BaselineSyncClient client = cougarClientWrapper.getClient();
        try {
            client.enumHandling(cougarClientWrapper.getCtx(), req, false);
        }
        catch (CougarClientException cfe) {
            assertEquals(toString(cfe), ServerFaultCode.ServerDeserialisationFailure, cfe.getServerFaultCode());
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
