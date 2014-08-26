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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionRequirement;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.util.ValidationUtils;

public class MandatoryCheckInterceptor implements ExecutionPreProcessor {

	private ExecutionVenue bev;
	private static final InterceptorResult SUCCESS = new InterceptorResult(InterceptorState.CONTINUE, null);
	private static final String NAME = "Mandatory Parameter Check Interceptor";

	public MandatoryCheckInterceptor(ExecutionVenue bev) {
		this.bev = bev;
	}

	@Override
	public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
		OperationDefinition operationDefinition = bev.getOperationDefinition(key);
		Parameter[] parms = operationDefinition.getParameters();

		for (int i = 0 ; i < args.length; i++) {

		    if (args[i] == null) {
	            if (parms[i].isMandatory()) {
	            	return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
	            			new CougarValidationException(ServerFaultCode.MandatoryNotDefined,
	            					"Mandatory attributes not defined for parameter '"+parms[i].getName() + "'"));
	            }
	        } else {
                try {
                    ValidationUtils.validateMandatory(args[i]);
                } catch (IllegalArgumentException e) {
                    return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
                            new CougarValidationException(ServerFaultCode.MandatoryNotDefined,
                                    "Embedded fields not defined for parameter '"+parms[i].getName() + "'", e));
                }
	        }
		}
		return SUCCESS;
	}

	public String getName() {
		return NAME;
	}

    @Override
    public ExecutionRequirement getExecutionRequirement() {
        return ExecutionRequirement.EXACTLY_ONCE;
    }
}
