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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_QueryParam_NonMandatory_NotSet.xls;
package com.betfair.cougar.tests.clienttests.exceptions;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.to.MandatoryParamsOperationResponseObject;
import com.betfair.baseline.v2.to.MandatoryParamsRequest;
import com.betfair.baseline.v2.to.NonMandatoryParamsOperationResponseObject;
import com.betfair.baseline.v2.to.NonMandatoryParamsRequest;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * Ensure that when a request with a non mandatory query parameter not set is performed against cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientPostQueryParamMandatoryNotSetTest {
    @Test(dataProvider = "TransportType", expectedExceptions = CougarValidationException.class)
    public void headerNull(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // Create body parameter to be passed
        MandatoryParamsRequest request = new MandatoryParamsRequest();
        request.setBodyParameter1("postBodyParamString1");

        request.setBodyParameter2("postBodyParamString2");
        // Make call to the method via client and validate the response is as expected
        wrapper.getClient().mandatoryParamsOperation(wrapper.getCtx(), null, "abc", request);
        fail("Expected an exception");
    }
    @Test(dataProvider = "TransportType", expectedExceptions = CougarValidationException.class)
    public void queryNull(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // Create body parameter to be passed
        MandatoryParamsRequest request = new MandatoryParamsRequest();
        request.setBodyParameter1("postBodyParamString1");
        request.setBodyParameter2("postBodyParamString2");

        // Make call to the method via client and validate the response is as expected
        wrapper.getClient().mandatoryParamsOperation(wrapper.getCtx(), "abc", null, request);
        fail("Expected an exception");
    }
    @Test(dataProvider = "TransportType", expectedExceptions = CougarValidationException.class)
    public void bodyNull(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);

        // Make call to the method via client and validate the response is as expected
        wrapper.getClient().mandatoryParamsOperation(wrapper.getCtx(), "abc", "abc", null);
        fail("Expected an exception");
    }
    @Test(dataProvider = "TransportType", expectedExceptions = CougarValidationException.class, enabled=false)
    public void bodyParametersNull(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // null params within request
        MandatoryParamsRequest request = new MandatoryParamsRequest();
        // Make call to the method via client and validate the response is as expected
        wrapper.getClient().mandatoryParamsOperation(wrapper.getCtx(), "abc", "abc", request);
        fail("Expected an exception");
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
