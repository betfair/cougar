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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_Doubles_OutOfRange_XML.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar can handle out of range Doubles in the post body, header params and query params (Testing for XML requests)
 */
public class RestPostRequestTypesDoublesOutOfRangeXMLTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        getNewHttpCallBean1.setOperationName("doubleOperation");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        // Set each of the parameter types to contain an out of range double datatype object
        Map map2 = new HashMap();
        map2.put("queryParam","-1.7976931344323158E969");
        getNewHttpCallBean1.setQueryParams(map2);
        
        Map map3 = new HashMap();
        map3.put("HeaderParam","1.7976931348334158E979");
        getNewHttpCallBean1.setHeaderParams(map3);
        
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument8 = xMLHelpers4.getXMLObjectFromString("<DoubleOperationRequest><message><bodyParameter>-1.7976935448623158E999</bodyParameter></message></DoubleOperationRequest>");
        
        getNewHttpCallBean1.setPostObjectForRequestType(createAsDocument8, "RESTXML");
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp10 = new Timestamp(System.currentTimeMillis());
        // Make an XML call to the operation requesting XML response type
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make an XML call to the operation requesting JSON response type
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers6 = new XMLHelpers();
        Document createAsDocument13 = xMLHelpers6.getXMLObjectFromString("<DoubleOperationResponse><DoubleOperationResponseObject><bodyParameter>-INF</bodyParameter><headerParameter>INF</headerParameter><queryParameter>-INF</queryParameter></DoubleOperationResponseObject></DoubleOperationResponse>");
        // Create the expected response as a JSON object
        JSONHelpers jSONHelpers7 = new JSONHelpers();
        JSONObject createAsJSONObject14 = jSONHelpers7.createAsJSONObject(new JSONObject("{bodyParameter:\"-Infinity\",headerParameter:\"Infinity\",queryParameter:\"-Infinity\"}"));
        // Check the 4 responses are as expected
        HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(createAsDocument13, response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response8.getHttpStatusText());
        
        HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(createAsJSONObject14, response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response9.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp10, new RequestLogRequirement("2.8", "doubleOperation"),new RequestLogRequirement("2.8", "doubleOperation") );
    }

}
