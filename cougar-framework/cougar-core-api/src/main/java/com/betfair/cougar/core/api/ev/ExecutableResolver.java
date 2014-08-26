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

/**
 * This interface encapsulates logic on how to resolve an OperationKey to an Executable.
 *
 */
public interface ExecutableResolver {

	/**
	 * Resolves an OperationKey to an Executable that will execute the operation.
	 *
     * @param operationKey the key of the operation
     * @param ev
     * @return the executable resolved from the key
	 */
	public Executable resolveExecutable(OperationKey operationKey, ExecutionVenue ev);
}
