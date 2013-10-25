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

package com.betfair.testing.utils.cougar.beans;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean to hold the response information returned from a call made to a Cougar
 * container within the Betfair system. 
 */
public class HttpResponseBean {

	
	private int httpStatusCode;
	private String httpStatusText;
	private Object responseObject;
	private Timestamp requestTime;
	private Timestamp responseTime;
	private Map<String, String> responseHeaders;
	
	public Timestamp getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Timestamp requestTime) {
		this.requestTime = requestTime;
	}
	public Timestamp getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(Timestamp responseTime) {
		this.responseTime = responseTime;
	}
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	public String getHttpStatusText() {
		return httpStatusText;
	}
	public void setHttpStatusText(String httpStatusText) {
		this.httpStatusText = httpStatusText;
	}
	public Object getResponseObject() {
		return responseObject;
	}
	public void setResponseObject(Object responseObject) {
		this.responseObject = responseObject;
	}
	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}
	public void setResponseHeaders(Map<String, String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public boolean isKeyInTheHeader(String key){
		return responseHeaders.containsKey(key);
	}
	public void addEntryToResponseHeaders(String key, String value)
	{
		if (this.responseHeaders==null)
		{
			responseHeaders = new HashMap<String, String>();
		}
		responseHeaders.put(key, value);
	}
	
	
}
