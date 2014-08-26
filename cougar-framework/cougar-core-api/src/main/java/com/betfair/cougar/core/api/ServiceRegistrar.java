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

package com.betfair.cougar.core.api;

import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.ev.ExecutableResolver;

import java.util.Map;
import java.util.Set;

public interface ServiceRegistrar {

	/**
	 * Register a ServiceDefinition against an actual Service implementation.
	 * @param serviceDefinition defines the service to be registered
	 * @param implementation the concrete implementation of the service as defined by the definition
	 */
	public void registerService(ServiceDefinition serviceDefinition, Service implementation, ExecutableResolver resolver);

    /**
     * Register a ServiceDefinition against an actual Service implementation in the given namespace.
     * @param namespace the namespace in which to register the service.
     * @param serviceDefinition defines the service to be registered
     * @param implementation the concrete implementation of the service as defined by the definition
     */
    public void registerService(String namespace, ServiceDefinition serviceDefinition, Service implementation, ExecutableResolver resolve);

    /**
     * @return This method returns a map between each namespace and the set of serviceDefinitions bound
     * to that namespace
     */
    public Map<String, Set<ServiceDefinition>> getNamespaceServiceDefinitionMap();
}

