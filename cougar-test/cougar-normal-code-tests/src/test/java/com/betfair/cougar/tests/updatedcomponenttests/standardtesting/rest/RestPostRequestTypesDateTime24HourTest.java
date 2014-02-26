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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_DateTime_24Hour.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

import com.betfair.testing.utils.cougar.misc.TimingHelpers;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;

/**
 * Ensure that Cougar fails with the dateTime data type in the post body (with the time set to 24:00:00)
 */
public class RestPostRequestTypesDateTime24HourTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        cougarManager1.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults","false");
        try {

            getNewHttpCallBean1.setOperationName("dateTimeOperation");

            getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");

            getNewHttpCallBean1.setVersion("v2");
            // Set the post body to contain a date time object
            getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<BodyParamDateTimeObject><dateTimeParameter>2009-06-01T24:00:00.0Z</dateTimeParameter></BodyParamDateTimeObject>".getBytes())));
            // Get current time for getting log entries later

            Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
            // Make the 4 REST calls to the operation
            cougarManager1.makeRestCougarHTTPCalls(getNewHttpCallBean1);
            // Create the expected response as an XML document (using the date objects created earlier)
            XMLHelpers xMLHelpers4 = new XMLHelpers();
            Document responseXMLForXMLInput = xMLHelpers4.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<fault><faultcode>Client</faultcode><faultstring>DSC-0044</faultstring><detail/></fault>").getBytes())));
            Document responseXMLForJSONInput = xMLHelpers4.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<fault><faultcode>Client</faultcode><faultstring>DSC-0044</faultstring><detail/></fault>").getBytes())));
            // Create the expected response as a JSON object (using the date objects created earlier)
            JSONHelpers jSONHelpers5 = new JSONHelpers();
            JSONObject responseJSONForJSONInput = jSONHelpers5.createAsJSONObject(new JSONObject("{\"detail\":{},\"faultcode\":\"Client\",\"faultstring\":\"DSC-0044\"}"));
            JSONObject responseJSONForXMLInput = jSONHelpers5.createAsJSONObject(new JSONObject("{\"detail\":{},\"faultcode\":\"Client\",\"faultstring\":\"DSC-0044\"}"));
            // Check the 4 responses are as expected
            HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
            AssertionUtils.multiAssertEquals(responseXMLForXMLInput, response6.getResponseObject());
            AssertionUtils.multiAssertEquals((int) 400, response6.getHttpStatusCode());
            AssertionUtils.multiAssertEquals("Bad Request", response6.getHttpStatusText());

            HttpResponseBean response7 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
            AssertionUtils.multiAssertEquals(responseJSONForJSONInput, response7.getResponseObject());
            AssertionUtils.multiAssertEquals((int) 400, response7.getHttpStatusCode());
            AssertionUtils.multiAssertEquals("Bad Request", response7.getHttpStatusText());

            HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
            AssertionUtils.multiAssertEquals(responseJSONForXMLInput, response8.getResponseObject());
            AssertionUtils.multiAssertEquals((int) 400, response8.getHttpStatusCode());
            AssertionUtils.multiAssertEquals("Bad Request", response8.getHttpStatusText());

            HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
            AssertionUtils.multiAssertEquals(responseXMLForJSONInput, response9.getResponseObject());
            AssertionUtils.multiAssertEquals((int) 400, response9.getHttpStatusCode());
            AssertionUtils.multiAssertEquals("Bad Request", response9.getHttpStatusText());

            // Check the log entries are as expected

        cougarManager1.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp9, new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/dateTimeOperation", "BadRequest"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/dateTimeOperation", "BadRequest"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/dateTimeOperation", "BadRequest"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/dateTimeOperation", "BadRequest") );
        }
        finally {
            cougarManager1.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults","true");
        }
    }

}
