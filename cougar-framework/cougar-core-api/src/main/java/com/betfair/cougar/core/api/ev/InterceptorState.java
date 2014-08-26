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

public enum InterceptorState {
	CONTINUE(true, false),
	FORCE_ON_EXCEPTION (false, true),
	FORCE_ON_RESULT(false, true);

	private boolean shouldInvoke;
	private boolean shouldAbortInterceptorChain;

	private InterceptorState(boolean shouldInvoke, boolean shouldAbortInterceptorChain) {
		this.shouldInvoke = shouldInvoke;
		this.shouldAbortInterceptorChain = shouldAbortInterceptorChain;
	}

    /**
     * Returns whether or not the Execution venue should go on to execute the operation.
     * It has no meaning whatsoever for Post Process Interceptors
     * @return
     */
	public boolean shouldInvoke() {
		return shouldInvoke;
	}

	public boolean shouldAbortInterceptorChain() {
		return shouldAbortInterceptorChain;
	}

}
