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

package com.betfair.cougar.tests.updatedcomponenttests.countrycode;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class GetCountryCodeTest {
    @Test
    public void doTest() throws Exception {
    //Positive Test Cases
     Map<String,String>[] maparray = new HashMap[13];
     maparray[0] = new HashMap();
     maparray[0].put("host","www.betfair.es");
     maparray[0].put("value","ES");
     maparray[1] = new HashMap();
     maparray[1].put("host","www.betfair.it");
     maparray[1].put("value","IT");
     maparray[2] = new HashMap();
     maparray[2].put("host","www.scommessebetfair.it");
     maparray[2].put("value","IT");
     maparray[7] = new HashMap();
     maparray[7].put("host","www.bet0fair.co.it");
     maparray[7].put("value","IT");
     maparray[7] = new HashMap();
     maparray[7].put("host","www.bet0fair.co.es");
     maparray[7].put("value","ES");
     maparray[8] = new HashMap();
     maparray[8].put("host","my.betfair.it");
     maparray[8].put("value","IT");
     maparray[9] = new HashMap();
     maparray[9].put("host","my.betfair.es");
     maparray[9].put("value","ES");
     maparray[5] = new HashMap();
     maparray[5].put("host","0www.123betfair.it");
     maparray[5].put("value","IT");
     maparray[3] = new HashMap();
     maparray[3].put("host","http.betfair.it");
     maparray[3].put("value","IT");

     maparray[10] = new HashMap();
     maparray[10].put("host","www.bet-fair.es");
     maparray[10].put("value","ES");
      //Negative test cases
     maparray[4] = new HashMap();
     maparray[4].put("host","www.betfair.100");
     maparray[4].put("value","null");

     maparray[6] = new HashMap();
     maparray[6].put("host","www.betfair.com");
     maparray[6].put("value","null");



     maparray[11] = new HashMap();
     maparray[11].put("host","*ww.bet-fair.es");
     maparray[11].put("value","null");

     maparray[12] = new HashMap();
     maparray[12].put("host","www.betfair.*");
     maparray[12].put("value","null");

     maparray[11] = new HashMap();
     maparray[11].put("host","$ww.bet-fair.es");
     maparray[11].put("value","null");

     for (int i = 0; i < maparray.length; i++) {
        getInferedCountry(maparray[i].get("host"), maparray[i].get("value"));
     }

   }
   public void getInferedCountry(String host, String value)throws Exception{
    CougarManager cougarManager=CougarManager.getInstance();
    HttpCallBean getNewHttpCallBean=cougarManager.getNewHttpCallBean("87.248.113.14");
    getNewHttpCallBean.setOperationName("getInferredCountryCode","inferredCountryCode");
    getNewHttpCallBean.setServiceName("baseline","cougarBaseline");
    getNewHttpCallBean.setVersion("v2");
    Map map3 = new HashMap();
    map3.put("Host",host);
    getNewHttpCallBean.setHeaderParams(map3);
    // Make REST JSON call to the operation requesting a JSON response
    cougarManager.makeRestCougarHTTPCall(getNewHttpCallBean, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTJSON, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.JSON);
   //Create expected response as XML document
    XMLHelpers xMLHelpers = new XMLHelpers();
   //Document createAsDocument = xMLHelpers.getXMLObjectFromString("<String><></></String>");
    Document createAsDocument=xMLHelpers.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<String>"+value+"</String>").getBytes())));
   // Convert expected response to a JSON object for comparison with JSON actual response
    JSONHelpers jSONHelpers = new JSONHelpers();
    JSONObject convertXMLDocumentToJSONObjectRemoveRootElement = jSONHelpers.convertXMLDocumentToJSONObjectRemoveRootElement(createAsDocument);
    // Check the 2 responses are as expected (Bad Request)
    HttpResponseBean response = getNewHttpCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
    AssertionUtils.multiAssertEquals(convertXMLDocumentToJSONObjectRemoveRootElement, response.getResponseObject());
        }
    }


