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

package com.betfair.cougar.api;

import java.util.List;


public class ServiceInfo {

    private String namespace;
	private Service service;
	private String serviceName;
	private String version;
	private List<String> operations;

	public ServiceInfo(String namespace, Service service, String serviceName,
			String version, List<String> operations ) {
		super();
        this.namespace = namespace;
		this.service = service;
		this.serviceName = serviceName;
		this.version = version;
		this.operations = operations;
	}

	public Service getService() {
		return service;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getVersion() {
		return version;
	}

    public List<String> getOperations() {
        return operations;
    }

    public String getNamespace() {
        return namespace;
    }
}
