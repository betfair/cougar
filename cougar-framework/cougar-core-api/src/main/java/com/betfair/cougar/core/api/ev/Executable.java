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

/**
 * That which can be executed from an ExecutionVenue.
 *
 */
public interface Executable {

	/**
	 * Executes an operation
	 * @param ctx Provides contextual information for execution, such as channel id, user identity, geographical location etc.
	 * @param key Defines the operation to be executed
	 * @param args The arguments to be provided to the operation
	 * @param observer The ExecutionObserver is notified of either the result of the execution, or of an exception thrown during execution.
	 * @param executionVenue The EV that invoked this Executable. Can be useful for chaining execution.
     * @param timeConstraints Time constraints affecting this call. If null there are no constraints. This execute call MAY return a timeout fault after any constraints have been violated.
	 */
	public void execute(ExecutionContext ctx, OperationKey key, Object [] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints);

}
