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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.api.ExecutionContext;

public interface ExecutionContextResolver<T> {

	/**
	 * Get the transport for which this resolver is able to resolve an ExecutionContext
	 * @return TransportInfo
	 */
	public String getSupportedTransport();

	/**
	 * From the supplied request, resolves this to an ExecutionContext
	 * @param request
	 * @return the ExecutionContext, populated with details from the request
	 */
	public ExecutionContext resolveExecutionContext(T request);
}
