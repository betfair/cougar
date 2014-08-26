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

package com.betfair.cougar.transport.socket;

import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.ev.ClientExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;

public class InvocationResponseImpl implements InvocationResponse {
	private final Object result;
	private final CougarException exception;

    public InvocationResponseImpl(final Object result) {
        this(result, null);
    }

	public InvocationResponseImpl(final Object result, final CougarException exception) {
		this.result = result;
		this.exception = exception;
	}

	public void recreate(ExecutionObserver observer, ParameterType returnType, long size) {
		if (exception!=null) {
			observer.onResult(new ClientExecutionResult(exception, size));
		} else {
            if (returnType.getImplementationClass().equals(EnumWrapper.class)) {
                observer.onResult(new ClientExecutionResult(new EnumWrapper(returnType.getComponentTypes()[0].getImplementationClass(), (String)result), size));
            }
            else {
			    observer.onResult(new ClientExecutionResult(result, size));
            }
		}
	}

	public boolean isSuccess() {
		return exception == null;
	}

	public Object getResult() {
		return result;
	}

	public CougarException getException() {
		return exception;
	}
}
