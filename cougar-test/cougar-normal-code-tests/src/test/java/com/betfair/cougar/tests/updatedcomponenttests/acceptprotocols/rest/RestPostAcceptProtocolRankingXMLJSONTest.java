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

// Originally from UpdatedComponentTests/AcceptProtocols/Rest/Rest_Post_AcceptProtocolRankingXMLJSON.xls;
package com.betfair.cougar.tests.updatedcomponenttests.acceptprotocols.rest;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that when a Rest XML Post operation is performed on Cougar, with JSON the highest ranked response protocol, the response is JSON
 */
public class RestPostAcceptProtocolRankingXMLJSONTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;

        getNewHttpCallBean1.setOperationName("testComplexMutator", "complex");

        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");

        getNewHttpCallBean1.setVersion("v2");
        // Set the response protocols (with json ranked highest)
        Map map2 = new HashMap();
        map2.put("application/text","q=70");
        map2.put("application/xml","q=10");
        map2.put("application/json","q=20");
        getNewHttpCallBean1.setAcceptProtocols(map2);

        getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><name>sum</name><value1>7</value1><value2>75</value2></message>".getBytes())));
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
        // Make the XML call to the operation
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<response><message>sum = 82</message></response>");
        // Convert the expected response to REST types
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertResponseToRestTypes11 = cougarManager1.convertResponseToRestTypes(createAsDocument10, getNewHttpCallBean1);
        // Check the response is as expected (and in JSON)
        HttpResponseBean getResponseObjectsByEnum12 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.REST);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes11.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), getResponseObjectsByEnum12.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, getResponseObjectsByEnum12.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", getResponseObjectsByEnum12.getHttpStatusText());
        // Check the response header is as expected (JSON)
        Map<String, String> map6 = getResponseObjectsByEnum12.getFlattenedResponseHeaders();
        AssertionUtils.multiAssertEquals("application/json", map6.get("Content-Type"));
        // Check the log entries are as expected

        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp8, new RequestLogRequirement("2.8", "testComplexMutator") );

        cougarManager1.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp8, new AccessLogRequirement(null, null, "Ok") );
    }

}
