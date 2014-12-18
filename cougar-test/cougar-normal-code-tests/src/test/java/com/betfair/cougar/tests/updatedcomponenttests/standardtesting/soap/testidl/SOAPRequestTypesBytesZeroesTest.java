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

// Originally from UpdatedComponentTests/StandardTesting/SOAP/Test-IDL/SOAP_RequestTypes_Bytes_Zeroes.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.soap.testidl;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Ensure that when a SOAP request is received, Cougar can handle the byte datatype with zeroes
 */
public class SOAPRequestTypesBytesZeroesTest {
    @Test
    public void doTest() throws Exception {
        // Create the SOAP request as an XML Document (with byte parameters)
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<ByteOperationRequest><headerParam>0</headerParam><queryParam>0</queryParam><message><bodyParameter>AAAA</bodyParameter></message></ByteOperationRequest>");
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean hbean = cougarManager2.getNewHttpCallBean("87.248.113.14");
        CougarManager hinstance = cougarManager2;

        hbean.setServiceName("Baseline");

        hbean.setVersion("v2");
        // Set the created SOAP request as the PostObject
        hbean.setPostObjectForRequestType(createAsDocument1, "SOAP");
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        // Make the SOAP call to the operation
        hinstance.makeSoapCougarHTTPCalls(hbean);
        // Create the expected response object as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument9 = xMLHelpers4.getXMLObjectFromString("<response><queryParameter>0</queryParameter><headerParameter>0</headerParameter><bodyParameter>AAAA</bodyParameter></response>");

        // Check the response is as expected
        HttpResponseBean response5 = hbean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(createAsDocument9, response5.getResponseObject());

        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected

        hinstance.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "byteOperation") );
    }

}
