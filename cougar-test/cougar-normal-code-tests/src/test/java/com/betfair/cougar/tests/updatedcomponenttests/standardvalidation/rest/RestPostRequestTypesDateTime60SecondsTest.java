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

// Originally from UpdatedComponentTests/StandardValidation/REST/Rest_Post_RequestTypes_DateTime_60Seconds.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;

/**
 * Ensure that Cougar returns the correct fault, when a REST request has a body parameter that contains a date with the seconds set to 60 (should be rolled to the next minute)
 */
public class RestPostRequestTypesDateTime60SecondsTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        try {
            cougarManager1.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");

            getNewHttpCallBean1.setOperationName("dateTimeOperation");

            getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");

            getNewHttpCallBean1.setVersion("v2");
            // Set the body parameter to a date time object with seconds incorrectly set to 60
            getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<bodyParamDateTimeObject><dateTimeParameter>2009-12-01T00:00:60.000Z</dateTimeParameter></bodyParamDateTimeObject>".getBytes())));
            // Get current time for getting log entries later

            Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
            // Make the REST JSON call to the operation requesting an XML response
            cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
            // Make the REST JSON call to the operation requesting a JSON response
            cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
            // Create the expected response as an XML document (Fault)
            XMLHelpers xMLHelpers3 = new XMLHelpers();
            Document expectedXML = xMLHelpers3.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0044</faultstring><detail/></fault>");
            // Convert the expected response to a JSON object for comparison with the actual response
            JSONHelpers jSONHelpers4 = new JSONHelpers();
            JSONObject expectedJSON = jSONHelpers4.convertXMLDocumentToJSONObjectRemoveRootElement(expectedXML);
            // Check the 2 responses are as expected (Bad Request)
            AssertionUtils.multiAssertEquals(expectedXML, getNewHttpCallBean1.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getResponseObject());
            AssertionUtils.multiAssertEquals(400, getNewHttpCallBean1.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getHttpStatusCode());
            AssertionUtils.multiAssertEquals("Bad Request", getNewHttpCallBean1.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getHttpStatusText());
            AssertionUtils.multiAssertEquals(expectedJSON,getNewHttpCallBean1.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getResponseObject());
            AssertionUtils.multiAssertEquals(400, getNewHttpCallBean1.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getHttpStatusCode());
            AssertionUtils.multiAssertEquals("Bad Request", getNewHttpCallBean1.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getHttpStatusText());
            // Check the log entries are as expected
            System.err.println(getTimeAsTimeStamp9);
            cougarManager1.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp9
                    , new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/dateTimeOperation", "BadRequest")
                    , new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/dateTimeOperation", "BadRequest")
            );
        }
        finally {
            cougarManager1.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "true");
        }
    }

}
