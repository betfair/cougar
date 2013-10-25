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

import java.util.List;
import java.util.Set;

import com.betfair.cougar.api.ExecutionContext;

/**
 * An Execution Venue (EV) is a central location where operations and their implementations can be registered for execution.
 * The abstraction of the OperationDefiniton allows registration of Executables that may not be Java classes - for
 * example, the Executable may invoke an external script, or may forward the execution to another EV.
 *
 */
public interface ExecutionVenue {

	/**
	 * Registers an OperationDefinition against an actual Executable implementation
	 * @param def defines the Operation and its parameters
	 * @param executable the executable implementation of the operation
	 * @param recorder Provides methods for recording execution statistics
	 */
	public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder);

	/**
	 * Gets the OperationDefinition that has been registered with the EV for the provided key
	 * @param key unique values of the operation (service name, service version and operation name)
	 * @return the registered OperationDefinition
	 */
	public OperationDefinition getOperationDefinition(OperationKey key);

	/**
	 * Gets the keys for all the Operations that have been registered with this EV.
	 * @return all registered OperationKeys
	 */
	public Set<OperationKey> getOperationKeys();

	/**
	 * Execute the Executable registered for the provided OperationKey.
	 * @param ctx Provides contextual information for execution, such as channel id, user identity, geographical location etc.
	 * @param key Defines the operation to be executed
	 * @param args The arguments to be provided to the operation
	 * @param observer The ExecutionObserver is notified of either the result of the execution, or of an exception thrown during execution.
	 */
	public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer);

	public void setPreProcessors(List<ExecutionPreProcessor> preProcessorList);
	
	public void setPostProcessors(List<ExecutionPostProcessor> preProcessorList);
	
}
