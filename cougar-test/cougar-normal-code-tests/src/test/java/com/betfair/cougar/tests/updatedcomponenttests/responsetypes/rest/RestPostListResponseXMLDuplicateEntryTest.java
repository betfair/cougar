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

// Originally from UpdatedComponentTests/ResponseTypes/REST/Rest_Post_ListResponse_XML_DuplicateEntry.xls;
package com.betfair.cougar.tests.updatedcomponenttests.responsetypes.rest;

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
 * Ensure that when a Rest (XML/JSON) Post operation is performed against Cougar, passing a Set with duplicate entries in the post body, it is correctly de-serialized, processed, returned the correct response.
 */
public class RestPostListResponseXMLDuplicateEntryTest {
    @Test
    public void doTest() throws Exception {
        
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        
        getNewHttpCallBean1.setOperationName("TestSimpleListGet", "simpleListGet");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        
        getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<inputList><String>bbb string</String><String>aaa string</String><String>bbb string</String><String>ddd string</String><String>ccc string</String></inputList>".getBytes())));
        

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        
        cougarManager1.makeRestCougarHTTPCalls(getNewHttpCallBean1);
        
        XMLHelpers xMLHelpers3 = new XMLHelpers();
        Document createAsDocument9 = xMLHelpers3.getXMLObjectFromString("<TestSimpleListGetResponse><String>bbb string</String><String>aaa string</String><String>bbb string</String><String>ddd string</String><String>ccc string</String></TestSimpleListGetResponse>");
        
        JSONHelpers jSONHelpers4 = new JSONHelpers();
        JSONObject createAsJSONObject10 = jSONHelpers4.createAsJSONObject(new JSONObject("{ \"response\":[\"bbb string\",\"aaa string\",\"bbb string\",\"ddd string\",\"ccc string\"]}"));
        
        HttpResponseBean response5 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(createAsDocument9, response5.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response5.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response5.getHttpStatusText());
        
        HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(createAsJSONObject10, response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "testSimpleListGet"),new RequestLogRequirement("2.8", "testSimpleListGet"),new RequestLogRequirement("2.8", "testSimpleListGet"),new RequestLogRequirement("2.8", "testSimpleListGet") );
    }

}
