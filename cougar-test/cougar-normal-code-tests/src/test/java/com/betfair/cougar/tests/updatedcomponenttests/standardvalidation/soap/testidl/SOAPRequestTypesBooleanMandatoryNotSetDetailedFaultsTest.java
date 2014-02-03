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

// Originally from UpdatedComponentTests/StandardValidation/SOAP/Test-IDL/SOAP_RequestTypes_Boolean_Mandatory_NotSet_DetailedFaults.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.soap.testidl;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Ensure that when a SOAP request is received, the correct Detailed Fault Message is returned when detailed faults are enabled and the request has a missing mandatory Boolean parameter
 */
public class SOAPRequestTypesBooleanMandatoryNotSetDetailedFaultsTest {
    @Test(dataProvider = "SchemaValidationEnabled")
    public void doTest(boolean schemaValidationEnabled) throws Exception {
        CougarHelpers helpers = new CougarHelpers();
        try {
            CougarManager cougarManager = CougarManager.getInstance();
            helpers.setSOAPSchemaValidationEnabled(schemaValidationEnabled);
            // Create the SOAP request as an XML Document (with a missing mandatory boolean parameter)
            XMLHelpers xMLHelpers1 = new XMLHelpers();
            Document createAsDocument2 = xMLHelpers1.getXMLObjectFromString("<BoolOperationRequest><headerParam>true</headerParam><message><bodyParameter>true</bodyParameter></message></BoolOperationRequest>");
            // Set up the Http Call Bean to make the request
            CougarManager cougarManager2 = CougarManager.getInstance();
            HttpCallBean getNewHttpCallBean3 = cougarManager2.getNewHttpCallBean("87.248.113.14");
            CougarManager cougarManager3 = cougarManager2;
            // Enable Detailed Faults
            cougarManager3.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "true");

            getNewHttpCallBean3.setServiceName("Baseline");

            getNewHttpCallBean3.setVersion("v2");
            // Set the created SOAP request as the PostObject
            getNewHttpCallBean3.setPostObjectForRequestType(createAsDocument2, "SOAP");
            // Get current time for getting log entries later

            Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
            // Make the SOAP call to the operation
            cougarManager3.makeSoapCougarHTTPCalls(getNewHttpCallBean3);
            // Check the response is as expected
            HttpResponseBean response5 = getNewHttpCallBean3.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);

            Map<String, String> map5 = helpers.convertFaultObjectToMap(response5);
            AssertionUtils.multiAssertEquals("soapenv:Client", map5.get("faultCode"));
            AssertionUtils.multiAssertEquals("DSC-0018", map5.get("faultString"));
            if (schemaValidationEnabled) {
                AssertionUtils.multiAssertEquals("org.xml.sax.SAXParseException; cvc-complex-type.2.4.b: The content of element 'bas:BoolOperationRequest' is not complete. One of '{\"http://www.betfair.com/servicetypes/v2/Baseline/\":queryParam}' is expected.", map5.get("faultMessage"));
                assertTrue(map5.get("faultTrace"),map5.get("faultTrace").startsWith("org.xml.sax.SAXParseException"));
            }
            else {
                AssertionUtils.multiAssertEquals("Mandatory attributes not defined for parameter 'queryParam'", map5.get("faultMessage"));
            }
            // generalHelpers.pauseTest(500L);
            // Check the log entries are as expected


            CougarHelpers cougarHelpers9 = new CougarHelpers();
            String JavaVersion = cougarHelpers9.getJavaVersion();

            CougarManager cougarManager10 = CougarManager.getInstance();
            cougarManager10.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp9, new AccessLogRequirement("87.248.113.14", "/BaselineService/v2", "BadRequest"));
            // Reset the Detailed Faults attribute for other tests
            cougarManager3.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
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
