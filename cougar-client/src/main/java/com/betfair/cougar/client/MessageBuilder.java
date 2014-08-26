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

package com.betfair.cougar.client;

import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor.ParamSource;

/**
 * Constructs a Message object based on provided args[], Parameters and RescriptOperationBindingDescriptor.
 *
 */
public class MessageBuilder {
	public Message build(Object[] args,
			Parameter[] parameters,
			RescriptOperationBindingDescriptor operationBinding) {

		int argIndex = 0;

		Message message = new Message();

		for (Parameter param : parameters) {
			String parameterName = param.getName();

			RescriptParamBindingDescriptor paramDescriptor = operationBinding.getHttpParamBindingDescriptor(parameterName);
			ParamSource parameterSource = paramDescriptor.getSource();

			Object currentArgument = null;
			if (argIndex < args.length) {
				currentArgument = args[argIndex++];
			}

			if (param.isMandatory() && currentArgument == null) {
				throw new CougarValidationException(ServerFaultCode.MandatoryNotDefined,"MANDATORY parameter cannot be null: " + parameterName);
			}

            switch (parameterSource) {

                case BODY:
                    // need to put the argument in the request body
                    message.addRequestBody(parameterName, currentArgument);
                    break;

                case HEADER:
                    // need to put the argument in the header
                    if (currentArgument != null) {
                        message.addHeaderParm(parameterName, currentArgument.toString());
                    }
                    break;

                case QUERY:
                    // Need to put the argument in the query string for this
                    // parameter
                    if (currentArgument != null) {
                        message.addQueryParm(parameterName, currentArgument);
                    }
                    break;

                default:
                    throw new UnsupportedOperationException(parameterSource + " is not supported");
            }
		}
		return message;
	}
}