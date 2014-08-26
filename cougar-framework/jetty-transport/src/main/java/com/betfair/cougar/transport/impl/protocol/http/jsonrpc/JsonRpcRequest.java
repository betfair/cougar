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

package com.betfair.cougar.transport.impl.protocol.http.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonRpcRequest {
	@JsonProperty
	private Object id;
	@JsonProperty
	private JsonNode params;
	@JsonProperty
	private String method;
	@JsonProperty
	private String jsonrpc;

	public Object getId() {
		return id;
	}
	public JsonNode getParams() {
		return params;
	}
	public String getMethod() {
		return method;
	}
	public String getJsonrpc() {
		return jsonrpc;
	}


}
