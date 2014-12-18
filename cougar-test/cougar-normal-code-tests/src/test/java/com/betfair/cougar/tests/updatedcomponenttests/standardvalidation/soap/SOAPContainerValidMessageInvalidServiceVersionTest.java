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

// Originally from UpdatedComponentTests/StandardValidation/SOAP/SOAP_Container_ValidMessage_InvalidServiceVersion.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.soap;

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

/**
 * Ensure that Cougar returns the correct DSC-0021 fault when a  SOAP request is made to a Version of a Service that doesn't exist. Error should be "Not Found"
 */
public class SOAPContainerValidMessageInvalidServiceVersionTest {
    @Test(dataProvider = "SchemaValidationEnabled")
    public void doTest(boolean schemaValidationEnabled) throws Exception {
        CougarHelpers helpers = new CougarHelpers();
        try {
            CougarManager cougarManager = CougarManager.getInstance();
            helpers.setSOAPSchemaValidationEnabled(schemaValidationEnabled);
            // Create the HttpCallBean
            CougarManager cougarManager1 = CougarManager.getInstance();
            HttpCallBean httpCallBeanBaseline = cougarManager1.getNewHttpCallBean();
            CougarManager cougarManagerBaseline = cougarManager1;
            // Get the cougar logging attribute for getting log entries later
            // Point the created HttpCallBean at the correct service
            httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");

            httpCallBeanBaseline.setVersion("v2");
            // Create the SOAP request as an XML Document
            XMLHelpers xMLHelpers2 = new XMLHelpers();
            Document createAsDocument2 = xMLHelpers2.getXMLObjectFromString("<SimpleGetRequest><message>pp1</message></SimpleGetRequest>");
            // Set up the Http Call Bean to make the request
            CougarManager cougarManager3 = CougarManager.getInstance();
            HttpCallBean getNewHttpCallBean3 = cougarManager3.getNewHttpCallBean();
            cougarManager3 = cougarManager3;

            cougarManager3.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

            getNewHttpCallBean3.setServiceName("Baseline");
            // Point the request at an invalid version of a service
            getNewHttpCallBean3.setVersion("v0.1");
            // Set the created SOAP request as the PostObject
            getNewHttpCallBean3.setPostObjectForRequestType(createAsDocument2, "SOAP");
            // Get current time for getting log entries later

            Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
            // Make the SOAP call to the operation
            cougarManager3.makeSoapCougarHTTPCalls(getNewHttpCallBean3);
            // Create the expected response object as an XML document (fault)
            XMLHelpers xMLHelpers5 = new XMLHelpers();
            Document createAsDocument11 = xMLHelpers5.getXMLObjectFromString("<soapenv:Fault><faultcode>soapenv:Client</faultcode><faultstring>DSC-0021</faultstring><detail/></soapenv:Fault>");

            // Check the response is as expected
            HttpResponseBean response6 = getNewHttpCallBean3.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
            AssertionUtils.multiAssertEquals(createAsDocument11, response6.getResponseObject());
            // Check the log entries are as expected

            CougarHelpers cougarHelpers8 = new CougarHelpers();
            String JavaVersion = cougarHelpers8.getJavaVersion();

            CougarManager cougarManager9 = CougarManager.getInstance();
            cougarManager9.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp9, new AccessLogRequirement("87.248.113.14", "/BaselineService/v0.1", "NotFound"));
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
