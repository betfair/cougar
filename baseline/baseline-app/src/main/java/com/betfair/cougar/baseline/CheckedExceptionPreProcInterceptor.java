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

package com.betfair.cougar.baseline;

import com.betfair.baseline.v2.enumerations.PreOrPostInterceptorException;
import com.betfair.baseline.v2.enumerations.SimpleExceptionErrorCodeEnum;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionRequirement;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationKey;

/**
 * This interceptor has just one role - if the appropriate opkey comes along with the correct argument set then
 * it will throw a SimpleException ...
 */
public class CheckedExceptionPreProcInterceptor implements ExecutionPreProcessor {
    @Override
    public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
        InterceptorResult result;

        if (key.getOperationName().equals("interceptorCheckedExceptionOperation") &&
            ((PreOrPostInterceptorException)args[0]) == PreOrPostInterceptorException.PRE) {

            result = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
                    new SimpleException(ResponseCode.BadRequest,
                            SimpleExceptionErrorCodeEnum.GENERIC,
                            "An anticipated pre-execution BSIDL defined checked exception"));
        } else {
            result = new InterceptorResult(InterceptorState.CONTINUE);
        }
        return result;
    }

    @Override
    public String getName() {
        return "Checked Exception Preprocessing interceptor";
    }

    @Override
    public ExecutionRequirement getExecutionRequirement() {
        return ExecutionRequirement.PRE_EXECUTE;
    }
}
