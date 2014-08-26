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

package com.betfair.cougar.transport.jetty.jmx;

import java.util.List;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class JettyEndpoints {

	private String endPointsString;

	public void setEndPoints(List<String> endPoints) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String endPoint: endPoints) {
			stringBuffer.append(endPoint).append("<br>");
		}
		this.endPointsString = stringBuffer.toString();
	}

	@ManagedOperation(description="List all available Endpoints")
	public String listEndPoints() {
		return endPointsString;
	}
}
