/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InterceptingExecutableWrapper implements ExecutableWrapper {

	private final Executable exec;
	private final List<ExecutionPreProcessor> preExecutionInterceptorList;
	private final List<ExecutionPostProcessor> postExecutionInterceptorList;

    private Map<Thread, List<ExecutionPreProcessor>> unexecutedPreProcessorsByThread = new ConcurrentHashMap<>();

	public InterceptingExecutableWrapper(Executable exec, List<ExecutionPreProcessor> preExecutionInterceptorList, List<ExecutionPostProcessor> postExecutionInterceptorList) {
		this.exec = exec;
		this.preExecutionInterceptorList = preExecutionInterceptorList;
		this.postExecutionInterceptorList = postExecutionInterceptorList;
	}

	@Override
	public void execute(final ExecutionContext ctx, final OperationKey key, final Object[] args, final ExecutionObserver observer, final ExecutionVenue executionVenue, final TimeConstraints timeConstraints) {

        final Runnable execution = new Runnable() {
            @Override
            public void run() {
                ExecutionObserver newObserver = new PostProcessingInterceptorWrapper(observer, postExecutionInterceptorList, ctx, key, args);

                try {
                    exec.execute(
                            ctx,
                            key,
                            args,
                            newObserver,
                            executionVenue,
                            timeConstraints);

                } catch (CougarException e) {
                    newObserver.onResult(new ExecutionResult(e));
                }
                catch (Exception e) {
                    newObserver.onResult(new ExecutionResult(
                            new CougarFrameworkException(ServerFaultCode.ServiceRuntimeException, "Exception thrown by service method", e)));
                }
            }
        };

        List<ExecutionPreProcessor> unexecuted = unexecutedPreProcessorsByThread.get(Thread.currentThread());
        if (unexecuted == null) {
            // this has to be a new list since the InterceptionUtils.execute call below is allowed to (expected to even) mutate the second list passed in
            unexecuted = new ArrayList<>(preExecutionInterceptorList);
        }

        try {
            InterceptionUtils.execute(preExecutionInterceptorList, unexecuted, ExecutionRequirement.PRE_EXECUTE, execution, ctx, key, args, observer);
        }
        finally {
            unexecutedPreProcessorsByThread.remove(Thread.currentThread());
        }
    }

    @Override
    public Executable getWrappedExecutable() {
        return exec;
    }

    @Override
    public <T extends Executable> T findChild(Class<T> clazz) {
        return ExecutableWrapperUtils.findChild(clazz, this);
    }

    public void setUnexecutedPreProcessorsForThisThread(List<ExecutionPreProcessor> remainingProcessors) {
        unexecutedPreProcessorsByThread.put(Thread.currentThread(), remainingProcessors);
    }
}
