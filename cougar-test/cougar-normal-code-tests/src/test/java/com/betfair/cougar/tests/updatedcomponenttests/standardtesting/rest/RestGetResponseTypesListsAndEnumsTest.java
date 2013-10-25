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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Get_ResponseTypes_ListsAndEnums.xls;
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
 * Ensure that cougar correctly serializes Rest (XML/JSON) responses holding lists and Enums.
 */
public class RestGetResponseTypesListsAndEnumsTest {
    @Test
    public void doTest() throws Exception {
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean("87.248.113.14");
        cougarManager1 = cougarManager1;
        
        getNewHttpCallBean1.setOperationName("testLargeGet", "largeGet");
        
        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");
        
        getNewHttpCallBean1.setVersion("v2");
        
        Map map2 = new HashMap();
        map2.put("size","10");
        getNewHttpCallBean1.setQueryParams(map2);
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        // Make the 4 REST calls to the operation
        cougarManager1.makeRestCougarHTTPCalls(getNewHttpCallBean1);
        // Create the expected response as an XML document (including a list and enums)
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document expXMLResponse = xMLHelpers4.getXMLObjectFromString("<TestLargeGetResponse><LargeRequest><objects><ComplexObject><name>name 0</name><value1>0</value1><value2>1</value2></ComplexObject><ComplexObject><name>name 1</name><value1>1</value1><value2>2</value2></ComplexObject><ComplexObject><name>name 2</name><value1>2</value1><value2>3</value2></ComplexObject><ComplexObject><name>name 3</name><value1>3</value1><value2>4</value2></ComplexObject><ComplexObject><name>name 4</name><value1>4</value1><value2>5</value2></ComplexObject><ComplexObject><name>name 5</name><value1>5</value1><value2>6</value2></ComplexObject><ComplexObject><name>name 6</name><value1>6</value1><value2>7</value2></ComplexObject><ComplexObject><name>name 7</name><value1>7</value1><value2>8</value2></ComplexObject><ComplexObject><name>name 8</name><value1>8</value1><value2>9</value2></ComplexObject><ComplexObject><name>name 9</name><value1>9</value1><value2>10</value2></ComplexObject></objects><oddOrEven>EVEN</oddOrEven><size>10</size></LargeRequest></TestLargeGetResponse>");
        // Create the expected response as a JSON object (including a list and enums)
        JSONHelpers jSONHelpers5 = new JSONHelpers();
        JSONObject expJSONResponse = jSONHelpers5.createAsJSONObject(new JSONObject("{\"objects\":[{\"value2\":1,\"value1\":0,\"name\":\"name 0\"},{\"value2\":2,\"value1\":1,\"name\":\"name 1\"},{\"value2\":3,\"value1\":2,\"name\":\"name 2\"},{\"value2\":4,\"value1\":3,\"name\":\"name 3\"},{\"value2\":5,\"value1\":4,\"name\":\"name 4\"},{\"value2\":6,\"value1\":5,\"name\":\"name 5\"},{\"value2\":7,\"value1\":6,\"name\":\"name 6\"},{\"value2\":8,\"value1\":7,\"name\":\"name 7\"},{\"value2\":9,\"value1\":8,\"name\":\"name 8\"},{\"value2\":10,\"value1\":9,\"name\":\"name 9\"}],oddOrEven:\"EVEN\",size:10}"));
        // Check the 4 responses are as expected
        HttpResponseBean response6 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(expXMLResponse, response6.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response6.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response6.getHttpStatusText());
        
        HttpResponseBean response7 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
        AssertionUtils.multiAssertEquals(expJSONResponse, response7.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response7.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response7.getHttpStatusText());
        
        HttpResponseBean response8 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLJSON);
        AssertionUtils.multiAssertEquals(expJSONResponse, response8.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response8.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response8.getHttpStatusText());
        
        HttpResponseBean response9 = getNewHttpCallBean1.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONXML);
        AssertionUtils.multiAssertEquals(expXMLResponse, response9.getResponseObject());
        AssertionUtils.multiAssertEquals((int) 200, response9.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response9.getHttpStatusText());
        
        // generalHelpers.pauseTest(500L);
        // Check the log entries are as expected
        
        cougarManager1.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "testLargeGet"),new RequestLogRequirement("2.8", "testLargeGet"),new RequestLogRequirement("2.8", "testLargeGet"),new RequestLogRequirement("2.8", "testLargeGet") );
    }

}
