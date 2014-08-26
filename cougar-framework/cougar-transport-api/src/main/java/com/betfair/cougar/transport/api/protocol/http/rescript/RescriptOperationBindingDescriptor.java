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

package com.betfair.cougar.transport.api.protocol.http.rescript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.OperationBindingDescriptor;

/**
 * Describes how to bind a cougar Service Operation to a Rescript Request.
 * This may be generated from an idd extensions xml file.
 *
 */
public class RescriptOperationBindingDescriptor implements OperationBindingDescriptor {

	private final String uri;
	private final OperationKey operationKey;
	private final String httpMethod;
	private final Map<String, RescriptParamBindingDescriptor> paramBindings;
	private final Class<? extends RescriptResponse> responseClass;
	private final Class<? extends RescriptBody> bodyClass;


    /**
	 * @param operationKey Defines the operation that is to be bound
	 * @param uri The http uri that will invoke this operation
	 * @param httpMethod GET or POST
	 * @param paramBindings Defines the list of arguments to the operation, and how they map to the request
	 */
	public RescriptOperationBindingDescriptor(final OperationKey operationKey, final String uri, final String httpMethod, final List<RescriptParamBindingDescriptor> paramBindings) {
		this(operationKey, uri, httpMethod, paramBindings, null, null);
	}

	/**
	 * @param operationKey Defines the operation that is to be bound
	 * @param uri The http uri that will invoke this operation
	 * @param httpMethod GET or POST
	 * @param paramBindings Defines the list of arguments to the operation, and how they map to the request
     * @param responseClass the class that the response will be wrapped in for unmarshalling
	 */
	public RescriptOperationBindingDescriptor(final OperationKey operationKey, final String uri, final String httpMethod, final List<RescriptParamBindingDescriptor> paramBindings, Class<? extends RescriptResponse> responseClass) {
		this(operationKey, uri, httpMethod, paramBindings, responseClass, null);
	}

	/**
	 * @param operationKey Defines the operation that is to be bound
	 * @param uri The http uri that will invoke this operation
	 * @param httpMethod GET or POST
	 * @param paramBindings Defines the list of arguments to the operation, and how they map to the request
     * @param responseClass the class that the response will be wrapped in for unmarshalling
     * @param bodyClass the class that the request's body will be marshalled into
	 */
	public RescriptOperationBindingDescriptor(final OperationKey operationKey, final String uri, final String httpMethod, final List<RescriptParamBindingDescriptor> paramBindings, Class<? extends RescriptResponse> responseClass, Class<? extends RescriptBody> bodyClass) {
		this.operationKey = operationKey;
		this.uri = uri;
		this.httpMethod = httpMethod;
		this.paramBindings = new HashMap<String, RescriptParamBindingDescriptor>();

        boolean hasBodyParams = false;
		for (RescriptParamBindingDescriptor desc : paramBindings) {
			this.paramBindings.put(desc.getName(), desc);
            if (desc.getSource() == RescriptParamBindingDescriptor.ParamSource.BODY) {
                hasBodyParams = true;
            }
		}

        if (hasBodyParams && bodyClass == null || !hasBodyParams && bodyClass != null) {
            throw new IllegalArgumentException("If an operation includes body parameters, then it must include a body class to unmarshall to!");
        }
        this.responseClass = responseClass;
		this.bodyClass = bodyClass;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getURI() {
		return uri;
	}

	public RescriptParamBindingDescriptor getHttpParamBindingDescriptor(String logicalName) {
		return paramBindings.get(logicalName);
	}

	public OperationKey getOperationKey() {
		return operationKey;
	}

	public Class<? extends RescriptBody> getBodyClass() {
		return bodyClass;
	}

	public Class<? extends RescriptResponse> getResponseClass() {
		return responseClass;
	}

    public boolean containsBodyData() {
        return (bodyClass != null);
    }

    public boolean voidReturnType() {
        return (responseClass == null);
    }

    @Override
    public String toString() {
        return "RescriptOperationBindingDescriptor{" +
                "uri='" + uri + '\'' +
                ", operationKey=" + operationKey +
                ", httpMethod='" + httpMethod + '\'' +
                ", paramBindings=" + paramBindings +
                ", responseClass=" + responseClass +
                ", bodyClass=" + bodyClass +
                '}';
    }
}
