/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_MapDateTimeKey_XML.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

import com.betfair.testing.utils.cougar.misc.TimingHelpers;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;

/**
 * Ensure that Cougar can handle a map with a datetime key in the post body of an XML request
 */
public class RestPostRequestTypesMapDateTimeKeyXMLTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean hbean = cougarManager1.getNewHttpCallBean("87.248.113.14");
        CougarManager hinstance = cougarManager1;

        hbean.setOperationName("mapDateTimeKeyOperation");

        hbean.setServiceName("baseline", "cougarBaseline");

        hbean.setVersion("v2");
        // Create a date time object expected to be in the response object (as a key)

        String date1 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 1, (int) 13, (int) 50, (int) 0, (int) 0);
        // Create a date time object expected to be in the response object (as a key)

        String date2 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 1, (int) 14, (int) 50, (int) 0, (int) 0);
        // Set the post body to contain a map object with datetime keys
        hbean.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><mapDateTimeKey><entry key=\"2009-06-01T13:50:00.0Z\"><String>date1</String></entry><entry key=\"2009-06-01T14:50:00.0Z\"><String>date2</String></entry></mapDateTimeKey></message>".getBytes())));
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp11 = new Timestamp(System.currentTimeMillis());
        // Make XML call to the operation requesting an XML response
        hinstance.makeRestCougarHTTPCall(hbean, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Create the expected response as an XML document (using the date object created earlier)
        XMLHelpers xMLHelpers5 = new XMLHelpers();
        Document expectedResponseXML = xMLHelpers5.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<MapDateTimeKeyOperationResponse><MapDateTimeKeyOperationResponseObject><responseMap><entry key=\""+date1+"\"><String>date1</String></entry><entry key=\""+date2+"\"><String>date2</String></entry></responseMap></MapDateTimeKeyOperationResponseObject></MapDateTimeKeyOperationResponse>").getBytes())));
        // Check the response is as expected
        HttpResponseBean response6 = hbean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(expectedResponseXML, (Document) response6.getResponseObject(),"/*[local-name()='MapDateTimeKeyOperationResponse']/*[local-name()='MapDateTimeKeyOperationResponseObject']/*[local-name()='responseMap']");
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());

        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected

        hinstance.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp11, new RequestLogRequirement("2.8", "mapDateTimeKeyOperation") );
    }

}
