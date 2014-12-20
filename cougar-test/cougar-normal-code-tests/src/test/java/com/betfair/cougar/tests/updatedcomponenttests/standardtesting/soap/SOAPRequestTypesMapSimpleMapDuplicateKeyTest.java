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

// Originally from UpdatedComponentTests/StandardTesting/SOAP/SOAP_RequestTypes_Map_SimpleMap_DuplicateKey.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.soap;

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
 * Ensure that when a SOAP request is received, Cougar can handle a SimpleMap datatype parameter that contains a duplicate key
 */
public class SOAPRequestTypesMapSimpleMapDuplicateKeyTest {
    @Test
    public void doTest() throws Exception {
        // Create the SOAP request as an XML Document (with a simple map parameter that contains a duplicate key)
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<SimpleMapOperationRequest><message><simpleMap><entry key=\"aaa\"><String>First Value for aaa</String></entry><entry key=\"aaa\"><String>Second Value for aaa</String></entry><entry key=\"bbb\"><String>Value for bbb</String></entry><entry key=\"ccc\"><String>Value for ccc</String></entry></simpleMap></message></SimpleMapOperationRequest>");
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean("87.248.113.14");
        cougarManager2 = cougarManager2;

        getNewHttpCallBean2.setServiceName("Baseline");

        getNewHttpCallBean2.setVersion("v2");
        // Set the created SOAP request as the PostObject
        getNewHttpCallBean2.setPostObjectForRequestType(createAsDocument1, "SOAP");
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        // Make the SOAP call to the operation
        cougarManager2.makeSoapCougarHTTPCalls(getNewHttpCallBean2);
        // Create the expected response object as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument9 = xMLHelpers4.getXMLObjectFromString("<response><responseMap><entry key=\"aaa\"><String>Second Value for aaa</String></entry><entry key=\"bbb\"><String>Value for bbb</String></entry><entry key=\"ccc\"><String>Value for ccc</String></entry></responseMap></response>");
        // Convert the expected response to SOAP for comparison with actual response

        // Check the response is as expected
        HttpResponseBean response5 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(createAsDocument9, response5.getResponseObject());

        // generalHelpers.pauseTest(2000L);
        // Check the log entries are as expected

        cougarManager2.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "simpleMapOperation") );
    }

}
