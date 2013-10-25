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

package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;

import java.util.List;
import java.util.logging.Level;

public class InterceptingExecutableWrapper implements Executable {
	private static final InterceptorResult CONTINUE = new InterceptorResult(InterceptorState.CONTINUE);
	private final static CougarLogger logger = CougarLoggingUtils.getLogger(InterceptingExecutableWrapper.class);
	
	private final Executable exec;
	private final List<ExecutionPreProcessor> preExecutionInterceptorList;
	private final List<ExecutionPostProcessor> postExecutionInterceptorList;

	public InterceptingExecutableWrapper(Executable exec, List<ExecutionPreProcessor> preExecutionInterceptorList, List<ExecutionPostProcessor> postExecutionInterceptorList) {
		this.exec = exec;
		this.preExecutionInterceptorList = preExecutionInterceptorList;
		this.postExecutionInterceptorList = postExecutionInterceptorList;
	}
	
	@Override
	public void execute(ExecutionContext ctx, OperationKey key, Object[] args,
			ExecutionObserver observer, ExecutionVenue executionVenue) {

		InterceptorResult result = invokePreProcessingInterceptors(ctx, key, args);
		
		/**
		 * Pre-processors can force ON_EXCEPTION or ON_RESULT without execution.
		 * The shouldInvoke will indicate whether actual invocation should take place.
		 */
		if (result.getState().shouldInvoke()) {
			observer = new PostProcessingInterceptorWrapper(observer, postExecutionInterceptorList, ctx, key, args);

			try {
				exec.execute(
						ctx, 
						key,
						args, 
						observer, 
						executionVenue);
			
			} catch (CougarException e) {
				observer.onResult(new ExecutionResult(e));
			}
			 catch (Exception e) {
				observer.onResult(new ExecutionResult(
						new CougarServiceException(ServerFaultCode.ServiceRuntimeException, 
								"Exception thrown by service method",
								e)));
			}
		} else {
			if (InterceptorState.FORCE_ON_EXCEPTION.equals(result.getState())) {
                Object interceptorResult = result.getResult();
                ExecutionResult executionResult;
                if (interceptorResult instanceof CougarException) {
                    executionResult = new ExecutionResult((CougarException)interceptorResult);
                } else if (interceptorResult instanceof CougarApplicationException) {
                    executionResult = new ExecutionResult((CougarApplicationException)interceptorResult);
                } else if (result.getResult() instanceof Exception) {
                    executionResult = new ExecutionResult(
                            new CougarServiceException(ServerFaultCode.ServiceRuntimeException,
                                    "Interceptor forced exception", (Exception)result.getResult()));
                } else {
                    // onException forced, but result is not an exception
                    executionResult = new ExecutionResult(
                            new CougarServiceException(ServerFaultCode.ServiceRuntimeException,
                                    "Interceptor forced exception, but result was not an exception - I found a " +
                                            result.getResult()));
                }
                observer.onResult(executionResult);

			} else if (InterceptorState.FORCE_ON_RESULT.equals(result.getState())) {
				observer.onResult(new ExecutionResult(result.getResult()));
			}
		}
	}

	private InterceptorResult invokePreProcessingInterceptors(ExecutionContext ctx, OperationKey key, Object[] args) {
		InterceptorResult result = CONTINUE;
		
		for (ExecutionPreProcessor pre : preExecutionInterceptorList) {
			try {
				result = pre.invoke(ctx, key, args);
				if (result == null || result.getState() == null) {
					// defensive
					throw new IllegalStateException(pre.getName() +" did not return a valid InterceptorResult");
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Pre Processor " + pre.getName() + " has failed.");
				logger.log(e);
				result = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, e);
				break;
			}
			if (result.getState().shouldAbortInterceptorChain()) {
				break;
			}
		}
		return result;
	}

    Executable getExec() {
        return exec;
    }
}
