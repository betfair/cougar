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

// Originally from UpdatedComponentTests/ContentTypes/Rest_Post_NullContentType.xls;
package com.betfair.cougar.tests.updatedcomponenttests.contenttypes;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that when a Rest Post is performed against Cougar, supplying a null content type, the correct error is returned
 */
public class RestPostNullContentTypeTest {
    @Test
    public void nullValue() throws Exception {
        // Create the HttpCallBean
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean httpCallBeanBaseline = cougarManager1.getNewHttpCallBean();
        CougarManager cougarManagerBaseline = cougarManager1;
        // Get the cougar logging attribute for getting log entries later
        // Point the created HttpCallBean at the correct service
        httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");
        
        httpCallBeanBaseline.setVersion("v2");
        // Turn off detailed fault reporting
        cougarManagerBaseline.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set up the Http Call Bean to make the request
        httpCallBeanBaseline.setOperationName("testComplexMutator", "complex");
        
        Map map2 = new HashMap();
        map2.put("REST","{  \n    \"name\":\"sum\",\n    \"value1\":\"7\",\n    \"value2\":\"75\"\n}");
        httpCallBeanBaseline.setPostQueryObjects(map2);
        // Set the content type header, giving a null content type
        Map map3 = new HashMap();
        map3.put("Content-Type","null");
        httpCallBeanBaseline.setHeaderParams(map3);
        
        Map map4 = new HashMap();
        map4.put("application/xml",null);
        httpCallBeanBaseline.setAcceptProtocols(map4);
        // Get current time for getting log entries later

        Timestamp Timestamp = new Timestamp(System.currentTimeMillis());
        // Make the REST call to the operation
        cougarManagerBaseline.makeRestCougarHTTPCall(httpCallBeanBaseline, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.REST);
        // Create the expected response as an XML document (fault)
        XMLHelpers xMLHelpers6 = new XMLHelpers();
        Document expResponse = xMLHelpers6.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0012</faultstring><detail/></fault>");
        // Check the response is as expected (correct error returned)
        HttpResponseBean response7 = httpCallBeanBaseline.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.REST);
        AssertionUtils.multiAssertEquals(expResponse, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 415, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Unsupported Media Type", response7.getHttpStatusText());
        // Check the log entries are as expected
        
        
        cougarManagerBaseline.verifyAccessLogEntriesAfterDate(Timestamp, new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/complex", "UnsupportedMediaType") );
    }
    @Test
    public void notSet() throws Exception {
        // Create the HttpCallBean
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean httpCallBeanBaseline = cougarManager1.getNewHttpCallBean();
        CougarManager cougarManagerBaseline = cougarManager1;
        // Get the cougar logging attribute for getting log entries later
        // Point the created HttpCallBean at the correct service
        httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");

        httpCallBeanBaseline.setVersion("v2");
        // Turn off detailed fault reporting
        cougarManagerBaseline.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        // Set up the Http Call Bean to make the request
        httpCallBeanBaseline.setOperationName("testComplexMutator", "complex");

        Map map2 = new HashMap();
        map2.put("REST","{  \n    \"name\":\"sum\",\n    \"value1\":\"7\",\n    \"value2\":\"75\"\n}");
        httpCallBeanBaseline.setPostQueryObjects(map2);
        // Set the content type header, giving a null content type
        Map map3 = new HashMap();
        map3.put("Content-Type","");
        httpCallBeanBaseline.setHeaderParams(map3);

        Map map4 = new HashMap();
        map4.put("application/xml",null);
        httpCallBeanBaseline.setAcceptProtocols(map4);
        // Get current time for getting log entries later

        Timestamp Timestamp = new Timestamp(System.currentTimeMillis());
        // Make the REST call to the operation
        cougarManagerBaseline.makeRestCougarHTTPCall(httpCallBeanBaseline, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.REST);
        // Create the expected response as an XML document (fault)
        XMLHelpers xMLHelpers6 = new XMLHelpers();
        Document expResponse = xMLHelpers6.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0011</faultstring><detail/></fault>");
        // Check the response is as expected (correct error returned)
        HttpResponseBean response7 = httpCallBeanBaseline.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.REST);
        AssertionUtils.multiAssertEquals(expResponse, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 415, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Unsupported Media Type", response7.getHttpStatusText());
        // Check the log entries are as expected


        cougarManagerBaseline.verifyAccessLogEntriesAfterDate(Timestamp, new AccessLogRequirement("87.248.113.14", "/cougarBaseline/v2/complex", "UnsupportedMediaType") );
    }

}
