/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

// Originally from UpdatedComponentTests/StandardValidation/SOAP/SOAP_NoTestParams_Valid.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.soap;

import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Test Basic functionality of the Container, test simple requests and responses work for SOAP Protocol.
 */
public class SOAPNoTestParamsValidTest {
    @Test(dataProvider = "SchemaValidationEnabled")
    public void doTest(boolean schemaValidationEnabled) throws Exception {
        CougarHelpers helpers = new CougarHelpers();
        try {
            CougarManager cougarManager = CougarManager.getInstance();
            helpers.setSOAPSchemaValidationEnabled(schemaValidationEnabled);
            // Create the SOAP request as an XML Document
            XMLHelpers xMLHelpers1 = new XMLHelpers();
            Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<TestNoParamsRequest/>");
            // Set up the Http Call Bean to make the request
            CougarManager cougarManager2 = CougarManager.getInstance();
            HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean();
            cougarManager2 = cougarManager2;

            cougarManager2.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

            getNewHttpCallBean2.setServiceName("Baseline");

            getNewHttpCallBean2.setVersion("v2");
            // Set the created SOAP request as the PostObject
            getNewHttpCallBean2.setPostObjectForRequestType(createAsDocument1, "SOAP");
            // Get current time for getting log entries later

            Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
            // Make the SOAP call to the operation
            cougarManager2.makeSoapCougarHTTPCalls(getNewHttpCallBean2);
            // Create the expected response object as an XML document
            XMLHelpers xMLHelpers4 = new XMLHelpers();
            Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<response><version>1.0.0</version><status>hello</status></response>");

            // Check the response is as expected
            HttpResponseBean response5 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
            AssertionUtils.multiAssertEquals(createAsDocument10, response5.getResponseObject());
            // todo: Check the log entries are as expected
        } finally {
            helpers.setSOAPSchemaValidationEnabled(true);
        }
    }

    @DataProvider(name = "SchemaValidationEnabled")
    public Object[][] versions() {
        return new Object[][]{
                {true}
                , {false}
        };
    }

}
