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

// Originally from UpdatedComponentTests/StandardValidation/REST/Rest_IDL_PostBodyElement_Boolean_JSON_null.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardvalidation.rest;

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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar returns the correct fault, when a REST JSON request contains a Boolean body parameter set to null
 */
public class RestIDLPostBodyElementBooleanJSONnullTest {
    @Test
    public void doTest() throws Exception {
        // Create the HttpCallBean
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean httpCallBeanBaseline = cougarManager1.getNewHttpCallBean();
        CougarManager cougarManagerBaseline = cougarManager1;
        // Get the cougar logging attribute for getting log entries later
        // Point the created HttpCallBean at the correct service
        httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");
        
        httpCallBeanBaseline.setVersion("v2");
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean("87.248.113.14");
        cougarManager2 = cougarManager2;
        
        cougarManager2.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        
        getNewHttpCallBean2.setOperationName("boolOperation");
        
        getNewHttpCallBean2.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean2.setVersion("v2");
        
        Map map3 = new HashMap();
        map3.put("HeaderParam","true");
        getNewHttpCallBean2.setHeaderParams(map3);
        
        Map map4 = new HashMap();
        map4.put("queryParam","true");
        getNewHttpCallBean2.setQueryParams(map4);
        // Set the Boolean body parameter as null
        Map map5 = new HashMap();
        map5.put("RESTJSON","{\"message\": {\n    \"bodyParameter\": null\n}}");
        getNewHttpCallBean2.setPostQueryObjects(map5);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp11 = new Timestamp(System.currentTimeMillis());
        // Make the REST JSON call to the operation, requesting XML response
        cougarManager2.makeRestCougarHTTPCall(getNewHttpCallBean2, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Make the REST JSON call to the operation, requesting JSON response
        cougarManager2.makeRestCougarHTTPCall(getNewHttpCallBean2, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
        // Create the expected response as an XML document (Fault)
        XMLHelpers xMLHelpers7 = new XMLHelpers();
        Document createAsDocument14 = xMLHelpers7.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0018</faultstring><detail/></fault>");
        // Convert the expected response to a JSON object for comparison with actual responses
        JSONHelpers jSONHelpers8 = new JSONHelpers();
        JSONObject convertXMLDocumentToJSONObjectRemoveRootElement15 = jSONHelpers8.convertXMLDocumentToJSONObjectRemoveRootElement(createAsDocument14);
        // Check the 2 responses are as expected (Bad Request)
        HttpResponseBean response9 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(convertXMLDocumentToJSONObjectRemoveRootElement15, response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 400, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Bad Request", response9.getHttpStatusText());
        
        HttpResponseBean response10 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(createAsDocument14, response10.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 400, response10.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Bad Request", response10.getHttpStatusText());
        // Check the log entries are as expected
        
        CougarManager cougarManager12 = CougarManager.getInstance();
        cougarManager12.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp11, new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/boolOperation", "BadRequest"),new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/boolOperation", "BadRequest") );
    }

}
