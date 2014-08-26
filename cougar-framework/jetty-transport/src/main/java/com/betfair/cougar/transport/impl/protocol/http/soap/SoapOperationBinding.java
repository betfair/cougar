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

package com.betfair.cougar.transport.impl.protocol.http.soap;

import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapServiceBindingDescriptor;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;

public class SoapOperationBinding {

	private final OperationKey operationKey;
	private final OperationDefinition operationDefinition;
	private final SoapOperationBindingDescriptor bindingDescriptor;
	private final SoapServiceBindingDescriptor serviceBindingDescriptor;
    private final Schema schema;

	public SoapOperationBinding(OperationDefinition operationDefinition,
			SoapOperationBindingDescriptor bindingDescriptor,
			SoapServiceBindingDescriptor serviceBindingDescriptor,
            Schema schema) {
		this.operationDefinition = operationDefinition;
		this.operationKey = operationDefinition.getOperationKey();
		this.bindingDescriptor = bindingDescriptor;
		this.serviceBindingDescriptor = serviceBindingDescriptor;
        this.schema = schema;
	}

	public OperationKey getOperationKey() {
		return operationKey;
	}

	public OperationDefinition getOperationDefinition() {
		return operationDefinition;
	}

	public SoapOperationBindingDescriptor getBindingDescriptor() {
		return bindingDescriptor;
	}

	public SoapServiceBindingDescriptor getServiceBindingDescriptor() {
		return serviceBindingDescriptor;
	}

    public Schema getSchema() {
        return schema;
    }
}
