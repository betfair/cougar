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

package com.betfair.cougar.core.impl.ev;

import java.util.List;
import java.util.logging.Level;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostProcessingInterceptorWrapper implements ExecutionObserver {

	private static final InterceptorResult CONTINUE = new InterceptorResult(InterceptorState.CONTINUE);

	private final static Logger LOGGER = LoggerFactory.getLogger(PostProcessingInterceptorWrapper.class);
	private ExecutionObserver observer;
	private List<ExecutionPostProcessor> postProcessors;
	private ExecutionContext ctx;
	private OperationKey key;
	private Object[] args;

	public PostProcessingInterceptorWrapper(	ExecutionObserver observer,
									List<ExecutionPostProcessor> postProcessors,
									final ExecutionContext ctx,
									final OperationKey key,
									final Object [] args) {//NOSONAR

		this.observer = observer;
		this.postProcessors = postProcessors;
		this.ctx = ctx;
		this.key = key;
		this.args = args;
	}


    @Override
    public void onResult(ExecutionResult executionResult) {
        InterceptorResult state = invokePostProcessors(executionResult);
        if (InterceptorState.FORCE_ON_RESULT.equals(state.getState())) {
            observer.onResult(new ExecutionResult(state.getResult()));
        } else if (InterceptorState.FORCE_ON_EXCEPTION.equals(state.getState())) {
            forceOnException(state);
        } else {
            observer.onResult(executionResult);
        }
    }

	private void forceOnException(InterceptorResult result) {
        Object interceptorResult = result.getResult();
        ExecutionResult executionResult;
		if (interceptorResult instanceof CougarException) {
			executionResult = new ExecutionResult(interceptorResult);
        } else if (interceptorResult instanceof CougarApplicationException) {
            executionResult = new ExecutionResult(interceptorResult);
		} else if (result.getResult() instanceof Exception) {
			executionResult = new ExecutionResult(
                    new CougarFrameworkException(ServerFaultCode.ServiceRuntimeException,
                            "Interceptor forced exception", (Exception)result.getResult()));
		} else {
			// onException forced, but result is not an exception
            executionResult = new ExecutionResult(
                    new CougarFrameworkException(ServerFaultCode.ServiceRuntimeException,
                            "Interceptor forced exception, but result was not an exception - I found a " +
                                    result.getResult()));
		}
		observer.onResult(executionResult);
	}

	private InterceptorResult invokePostProcessors(ExecutionResult executionResult) {

		InterceptorResult result = CONTINUE;

		for (ExecutionPostProcessor postProcessor : postProcessors) {
			try {
				result = postProcessor.invoke(ctx, key, args, executionResult);
				if (result == null || result.getState() == null) {
					// defensive
					String detail = "Post Processor " + postProcessor.getName() + " did not return a valid InterceptorResult.";
                    LOGGER.error(detail);
					result = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, new IllegalStateException(detail));
					break;
				}
				if (result.getState().shouldAbortInterceptorChain()) {
					break;
				}

			} catch (Exception e) {
				LOGGER.error("Post Processor " + postProcessor.getName() + " has failed.", e);
				result = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, e);
				break;
			}
		}

		return result;
	}
}
