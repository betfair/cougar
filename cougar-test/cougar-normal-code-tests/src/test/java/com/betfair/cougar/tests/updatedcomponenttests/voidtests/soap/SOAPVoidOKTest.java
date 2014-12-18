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

// Originally from UpdatedComponentTests/VoidTests/SOAP/SOAP_Void_OK.xls;
package com.betfair.cougar.tests.updatedcomponenttests.voidtests.soap;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Test a SOAP call to an operation with a void reponse type is handled correctly
 */
public class SOAPVoidOKTest {
    @Test
    public void doTest() throws Exception {
        // Create the SOAP request as an XML Document
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<VoidResponseOperationRequest><message>foo</message></VoidResponseOperationRequest>");
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean("87.248.113.14");
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
        // Create the expected response object as an XML document (void response body)
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<soapenv:Body></soapenv:Body>");
        // Check the responses are as expected (with a null/void responseObject)
        HttpResponseBean response5 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(createAsDocument10, response5.getResponseObject());
        // Check the log entries are as expected

        cougarManager2.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp8, new RequestLogRequirement("2.8", "voidResponseOperation") );

        CougarManager cougarManager8 = CougarManager.getInstance();
        cougarManager8.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp8, new AccessLogRequirement("87.248.113.14", "/BaselineService/v2", "Ok") );
    }

}
