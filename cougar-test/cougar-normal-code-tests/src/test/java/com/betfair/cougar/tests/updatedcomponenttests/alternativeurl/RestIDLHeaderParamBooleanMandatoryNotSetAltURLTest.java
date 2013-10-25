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

// Originally from UpdatedComponentTests/AlternativeURL/Rest_IDL_HeaderParam_Boolean_Mandatory_NotSet_AltURL.xls;
package com.betfair.cougar.tests.updatedcomponenttests.alternativeurl;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that Cougar  can successfully handle a request, with a missing mandatory parameter, to the alternative URL exposed by the baseline service (e.g. http://10.2.8.203:8080/www/cougarBaseline/v2.8/boolOperation)
 */
public class RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager manager = CougarManager.getInstance();
        HttpCallBean hbean = manager.getNewHttpCallBean("87.248.113.14");

        manager.setCougarFaultControllerJMXMBeanAttrbiute("DetailedFaults", "false");
        
        hbean.setOperationName("boolOperation");
        
        hbean.setServiceName("baseline", "cougarBaseline");
        
        hbean.setVersion("v2");
        // Set the request to use the alternative URL (With www inserted into it)
        hbean.setAlternativeURL("/www");
        // Set the parameters but don't set the mandatory bool header parameter
        Map map2 = new HashMap();
        map2.put("queryParam", "true");
        hbean.setQueryParams(map2);
        
        hbean.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><bodyParameter>false</bodyParameter></message>".getBytes())));
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp10 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the operation
        manager.makeRestCougarHTTPCalls(hbean);
        // Create the expected response as an XML document (Fault)
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument12 = xMLHelpers4.getXMLObjectFromString("<fault><faultcode>Client</faultcode><faultstring>DSC-0018</faultstring><detail/></fault>");
        // Convert the expected response to REST types for comparison with actual responses
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertResponseToRestTypes13 = manager.convertResponseToRestTypes(createAsDocument12, hbean);
        // Check the 4 responses are as expected (Bad Request)
        HttpResponseBean response5 = hbean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes13.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response5.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 400, response5.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Bad Request", response5.getHttpStatusText());
        
        HttpResponseBean response6 = hbean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes13.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 400, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Bad Request", response6.getHttpStatusText());
        
        HttpResponseBean response7 = hbean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes13.get(CougarMessageProtocolRequestTypeEnum.RESTJSON), response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 400, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Bad Request", response7.getHttpStatusText());
        
        HttpResponseBean response8 = hbean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes13.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 400, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("Bad Request", response8.getHttpStatusText());

        manager.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp10, 5000,
                new AccessLogRequirement("87.248.113.14", "/www/cougarBaseline/v2/boolOperation", "BadRequest"),
                new AccessLogRequirement("87.248.113.14", "/www/cougarBaseline/v2/boolOperation", "BadRequest"),
                new AccessLogRequirement("87.248.113.14", "/www/cougarBaseline/v2/boolOperation", "BadRequest"),
                new AccessLogRequirement("87.248.113.14", "/www/cougarBaseline/v2/boolOperation", "BadRequest")
        );
    }

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        new RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest().doTest();
        double total1 = (System.nanoTime() - start) / 1000000.0;
        System.out.println(total1+"ms");
        start = System.nanoTime();
        new RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest().doTest();
        new RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest().doTest();
        new RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest().doTest();
        new RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest().doTest();
        new RestIDLHeaderParamBooleanMandatoryNotSetAltURLTest().doTest();
        double total = (System.nanoTime() - start) / 5000000.0;
        System.out.println(total+"ms");
    }
}
