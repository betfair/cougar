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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Post_RequestTypes_Bytes_Zeroes.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
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
 * Ensure that Cougar can handle Bytes in the post body, header params and query params that are zero
 */
public class RestPostRequestTypesBytesZeroesTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        HttpCallBean hbean = getNewHttpCallBean1;
        CougarManager hinstance = cougarManager1;
        // Get logging attribute for getting log entries later
        hbean.setOperationName("byteOperation");
        hbean.setServiceName("baseline","cougarBaseline");
        hbean.setVersion("v2");
        // Set each of the parameter types to contain a byte datatype object for zero
        Map<String, String> headerParams = new HashMap<String, String>();
        headerParams.put("HeaderParam","0");
        hbean.setHeaderParams(headerParams);
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("queryParam","0");
        hbean.setQueryParams(queryParams);
        hbean.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><bodyParameter>EYitl82RbhhPWMZKw2MNlxF4kIGuX03TWEPUBbAxaBs=</bodyParameter></message>".getBytes())));
        // Get current time for getting log entries later
        Timestamp getTimeAsTimeStamp9 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the operation
        hinstance.makeRestCougarHTTPCalls(getNewHttpCallBean1);
        // Create the expected response as an XML document
        Document createAsDocument11 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<ByteOperationResponseObject><bodyParameter>EYitl82RbhhPWMZKw2MNlxF4kIGuX03TWEPUBbAxaBs=</bodyParameter><headerParameter>0</headerParameter><queryParameter>0</queryParameter></ByteOperationResponseObject>".getBytes()));
        // Convert the expected response to REST types for comparison with actual responses
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertResponseToRestTypes12 = hinstance.convertResponseToRestTypes(createAsDocument11, hbean);
        // Check the 4 responses are as expected
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes12.get(CougarMessageProtocolRequestTypeEnum.RESTXML), hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLXML).getResponseObject());
        AssertionUtils.multiAssertEquals(200, hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLXML).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLXML).getHttpStatusText());
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes12.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getResponseObject());
        AssertionUtils.multiAssertEquals(200, hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON).getHttpStatusText());
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes12.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLJSON).getResponseObject());
        AssertionUtils.multiAssertEquals(200, hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLJSON).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTXMLJSON).getHttpStatusText());
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes12.get(CougarMessageProtocolRequestTypeEnum.RESTXML), hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getResponseObject());
        AssertionUtils.multiAssertEquals(200, hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", hbean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONXML).getHttpStatusText());
        // Check the log entries are as expected
        hinstance.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp9,
                new RequestLogRequirement("2.8","byteOperation"),
                new RequestLogRequirement("2.8","byteOperation"),
                new RequestLogRequirement("2.8","byteOperation"),
                new RequestLogRequirement("2.8","byteOperation"));
    }

}
