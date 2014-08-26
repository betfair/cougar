/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.client;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the properties of an HTTP Message
 *
 */
public class Message {

	private LinkedHashMap<String, Object> queryParmMap;
	private LinkedHashMap<String, Object> headerParmMap;
	private LinkedHashMap<String, Object> requestBodyMap;

	public Message() {
		this.queryParmMap = new LinkedHashMap<String, Object>();
		this.headerParmMap = new LinkedHashMap<String, Object>();
		this.requestBodyMap = new LinkedHashMap<String, Object>();
	}
	public Map<String, Object> getQueryParmMap() {
		return queryParmMap;
	}

	public Map<String, Object> getHeaderMap() {
		return headerParmMap;
	}
	public Map<String, Object> getRequestBodyMap() {
		return requestBodyMap;
	}

	public Object addQueryParm(String key, Object value) {
		return queryParmMap.put(key, value);
	}

	public Object addHeaderParm(String key, Object value) {
		return headerParmMap.put(key, value);
	}

	public Object addRequestBody(String key, Object value) {
		return requestBodyMap.put(key, value);
	}

}
