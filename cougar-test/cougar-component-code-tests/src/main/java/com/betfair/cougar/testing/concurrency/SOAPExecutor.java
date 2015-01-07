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
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SOAPExecutor extends Thread{
	private CougarManager cougarManager = CougarManager.getInstance();

	private String identifier;
	private int numberOfRequests;

	private List<HttpCallBean> httpCallBeans = new ArrayList<HttpCallBean>();
	private Map<String, Timestamp> expectedRequestTimes = new LinkedHashMap<String, Timestamp>();

	private Map<String, HttpResponseBean> expectedResponses = new LinkedHashMap<String, HttpResponseBean>();
	private Map<String, HttpResponseBean> actualResponses = new LinkedHashMap<String, HttpResponseBean>();

	public SOAPExecutor(String identifier) {
		this.identifier = identifier;
	}

	public Map<String, HttpResponseBean> getExpectedResponses()
	{
		return  expectedResponses;
	}

	public Map<String, HttpResponseBean> getActualResponses()
	{
		return actualResponses;
	}

	public void setNumberOfRequests(int numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}

	public void run() {
		this.makeCalls();
	}

	public void makeCalls() {
		// Make the calls
		int loopCounter = 0;
		for (HttpCallBean callBean : httpCallBeans) {

			Date time = new Date();
			expectedRequestTimes.put(identifier + "Response " + loopCounter, new Timestamp(time.getTime()));

			cougarManager.makeSoapCougarHTTPCalls(callBean);

			loopCounter++;
		}

		// Get actual responses
		loopCounter = 0;
		for (HttpCallBean httpCallBean : httpCallBeans) {
			HttpResponseBean responseBean = httpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.SOAP);
			responseBean.clearResponseHeaders();
			actualResponses.put(identifier + "Response " + loopCounter, responseBean);
			loopCounter++;
		}

		// Set the expected response time
		for (String keyString : expectedResponses.keySet()) {
			HttpResponseBean responseBean = expectedResponses
					.get(keyString);
			Timestamp requestTime = expectedRequestTimes.get(keyString);

			responseBean.setRequestTime(requestTime);
			responseBean.setResponseTime(requestTime);
		}
	}

	public void buildCalls(String operationName) {

		for (int i = 0; i < numberOfRequests + 1; i++) {

			// Setup call beans
			HttpCallBean callBean = new HttpCallBean();
			callBean.setServiceName("BaselineService");
			callBean.setVersion("v2");

			//TODO decide here at run time what message to create? for multiple services??

			SOAPMessageExchange msgEx = null;

			if(operationName.equalsIgnoreCase("TestComplexMutator")){
				msgEx = SOAPGenerator.buildSOAPMessageCOMPLEX(callBean);
			}
			else if (operationName.equalsIgnoreCase("TestSimpleGet")){
				msgEx = SOAPGenerator.buildSOAPMessageSIMPLEGET(callBean);
			}
			else if(operationName.equalsIgnoreCase("TestParameterStylesQA")){
				msgEx = SOAPGenerator.buildSOAPMessagePARAMSTYLES(callBean);
			}
			else if(operationName.equalsIgnoreCase("TestAsyncGet")){
				msgEx = SOAPGenerator.buildSOAPMessageAsyncGet(callBean);
			}
			else{
				throw new RuntimeException("Unsupported operation. i dont know how to handle the following operation: " + operationName);
			}

			callBean.setSOAPMessage(msgEx.getRequest());

			httpCallBeans.add(callBean);

			// create expected responses
			HttpResponseBean responseBean = new HttpResponseBean();

			//set the message to correspond to the request

			responseBean.setResponseObject(msgEx.getResponse());

			expectedResponses.put(identifier + "Response " + i,
					responseBean);
		}

	}
}
