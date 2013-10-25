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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_ComplexDelegate_XML.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

import com.betfair.testing.utils.cougar.misc.TimingHelpers;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;

/**
 * Ensure that when a Rest (XML) Post operation is performed against Cougar a delegate can be used to populate complex response objects.
 */
public class RestPostComplexDelegateXMLTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        getNewHttpCallBean1.setOperationName("complexMapOperation");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Instruct cougar to use a delegate to populate the complex response object
        getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><complexMap><entry key=\"DELEGATE\"><SomeComplexObject/></entry></complexMap></message>".getBytes())));
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        // Make the XML call to the operation with XML response type
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make the XML call to the operation with JSON response type
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create date object expected to be received in response object

        String date = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 1970, (int) 1, (int) 1, (int) 0, (int) 1, (int) 52, (int) 233);
        // Create the expected response as an XML document (using date just created)
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document expectedResponseXML = xMLHelpers4.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<ComplexMapOperationResponse><ComplexMapOperationResponseObject><responseMap><entry key=\"object1\"><SomeComplexObject><dateTimeParameter>"+date+"</dateTimeParameter><listParameter><String>item1</String><String>item2</String></listParameter><enumParameter>BAR</enumParameter><stringParameter>delegate1</stringParameter></SomeComplexObject></entry><entry key=\"object2\"><SomeComplexObject><dateTimeParameter>"+date+"</dateTimeParameter><listParameter><String>item1</String><String>item2</String></listParameter><enumParameter>BAR</enumParameter><stringParameter>delegate2</stringParameter></SomeComplexObject></entry><entry key=\"object3\"><SomeComplexObject><dateTimeParameter>"+date+"</dateTimeParameter><listParameter><String>item1</String><String>item2</String></listParameter><enumParameter>BAR</enumParameter><stringParameter>delegate3</stringParameter></SomeComplexObject></entry></responseMap></ComplexMapOperationResponseObject></ComplexMapOperationResponse>").getBytes())));
        // Create the expected response as an JSON document (using date just created)
        JSONHelpers jSONHelpers5 = new JSONHelpers();
        JSONObject expectedResponseJSON = jSONHelpers5.createAsJSONObject(new JSONObject("{responseMap:{object1:{dateTimeParameter:\""+date+"\",listParameter:[item1,item2],enumParameter:\"BAR\",stringParameter:\"delegate1\"},object2:{dateTimeParameter:\""+date+"\",listParameter:[item1,item2],enumParameter:\"BAR\",stringParameter:\"delegate2\"},object3:{dateTimeParameter:\""+date+"\",listParameter:[item1,item2],enumParameter:\"BAR\",stringParameter:\"delegate3\"}}}"));
        // Check the XML response is as expected
        HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(expectedResponseXML, response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());
        // Check the JSON response is as expected
        HttpResponseBean response7 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(expectedResponseJSON, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response7.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "complexMapOperation"),new RequestLogRequirement("2.8", "complexMapOperation") );
    }

}
