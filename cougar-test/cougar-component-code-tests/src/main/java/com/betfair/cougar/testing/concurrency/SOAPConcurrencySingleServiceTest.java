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

import com.betfair.testing.utils.cougar.beans.HttpResponseBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SOAP Concurrency test: number of Threads, calls per thread
 * targets a specific named service
 *
 */
public class SOAPConcurrencySingleServiceTest{

	public SOAPConcurrencySingleServiceTest(){}
	
	private List<Thread> threads = new ArrayList<Thread>();
	private List<SOAPExecutor> executors = new ArrayList<SOAPExecutor>();

	public List<Thread> getThreads() {
		return threads;
	}

	public void setThreads(List<Thread> threads) {
		this.threads = threads;
	}

	public List<SOAPExecutor> getExecutors() {
		return executors;
	}

	public void setExecutors(List<SOAPExecutor> executors) {
		this.executors = executors;
	}

	public SOAPConcurrenyResultBean executeTest(Integer numberOfThreads, Integer numberOfCallsPerThread, String operationName) throws InterruptedException {

		// Build required calls and executors, and thread them
		for (int i = 0; i < numberOfThreads; i++) {
			SOAPExecutor executor = new SOAPExecutor("executor" + i);
			executors.add(executor);
			Thread thread = new Thread(executor);
			threads.add(thread);

			executor.setNumberOfRequests(numberOfCallsPerThread);

			executor.buildCalls(operationName);
		}

		// Start the threads
		for (Thread thread : threads) {
			thread.start();
		}

		// Wait until all threads finished
		for (Thread thread : threads) {
			thread.join();
		}

		// Create maps to hold responses to assert
		Map<String, HttpResponseBean> expectedResponses = new LinkedHashMap<String, HttpResponseBean>();
		Map<String, HttpResponseBean> actualResponses = new LinkedHashMap<String, HttpResponseBean>();

		//Populate response maps
		for (SOAPExecutor executor: executors) {
			Map<String, HttpResponseBean> executorExpectedResponses = executor.getExpectedResponses();
			expectedResponses.putAll(executorExpectedResponses);
			Map<String, HttpResponseBean> executorActualResponses = executor.getActualResponses();
			actualResponses.putAll(executorActualResponses);	
		}
		
		//Put maps into bean and return
		SOAPConcurrenyResultBean returnBean = new SOAPConcurrenyResultBean();
		returnBean.setActualResponses(actualResponses);
		returnBean.setExpectedResponses(expectedResponses);
		return returnBean;
	}

}
