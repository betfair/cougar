/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.tests.clienttests;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.to.BodyParamBoolObject;
import com.betfair.baseline.v2.to.BodyParamI32Object;
import com.betfair.baseline.v2.to.BoolOperationResponseObject;
import com.betfair.baseline.v2.to.I32OperationResponseObject;
import com.betfair.baseline.v2.to.SimpleResponse;
import com.betfair.cougar.api.ExecutionContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CougarClientConcurrencyTestUtils {

	private CougarClientWrapper.TransportType transport;
	private List<Thread> threads = new ArrayList<Thread>();
	private List<Executor> executors = new ArrayList<Executor>();

	private static final int I32_HEADER = 5;
	private static final int I32_QUERY = 6;
	private static final int I32_BODY = 7;

    public ClientConcurrencyTestResultBean executeTest(CougarClientWrapper.TransportType transportType) throws Exception {

        this.transport = transportType;

        createExecutors();

        //Start the threads
        for (Thread t : threads) {
            t.start();
        }

        //Wait until all threads finished
        for (Thread t : threads) {
            t.join();
        }

        //Create maps to hold responses to assert
        Map<String, JSONObject> expectedResponses = new LinkedHashMap<String, JSONObject>();
        Map<String, JSONObject> actualResponses = new LinkedHashMap<String, JSONObject>();
        Map<String, Exception> exceptionResponses = new LinkedHashMap<String, Exception>();

        //Populate response maps
        for (Executor ex : executors) {
            actualResponses.put(ex.getMethod(), ex.getActualResponse());
            exceptionResponses.put(ex.getMethod(), ex.getExceptionResponse());
            expectedResponses.put(ex.getMethod(), ex.getExpectedResponse());
        }

        //Put maps into response bean and return
        ClientConcurrencyTestResultBean responseBean = new ClientConcurrencyTestResultBean();
        responseBean.setActualResponses(actualResponses);
        responseBean.setExpectedResponses(expectedResponses);
        responseBean.setExceptionResponses(exceptionResponses);

        return responseBean;
    }

	// Add as many threads as required in here and create a corresponding createExecutor method for each one
	public void createExecutors() throws Exception{

		Executor simpleExecutor = createSimpleExecutor();
		executors.add(simpleExecutor);
		Thread simpleThread = new Thread(simpleExecutor);
		threads.add(simpleThread);

		Executor boolExecutor = createBoolExecutor();
		executors.add(boolExecutor);
		Thread boolThread = new Thread(boolExecutor);
		threads.add(boolThread);

		Executor intExecutor = createIntExecutor();
		executors.add(intExecutor);
		Thread intThread = new Thread(intExecutor);
		threads.add(intThread);
	}

	public Executor createSimpleExecutor() throws Exception {


		//Set up request wrapper
		CougarClientWrapper simpleWrapper = CougarClientWrapper.getInstance(transport);
        Executor simpleExecutor = new Executor("testSimpleGet", simpleWrapper);

		//Set up expected response object
		SimpleResponse expectedResponse = new SimpleResponse();
		expectedResponse.setMessage("foo");

		JSONObject jsonObject = new JSONObject(expectedResponse.toString());
		simpleExecutor.setExpectedResponse(jsonObject);

		return simpleExecutor;
	}

	public Executor createBoolExecutor() throws Exception{

		//Set up request wrapper
		CougarClientWrapper boolWrapper = CougarClientWrapper.getInstance(transport);

        Executor boolExecutor = new Executor("boolOperation", boolWrapper);

		//Set up expected response object
		BoolOperationResponseObject expectedResponse = new BoolOperationResponseObject();
		expectedResponse.setHeaderParameter(false);
		expectedResponse.setQueryParameter(true);
		expectedResponse.setBodyParameter(true);

		JSONObject jsonObject = new JSONObject(expectedResponse.toString());
		boolExecutor.setExpectedResponse(jsonObject);

		return boolExecutor;
	}

	public Executor createIntExecutor() throws Exception{


		//Set up request wrapper
		CougarClientWrapper intWrapper = CougarClientWrapper.getInstance(transport);
        Executor intExecutor = new Executor("i32Operation", intWrapper);

		//Set up expected response object
		I32OperationResponseObject expectedResponse = new I32OperationResponseObject();
		expectedResponse.setHeaderParameter(I32_HEADER);
		expectedResponse.setQueryParameter(I32_QUERY);
		expectedResponse.setBodyParameter(I32_BODY);

		JSONObject jsonObject = new JSONObject(expectedResponse.toString());
		intExecutor.setExpectedResponse(jsonObject);

		return intExecutor;
	}

	public static class Executor implements Runnable{

		private String methodToExecute;
		private JSONObject actualResponse;
		private JSONObject expectedResponse;
        private final CougarClientWrapper wrapper;
        private Exception exceptionResponse;

        public Executor(String method, CougarClientWrapper wrapper){
			this.methodToExecute = method;
            this.wrapper = wrapper;
		}

		public JSONObject getActualResponse(){
			return actualResponse;
		}

        public Exception getExceptionResponse() {
            return exceptionResponse;
        }

        public void setExpectedResponse(JSONObject expectedResponse){
			this.expectedResponse = expectedResponse;
		}

		public JSONObject getExpectedResponse(){
			return expectedResponse;
		}

		public String getMethod(){
			return methodToExecute;
		}

		@Override
		public void run(){
			this.makeCall();
		}

		public void makeCall(){
			BaselineSyncClient client = wrapper.getClient();
			ExecutionContext ctx = wrapper.getCtx();

			if("testSimpleGet".equals(methodToExecute)){
				try{
					SimpleResponse response = client.testSimpleGet(ctx, "foo");
					this.actualResponse = new JSONObject(response.toString());
				} catch(SimpleException e){
					this.actualResponse = null;
                    this.exceptionResponse = e;
				} catch(JSONException j){
					this.actualResponse = null;
                    this.exceptionResponse = j;
				}
			}
			else if("i32Operation".equals(methodToExecute)){
				BodyParamI32Object body = new BodyParamI32Object();
				body.setBodyParameter(I32_BODY);
				try {
					I32OperationResponseObject response = client.i32Operation(ctx, I32_HEADER, I32_QUERY, body);
					this.actualResponse = new JSONObject(response.toString());
				} catch(SimpleException e) {
                    this.actualResponse = null;
                    this.exceptionResponse = e;
                } catch(JSONException j){
                    this.actualResponse = null;
                    this.exceptionResponse = j;
				}
			}
			else if("boolOperation".equals(methodToExecute)){
				BodyParamBoolObject body = new BodyParamBoolObject();
				body.setBodyParameter(true);
				try {
					 BoolOperationResponseObject response = client.boolOperation(ctx, false, true, body);
					 this.actualResponse = new JSONObject(response.toString());
				} catch(SimpleException e) {
                    this.actualResponse = null;
                    this.exceptionResponse = e;
                } catch(JSONException j){
                    this.actualResponse = null;
                    this.exceptionResponse = j;
				}
			}
		}
	}

	public static class ClientConcurrencyTestResultBean {

		private Map<String, JSONObject> expectedResponses = new LinkedHashMap<String, JSONObject>();
		private Map<String, JSONObject> actualResponses = new LinkedHashMap<String, JSONObject>();
        private Map<String, Exception> exceptionResponses;

		public void setActualResponses(Map<String, JSONObject> actualResponses) {
			this.actualResponses = actualResponses;
		}
		public void setExpectedResponses(Map<String, JSONObject> expectedResponses) {
			this.expectedResponses = expectedResponses;
		}

        public void setExceptionResponses(Map<String,Exception> exceptionResponses) {
            this.exceptionResponses = exceptionResponses;
        }

        public void assertOutcome(AssertionWrapper wrapper) {
            for (String key : expectedResponses.keySet()) {
                JSONObject expected = expectedResponses.get(key);
                JSONObject actual = actualResponses.get(key);
                Exception ex = exceptionResponses.get(key);
                if (ex != null) {
                    ex.printStackTrace();
                }
                wrapper.assertEquals(expected, actual);
            }
        }

        public static interface AssertionWrapper {
            void assertEquals(Object expected, Object actual);
        }
    }
}
