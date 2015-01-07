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

package com.betfair.cougar.testing.concurrency;

import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestSustainedPostRequestsJETTTest {

	private XMLHelpers xHelpers = new XMLHelpers();
	private CougarManager cougarManager = CougarManager.getInstance();
	private static final int OK_STATUS_CODE = 200;

	public RestSustainedPostRequestsTestResultBean executeTest(Integer numberOfRequests, CougarMessageProtocolRequestTypeEnum protocolRequestType, CougarMessageContentTypeEnum responseContentType) {

		List<HttpCallBean> httpCallBeans = new ArrayList<HttpCallBean>();
		Map<String, HttpResponseBean> expectedResponses = new LinkedHashMap<String, HttpResponseBean>();
		Map<String, HttpResponseBean> actualResponses = new LinkedHashMap<String, HttpResponseBean>();
		Map<String, Timestamp> expectedRequestTimes = new LinkedHashMap<String, Timestamp>();


		for (int i = 0; i < numberOfRequests+1; i++) {
			//Setup call beans
			HttpCallBean callBean = createComplexCallBean(responseContentType, i);
			httpCallBeans.add(callBean);

			//Store expected responses
			HttpResponseBean responseBean = createExpectedResponse(responseContentType, callBean, i);
			expectedResponses.put("Response " + i, responseBean);
		}

		//Make the calls
		int loopCounter = 0;
		for(HttpCallBean callBean: httpCallBeans) {
			Date time = new Date();
			expectedRequestTimes.put("Response " + loopCounter, new Timestamp(time.getTime()));

			cougarManager.makeRestCougarHTTPCall(callBean, protocolRequestType);
			loopCounter++;
		}


		//Get actual responses
		loopCounter=0;
		for (HttpCallBean httpCallBean: httpCallBeans) {
			HttpResponseBean responseBean = httpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.REST);
			responseBean.clearResponseHeaders();
			actualResponses.put("Response " + loopCounter, responseBean);
			loopCounter++;
		}

		//Set the expected response time
		for(Map.Entry<String, HttpResponseBean> entry: expectedResponses.entrySet()){
			HttpResponseBean responseBean = entry.getValue();
			String keyString = entry.getKey();
			Timestamp requestTime = expectedRequestTimes.get(keyString);
			responseBean.setRequestTime(requestTime);
			responseBean.setResponseTime(requestTime);
		}

		RestSustainedPostRequestsTestResultBean returnBean = new RestSustainedPostRequestsTestResultBean();
		returnBean.setActualResponses(actualResponses);
		returnBean.setExpectedResponses(expectedResponses);
		return returnBean;
	}

	private HttpCallBean createComplexCallBean(CougarMessageContentTypeEnum responseContentType, int i){

		HttpCallBean callBean = new HttpCallBean();
		callBean.setServiceName("baseline","cougarBaseline");
		callBean.setVersion("v2");
		callBean.setOperationName("testComplexMutator","complex");

		Map<String, String> acceptProtocols = new HashMap<String,String>();
		switch (responseContentType) {
		case JSON:
			acceptProtocols.put("application/json", "");
			break;
		case XML:
			acceptProtocols.put("application/xml", "");
			break;
		}
		callBean.setAcceptProtocols(acceptProtocols);

		String requestXmlString = "";
		requestXmlString = "<message><name>sum</name><value1> " + i + "</value1><value2>" + i + "</value2></message>";
		Document requestDocument = xHelpers.getXMLObjectFromString(requestXmlString);
		callBean.setRestPostQueryObjects(requestDocument);

		return callBean;
	}

	private HttpResponseBean createExpectedResponse(CougarMessageContentTypeEnum responseContentType, HttpCallBean callBean, int i){

		HttpResponseBean responseBean = new HttpResponseBean();
		String responseXmlString = "<SimpleResponse><message>sum = " + i*2 + "</message></SimpleResponse>";
		Document responseBaseObject = xHelpers.getXMLObjectFromString(responseXmlString);
		Map<CougarMessageProtocolRequestTypeEnum, Object> builtExpectedResponse = cougarManager.convertResponseToRestTypes(responseBaseObject, callBean);
		switch (responseContentType) {
		case XML:
			responseBean.setResponseObject(builtExpectedResponse.get(CougarMessageProtocolRequestTypeEnum.RESTXML));
			break;
		case JSON:
			responseBean.setResponseObject(builtExpectedResponse.get(CougarMessageProtocolRequestTypeEnum.RESTJSON));
			break;
		}
		responseBean.setHttpStatusCode(OK_STATUS_CODE);
		responseBean.setHttpStatusText("OK");

		return responseBean;

	}

	public static class RestSustainedPostRequestsTestResultBean {

		private Map<String, HttpResponseBean> expectedResponses = new LinkedHashMap<String, HttpResponseBean>();
		private Map<String, HttpResponseBean> actualResponses = new LinkedHashMap<String, HttpResponseBean>();

		public Map<String, HttpResponseBean> getActualResponses() {
			return actualResponses;
		}
		public void setActualResponses(Map<String, HttpResponseBean> actualResponses) {
			this.actualResponses = actualResponses;
		}
		public Map<String, HttpResponseBean> getExpectedResponses() {
			return expectedResponses;
		}
		public void setExpectedResponses(Map<String, HttpResponseBean> expectedResponses) {
			this.expectedResponses = expectedResponses;
		}

	}

}
