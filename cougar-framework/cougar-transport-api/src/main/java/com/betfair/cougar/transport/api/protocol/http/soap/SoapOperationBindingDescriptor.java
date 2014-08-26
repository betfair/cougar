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

package com.betfair.cougar.transport.api.protocol.http.soap;

import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ev.OperationKey;


/**
 * Describes how a Soap Request is bound to a particular Service operation and its parameters
 *
 */
public class SoapOperationBindingDescriptor implements OperationBindingDescriptor {

	private final String requestName;
	private final String responseName;
	private final OperationKey operationKey;

	public SoapOperationBindingDescriptor(OperationKey operationKey, String requestName, String responseName) {
		this.operationKey = operationKey;
		this.requestName = requestName;
		this.responseName = responseName;
	}

	@Override
	public OperationKey getOperationKey() {
		return operationKey;
	}

	/**
	 * @return the name of the SOAP request element
	 */
	public String getRequestName() {
		return requestName;
	}

	/**
	 * @return the name of the SOAP response element
	 */
	public String getResponseName() {
		return responseName;
	}

}
