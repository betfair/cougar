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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_Map_ComplexMap_XML.xls;
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
 * Ensure that when a Rest (XML) Post operation is performed, Cougar can correctly handle a complex Map in the post body
 */
public class RestPostRequestTypesMapComplexMapXMLTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        getNewHttpCallBean1.setOperationName("complexMapOperation");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Create date object that will appear in the returned map

        String date1 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 1, (int) 13, (int) 50, (int) 0, (int) 435);
        // Create date object that will appear in the returned map

        String date2 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 2, (int) 13, (int) 50, (int) 0, (int) 435);
        // Create date object that will appear in the returned map

        String date3 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 3, (int) 13, (int) 50, (int) 0, (int) 435);
        // Set the post body to contain a complex map using the dates created previously
        getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><complexMap><entry key=\"aaa\"><SomeComplexObject><stringParameter>String value for aaa</stringParameter><dateTimeParameter>2009-06-01T13:50:00.435Z</dateTimeParameter><enumParameter>BAR</enumParameter><listParameter><String>aaa List Entry 1</String><String>aaa List Entry 2</String><String>aaa List Entry 3</String></listParameter></SomeComplexObject></entry><entry key=\"ccc\"><SomeComplexObject><stringParameter>String value for ccc</stringParameter><dateTimeParameter>2009-06-03T13:50:00.435Z</dateTimeParameter><enumParameter>FOO</enumParameter><listParameter><String>ccc List Entry 1</String><String>ccc List Entry 2</String><String>ccc List Entry 3</String></listParameter></SomeComplexObject></entry><entry key=\"bbb\"><SomeComplexObject><stringParameter>String value for bbb</stringParameter><dateTimeParameter>2009-06-02T13:50:00.435Z</dateTimeParameter><enumParameter>FOOBAR</enumParameter><listParameter><String>bbb List Entry 1</String><String>bbb List Entry 2</String><String>bbb List Entry 3</String></listParameter></SomeComplexObject></entry></complexMap></message>".getBytes())));
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp10 = new Timestamp(System.currentTimeMillis());
        // Make XML call to the operation requesting an XML response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make XML call to the operation requesting a JSON response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers6 = new XMLHelpers();
        Document expectedResponseXML = xMLHelpers6.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<ComplexMapOperationResponse><ComplexMapOperationResponseObject><responseMap><entry key=\"aaa\"><SomeComplexObject><dateTimeParameter>"+date1+"</dateTimeParameter><listParameter><String>aaa List Entry 1</String><String>aaa List Entry 2</String><String>aaa List Entry 3</String></listParameter><enumParameter>BAR</enumParameter><stringParameter>String value for aaa</stringParameter></SomeComplexObject></entry><entry key=\"bbb\"><SomeComplexObject><dateTimeParameter>"+date2+"</dateTimeParameter><listParameter><String>bbb List Entry 1</String><String>bbb List Entry 2</String><String>bbb List Entry 3</String></listParameter><enumParameter>FOOBAR</enumParameter><stringParameter>String value for bbb</stringParameter></SomeComplexObject></entry><entry key=\"ccc\"><SomeComplexObject><dateTimeParameter>"+date3+"</dateTimeParameter><listParameter><String>ccc List Entry 1</String><String>ccc List Entry 2</String><String>ccc List Entry 3</String></listParameter><enumParameter>FOO</enumParameter><stringParameter>String value for ccc</stringParameter></SomeComplexObject></entry></responseMap></ComplexMapOperationResponseObject></ComplexMapOperationResponse>").getBytes())));
        // Create the expected response as a JSON object
        JSONHelpers jSONHelpers7 = new JSONHelpers();
        JSONObject expectedResponseJSON = jSONHelpers7.createAsJSONObject(new JSONObject("{responseMap:{aaa:{dateTimeParameter:\""+date1+"\",listParameter:[aaa List Entry 1,aaa List Entry 2,aaa List Entry 3],enumParameter:\"BAR\",stringParameter:\"String value for aaa\"},bbb:{dateTimeParameter:\""+date2+"\",listParameter:[bbb List Entry 1,bbb List Entry 2,bbb List Entry 3],enumParameter:\"FOOBAR\",stringParameter:\"String value for bbb\"},ccc:{dateTimeParameter:\""+date3+"\",listParameter:[ccc List Entry 1,ccc List Entry 2,ccc List Entry 3],enumParameter:\"FOO\",stringParameter:\"String value for ccc\"}}}"));
        // Check the 2 responses are as expected
        HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(expectedResponseXML, response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response8.getHttpStatusText());
        
        HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(expectedResponseJSON, response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response9.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp10, new RequestLogRequirement("2.8", "complexMapOperation"),new RequestLogRequirement("2.8", "complexMapOperation") );
    }

}
