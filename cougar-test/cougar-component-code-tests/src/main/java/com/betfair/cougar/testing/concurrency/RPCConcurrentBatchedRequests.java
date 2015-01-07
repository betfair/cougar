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

import com.betfair.testing.utils.cougar.beans.BatchedRequestBean;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import org.json.JSONException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RPCConcurrentBatchedRequests {

	private List<Thread> threads = new ArrayList<Thread>();
	private List<Executor> executors = new ArrayList<Executor>();
	private static final String OK_STATUS_CODE = "200";

	public List<Thread> getThreads() {
		return threads;
	}

	public void setThreads(List<Thread> threads) {
		this.threads = threads;
	}

	public List<Executor> getExecutors() {
		return executors;
	}

	public void setExecutors(List<Executor> executors) {
		this.executors = executors;
	}

	public RPCConcurrentBatchedRequestsResultBean executeTest(Integer numberOfThreads, Integer numberOfCallsPerThread) throws InterruptedException, JSONException{

		//Build required calls and executors, and thread them
		for (int i = 0; i < numberOfThreads; i++) {
			Executor executor = new Executor("executor"+i);
			executors.add(executor);
			Thread thread = new Thread(executor);
			threads.add(thread);
			executor.setNumberOfRequests(numberOfCallsPerThread);
			executor.buildCalls();
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
		Map<String, Map<String,Object>> expectedResponses = new LinkedHashMap<String, Map<String,Object>>();
		Map<String, Map<String,Object>> actualResponses = new LinkedHashMap<String, Map<String,Object>>();

		//Populate response maps
		for (Executor executor: executors) {
			Map<String, Map<String,Object>> executorExpectedResponses = executor.getExpectedResponses();
			expectedResponses.putAll(executorExpectedResponses);
			Map<String, Map<String,Object>> executorActualResponses = executor.getActualResponses();
			actualResponses.putAll(executorActualResponses);
		}

		//Put maps into bean and return
		RPCConcurrentBatchedRequestsResultBean returnBean = new RPCConcurrentBatchedRequestsResultBean();
		returnBean.setActualResponses(actualResponses);
		returnBean.setExpectedResponses(expectedResponses);
		return returnBean;

	}




	public static class Executor implements Runnable {

		public Executor(String identifier) {
			this.identifier = identifier;
		}

		private CougarManager cougarManager = CougarManager.getInstance();
		private CougarHelpers cougarHelpers = new CougarHelpers();

		private String identifier;
		private int numberOfRequests;

		private Map<String, Map<String,Object>> expectedResponses = new LinkedHashMap<String, Map<String,Object>>();
		private Map<String, Map<String,Object>> actualResponses = new LinkedHashMap<String, Map<String,Object>>();
		private List<HttpCallBean> httpCallBeans = new ArrayList<HttpCallBean>();
		private Map<String, Timestamp> expectedRequestTimes = new LinkedHashMap<String, Timestamp>();

		public void run() {
			try {
				this.makeCalls();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void buildCalls() throws JSONException {

			for (int i = 0; i < numberOfRequests+1; i++) {
				//Setup call beans
				HttpCallBean callBean = new HttpCallBean();

				BatchedRequestBean request1 = new BatchedRequestBean();
				request1.setMethod("testSimpleGet");
				request1.setId("1");
				request1.setParams("[\"foo\"]");
				request1.setVersion("2.0");
                request1.setService("Baseline");

				BatchedRequestBean request2 = new BatchedRequestBean();
				request2.setMethod("testSimpleGet");
				request2.setId("2");
				request2.setParams("[\"foo\"]");
				request2.setVersion("2.0");
                request2.setService("Baseline");

				List<BatchedRequestBean> requests = new ArrayList<BatchedRequestBean>();
				requests.add(request1);
				requests.add(request2);

				callBean.setJSONRPC(true);
				callBean.setBatchedRequestsDirect(requests);
				httpCallBeans.add(callBean);

				//Store expected responses
				Map<String,Object> responseMap = new HashMap<String,Object>();

				String response1 = "{\"id\":1,\"result\":{\"message\":\"foo\"},\"jsonrpc\":\"2.0\"}";
				String response2 = "{\"id\":2,\"result\":{\"message\":\"foo\"},\"jsonrpc\":\"2.0\"}";

				responseMap.put("response1", response1);
				responseMap.put("response2", response2);
				responseMap.put("httpStatusCode", OK_STATUS_CODE);
				responseMap.put("httpStatusText", "OK");

				expectedResponses.put(identifier + "Response " + i, responseMap);
			}
		}

		public void makeCalls() throws JSONException {
			//Make the calls
			int loopCounter = 0;
			for(HttpCallBean callBean: httpCallBeans) {
				Date time = new Date();
				//System.out.println("Making call: " + identifier + "-" + "Response " + loopCounter + " at: " + time.getTime()) ;
				expectedRequestTimes.put(identifier + "Response " + loopCounter, new Timestamp(time.getTime()));
				cougarManager.makeRestCougarHTTPCall(callBean, CougarMessageProtocolRequestTypeEnum.RESTJSON, CougarMessageContentTypeEnum.JSON);
				loopCounter++;
			}


			//Get actual responses
			loopCounter=0;
			for (HttpCallBean httpCallBean: httpCallBeans) {
				HttpResponseBean responseBean = httpCallBean.getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum.RESTJSONJSON);
				responseBean.clearResponseHeaders();
				Map<String,Object> responseMap = cougarHelpers.convertBatchedResponseToMap(responseBean);
				actualResponses.put(identifier + "Response " + loopCounter, responseMap);
				loopCounter++;
			}

			//Set the expected response time
			for (String keyString: expectedResponses.keySet()) {
				Map<String,Object> responseMap = expectedResponses.get(keyString);
				Timestamp requestTime = expectedRequestTimes.get(keyString);
				responseMap.put("requestTime", requestTime);
				responseMap.put("responseTime", requestTime);
			}

		}

		public Map<String, Map<String,Object>> getActualResponses() {
			return actualResponses;
		}

		public void setActualResponses(Map<String, Map<String,Object>> actualResponses) {
			this.actualResponses = actualResponses;
		}

		public Map<String, Map<String,Object>> getExpectedResponses() {
			return expectedResponses;
		}

		public void setExpectedResponses(Map<String, Map<String,Object>> expectedResponses) {
			this.expectedResponses = expectedResponses;
		}

		public int getNumberOfRequests() {
			return numberOfRequests;
		}

		public void setNumberOfRequests(int numberOfRequests) {
			this.numberOfRequests = numberOfRequests;
		}
	}

	public static class RPCConcurrentBatchedRequestsResultBean {

		private Map<String, Map<String,Object>> expectedResponses = new LinkedHashMap<String, Map<String,Object>>();
		private Map<String, Map<String,Object>> actualResponses = new LinkedHashMap<String, Map<String,Object>>();

		public Map<String, Map<String,Object>> getActualResponses() {
			return actualResponses;
		}
		public void setActualResponses(Map<String, Map<String,Object>> actualResponses) {
			this.actualResponses = actualResponses;
		}
		public Map<String, Map<String,Object>> getExpectedResponses() {
			return expectedResponses;
		}
		public void setExpectedResponses(Map<String, Map<String,Object>> expectedResponses) {
			this.expectedResponses = expectedResponses;
		}
	}
}