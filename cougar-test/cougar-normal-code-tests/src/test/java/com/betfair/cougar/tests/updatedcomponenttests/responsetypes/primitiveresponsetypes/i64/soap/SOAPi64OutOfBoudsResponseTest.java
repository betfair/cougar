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

// Originally from UpdatedComponentTests/ResponseTypes/PrimitiveResponseTypes/i64/SOAP/SOAP_i64OutOfBoudsResponse.xls;
package com.betfair.cougar.tests.updatedcomponenttests.responsetypes.primitiveresponsetypes.i64.soap;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Test that the Cougar service returns a correct error when an out of type bounds value is passed in i64 primitive type (SOAP)
 */
public class SOAPi64OutOfBoudsResponseTest {
    @Test
    public void doTest() throws Exception {
        // Create a soap request structure as a Document object
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document requestDocument = xMLHelpers1.getXMLObjectFromString("<I64SimpleTypeEchoRequest><msg>-9223372036854775809</msg></I64SimpleTypeEchoRequest>");
        // Get an HTTPCallBean
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean HTTPCallBean = cougarManager2.getNewHttpCallBean("87.248.113.14");
        CougarManager CougarManager = cougarManager2;
        // Get LogManager JMX Attribute
        // Set Cougar Fault Controller attributes
        CougarManager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set service name to call
        HTTPCallBean.setServiceName("Baseline");
        // Set service version to call
        HTTPCallBean.setVersion("v2");
        // Set post object and request type
        HTTPCallBean.setPostObjectForRequestType(requestDocument, "SOAP");
        // Get current time

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        // Make Cougar SOAP call
        CougarManager.makeSoapCougarHTTPCalls(HTTPCallBean);
        // Create a soap response structure as a Document object
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document responseDocument = xMLHelpers4.getXMLObjectFromString("<soapenv:Fault><faultcode>soapenv:Client</faultcode><faultstring>DSC-0044</faultstring><detail/></soapenv:Fault>");

        // Get the actual SOAP response and compare it to the expected response
        HttpResponseBean response6 = HTTPCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(responseDocument, response6.getResponseObject());
        // Pause test
        // generalHelpers.pauseTest(500L);
        // Get access log entries after the time recorded earlier in the test
        CougarManager.verifyAccessLogEntriesAfterDate(timestamp, new AccessLogRequirement("87.248.113.14", "/BaselineService/v2", "BadRequest") );
    }

}
