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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;

/**
 * Defines an Operation, its parameters and its return type.
 *
 */
public interface OperationDefinition {

	/**
	 * Get the key values of the Operation (service name, service version and operation name)
	 * @return
	 */
	public OperationKey getOperationKey();

	/**
	 * Get the Parameters (names and ParameterType) for the operation.
	 * @return
	 */
	public Parameter[] getParameters();

	/**
	 * Get the return type of the Operation
	 * @return
	 */
	public ParameterType getReturnType();

}