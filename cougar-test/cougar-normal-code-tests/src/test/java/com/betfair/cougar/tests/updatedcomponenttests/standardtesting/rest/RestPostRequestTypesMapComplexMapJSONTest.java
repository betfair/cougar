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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_Map_ComplexMap_JSON.xls;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that when a Rest (JSON) Post operation is performed, Cougar can correctly handle a complex Map in the post body
 */
public class RestPostRequestTypesMapComplexMapJSONTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        // Create date object that will appear in the returned map

        String date1 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 1, (int) 14, (int) 50, (int) 0, (int) 435);
        // Create date object that will appear in the returned map

        String date2 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 2, (int) 14, (int) 50, (int) 0, (int) 435);
        // Create date object that will appear in the returned map

        String date3 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 3, (int) 14, (int) 50, (int) 0, (int) 435);
        
        getNewHttpCallBean1.setOperationName("complexMapOperation");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Set the post body to contain a complex map using the dates created previously
        Map map5 = new HashMap();
        map5.put("RESTJSON","{\"message\":\n{    \n \"complexMap\":{   \n  \"aaa\":{  \n   \"dateTimeParameter\":\"2009-06-01T14:50:00.435Z\", \n   \"listParameter\":[ \n    \"aaa List Entry 1\",\n    \"aaa List Entry 2\",\n    \"aaa List Entry 3\"\n   ], \n   \"enumParameter\":\"BAR\", \n   \"stringParameter\":\"String value for aaa\" \n  },  \n  \"ccc\":{  \n   \"dateTimeParameter\":\"2009-06-03T14:50:00.435Z\", \n   \"listParameter\":[ \n    \"ccc List Entry 1\",\n    \"ccc List Entry 2\",\n    \"ccc List Entry 3\"\n   ], \n   \"enumParameter\":\"FOO\", \n   \"stringParameter\":\"String value for ccc\" \n  },  \n  \"bbb\":{  \n   \"dateTimeParameter\":\"2009-06-02T14:50:00.435Z\", \n   \"listParameter\":[ \n    \"bbb List Entry 1\",\n    \"bbb List Entry 2\",\n    \"bbb List Entry 3\"\n   ], \n   \"enumParameter\":\"FOOBAR\", \n   \"stringParameter\":\"String value for bbb\" \n  }  \n }   \n}\n}");
        getNewHttpCallBean1.setPostQueryObjects(map5);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp10 = new Timestamp(System.currentTimeMillis());
        // Make JSON call to the operation requesting an XML response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make JSON call to the operation requesting a JSON response
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers7 = new XMLHelpers();
        Document expectedResponseXML = xMLHelpers7.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<ComplexMapOperationResponse><ComplexMapOperationResponseObject><responseMap><entry key=\"aaa\"><SomeComplexObject><dateTimeParameter>"+date1+"</dateTimeParameter><listParameter><String>aaa List Entry 1</String><String>aaa List Entry 2</String><String>aaa List Entry 3</String></listParameter><enumParameter>BAR</enumParameter><stringParameter>String value for aaa</stringParameter></SomeComplexObject></entry><entry key=\"bbb\"><SomeComplexObject><dateTimeParameter>"+date2+"</dateTimeParameter><listParameter><String>bbb List Entry 1</String><String>bbb List Entry 2</String><String>bbb List Entry 3</String></listParameter><enumParameter>FOOBAR</enumParameter><stringParameter>String value for bbb</stringParameter></SomeComplexObject></entry><entry key=\"ccc\"><SomeComplexObject><dateTimeParameter>"+date3+"</dateTimeParameter><listParameter><String>ccc List Entry 1</String><String>ccc List Entry 2</String><String>ccc List Entry 3</String></listParameter><enumParameter>FOO</enumParameter><stringParameter>String value for ccc</stringParameter></SomeComplexObject></entry></responseMap></ComplexMapOperationResponseObject></ComplexMapOperationResponse>").getBytes())));
        // Create the expected response as a JSON object
        JSONHelpers jSONHelpers8 = new JSONHelpers();
        JSONObject expectedResponseJSON = jSONHelpers8.createAsJSONObject(new JSONObject("{responseMap:{aaa:{dateTimeParameter:\""+date1+"\",listParameter:[aaa List Entry 1,aaa List Entry 2,aaa List Entry 3],enumParameter:\"BAR\",stringParameter:\"String value for aaa\"},bbb:{dateTimeParameter:\""+date2+"\",listParameter:[bbb List Entry 1,bbb List Entry 2,bbb List Entry 3],enumParameter:\"FOOBAR\",stringParameter:\"String value for bbb\"},ccc:{dateTimeParameter:\""+date3+"\",listParameter:[ccc List Entry 1,ccc List Entry 2,ccc List Entry 3],enumParameter:\"FOO\",stringParameter:\"String value for ccc\"}}}"));
        // Check the 2 responses are as expected
        HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(expectedResponseXML, response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response9.getHttpStatusText());
        
        HttpResponseBean response10 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(expectedResponseJSON, response10.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response10.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response10.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp10, new RequestLogRequirement("2.8", "complexMapOperation"),new RequestLogRequirement("2.8", "complexMapOperation") );
    }

}
