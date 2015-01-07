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

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BothRestAndSoapConcurrentRequestsJETTTest {

	private List<Thread> threads = new ArrayList<Thread>();
	private List<Executor> restExecutors = new ArrayList<Executor>();
	private List<SOAPExecutor> sOAPExecutors = new ArrayList<SOAPExecutor>();
	private static final int OK_STATUS_CODE = 200;

	public List<Thread> getThreads() {
		return threads;
	}


	public void setThreads(List<Thread> threads) {
		this.threads = threads;
	}


	public List<Executor> getRestExecutors() {
		return restExecutors;
	}


	public void setRestExecutors(List<Executor> restExecutors) {
		this.restExecutors = restExecutors;
	}


	public List<SOAPExecutor> getsOAPExecutors() {
		return sOAPExecutors;
	}


	public void setsOAPExecutors(List<SOAPExecutor> sOAPExecutors) {
		this.sOAPExecutors = sOAPExecutors;
	}


	public BothRestAndSoapConcurrentRequestsJETTTestResultBean executeTest(Integer numberOfThreadsPerOperation, Integer numberOfCallsPerThread, CougarMessageProtocolRequestTypeEnum protocolRequestType, CougarMessageContentTypeEnum responseContentType) throws InterruptedException {

		//Build required calls and executors, and thread them
		for (int i = 0; i < numberOfThreadsPerOperation; i++) {
			SimpleExecutor restSimpleExecutor = new SimpleExecutor("restSimpleExecutor"+i);
			SOAPExecutor soapSimpleExecutor = new SOAPExecutor("soapSimpleExecutor"+i);

			restExecutors.add(restSimpleExecutor);
			sOAPExecutors.add(soapSimpleExecutor);

			Thread thread1 = new Thread(restSimpleExecutor);
			Thread thread2 = new Thread(soapSimpleExecutor);

			threads.add(thread1);
			threads.add(thread2);

			restSimpleExecutor.setNumberOfRequests(numberOfCallsPerThread);
			restSimpleExecutor.setRequestProtocolTypeEnum(protocolRequestType);
			restSimpleExecutor.buildCalls(responseContentType);

			soapSimpleExecutor.setNumberOfRequests(numberOfCallsPerThread);
			soapSimpleExecutor.buildCalls("TestSimpleGet");
		}

		//Start the threads
		for (Thread thread: threads) {
			thread.start();
		}

		//Wait until all threads finished
		for (Thread thread: threads) {
			thread.join();
		}

		//Create maps to hold responses to assert
		Map<String, HttpResponseBean> expectedResponses = new LinkedHashMap<String, HttpResponseBean>();
		Map<String, HttpResponseBean> actualResponses = new LinkedHashMap<String, HttpResponseBean>();


		//Populate response maps
		for (Executor executor: restExecutors) {
			Map<String, HttpResponseBean> executorExpectedResponses = executor.getExpectedResponses();
			expectedResponses.putAll(executorExpectedResponses);
			Map<String, HttpResponseBean> executorActualResponses = executor.getActualResponses();
			actualResponses.putAll(executorActualResponses);
		}

		//Populate response maps
		for (SOAPExecutor executor: sOAPExecutors) {
			Map<String, HttpResponseBean> executorExpectedResponses = executor.getExpectedResponses();
			expectedResponses.putAll(executorExpectedResponses);
			Map<String, HttpResponseBean> executorActualResponses = executor.getActualResponses();
			actualResponses.putAll(executorActualResponses);
		}

		//Put maps into bean and return
		BothRestAndSoapConcurrentRequestsJETTTestResultBean returnBean = new BothRestAndSoapConcurrentRequestsJETTTestResultBean();
		returnBean.setActualResponses(actualResponses);
		returnBean.setExpectedResponses(expectedResponses);
		return returnBean;

	}

	public abstract class Executor implements Runnable {
		public abstract Map<String, HttpResponseBean> getExpectedResponses();
		public abstract Map<String, HttpResponseBean> getActualResponses();
	};


	public class SimpleExecutor extends Executor {

		public SimpleExecutor(String identifier) {
			this.identifier = identifier;
		}

		private XMLHelpers xHelpers = new XMLHelpers();
		private CougarManager cougarManager = CougarManager.getInstance();

		private String identifier;
		private int numberOfRequests;
		private CougarMessageProtocolRequestTypeEnum requestProtocolTypeEnum;

		private Map<String, HttpResponseBean> expectedResponses = new LinkedHashMap<String, HttpResponseBean>();
		private Map<String, HttpResponseBean> actualResponses = new LinkedHashMap<String, HttpResponseBean>();
		private List<HttpCallBean> httpCallBeans = new ArrayList<HttpCallBean>();
		private Map<String, Timestamp> expectedRequestTimes = new LinkedHashMap<String, Timestamp>();

		public CougarMessageProtocolRequestTypeEnum getRequestProtocolTypeEnum() {
			return requestProtocolTypeEnum;
		}

		public void setRequestProtocolTypeEnum(
				CougarMessageProtocolRequestTypeEnum requestProtocolTypeEnum) {
			this.requestProtocolTypeEnum = requestProtocolTypeEnum;
		}

		public void run() {
			this.makeCalls();
		}

		public void buildCalls(CougarMessageContentTypeEnum responseContentTypeEnum) {

			for (int i = 0; i < numberOfRequests+1; i++) {
				//Setup call beans
				HttpCallBean callBean = new HttpCallBean();
				callBean.setServiceName("baseline","cougarBaseline");
				callBean.setVersion("v2");
				callBean.setOperationName("testSimpleGet","simple");
				LinkedHashMap<String, String> queryParams = new LinkedHashMap<String, String>();
				String queryParameter = identifier + "-" + i;
				queryParams.put("message", queryParameter);
				callBean.setQueryParams(queryParams);
				httpCallBeans.add(callBean);

				Map<String, String> acceptProtocols = new HashMap<String,String>();
				switch (responseContentTypeEnum) {
				case JSON:
					acceptProtocols.put("application/json", "");
					break;
				case XML:
					acceptProtocols.put("application/xml", "");
					break;
				}
				callBean.setAcceptProtocols(acceptProtocols);

				//Store expected responses
				HttpResponseBean responseBean = new HttpResponseBean();
				String responseXmlString = "<SimpleResponse><message>" + queryParameter + "</message></SimpleResponse>";
				Document responseBaseObject = xHelpers.getXMLObjectFromString(responseXmlString);
				Map<CougarMessageProtocolRequestTypeEnum, Object> builtExpectedResponse = cougarManager.convertResponseToRestTypes(responseBaseObject, callBean);
				switch (responseContentTypeEnum) {
				case XML:
					responseBean.setResponseObject(builtExpectedResponse.get(CougarMessageProtocolRequestTypeEnum.RESTXML));
					break;
				case JSON:
					responseBean.setResponseObject(builtExpectedResponse.get(CougarMessageProtocolRequestTypeEnum.RESTJSON));
					break;
				}
				responseBean.setHttpStatusCode(OK_STATUS_CODE);
				responseBean.setHttpStatusText("OK");

				expectedResponses.put(identifier + "Response " + i, responseBean);
			}
		}

		public void makeCalls() {
			//Make the calls
			int loopCounter = 0;
			for(HttpCallBean callBean: httpCallBeans) {
				Date time = new Date();
				//System.out.println("Making call: " + identifier + "-" + "Response " + loopCounter + " at: " + time.getTime()) ;
				expectedRequestTimes.put(identifier + "Response " + loopCounter, new Timestamp(time.getTime()));
				cougarManager.makeRestCougarHTTPCall(callBean, requestProtocolTypeEnum);
				loopCounter++;
			}


			//Get actual responses
			loopCounter=0;
			for (HttpCallBean httpCallBean: httpCallBeans) {
				HttpResponseBean responseBean = httpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.REST);
				responseBean.clearResponseHeaders();
				actualResponses.put(identifier + "Response " + loopCounter, responseBean);
				loopCounter++;
			}

			//Set the expected response time
			for (String keyString: expectedResponses.keySet()) {
				HttpResponseBean responseBean = expectedResponses.get(keyString);
				Timestamp requestTime = expectedRequestTimes.get(keyString);
				responseBean.setRequestTime(requestTime);
				responseBean.setResponseTime(requestTime);
			}

		}

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

		public int getNumberOfRequests() {
			return numberOfRequests;
		}

		public void setNumberOfRequests(int numberOfRequests) {
			this.numberOfRequests = numberOfRequests;
		}
	}


	public static class BothRestAndSoapConcurrentRequestsJETTTestResultBean {

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

