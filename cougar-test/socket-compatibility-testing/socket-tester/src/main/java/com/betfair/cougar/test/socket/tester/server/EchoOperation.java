/*
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

package com.betfair.cougar.test.socket.tester.server;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.security.SSLAwareTokenResolver;
import com.betfair.cougar.test.socket.tester.common.*;

/**
*
*/
class EchoOperation extends ClientAuthExecutable {

    EchoOperation(boolean needsClientAuth) {
        super(needsClientAuth);
    }

    @Override
    public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
        if (key.equals(Common.echoOperationDefinition.getOperationKey())) {
            if (!checkClientAuth(ctx,observer))
            {
                return;
            }
            EchoResponse response = new EchoResponse();
            response.setMessage((String) args[0]);
            try {
                ExecutionContextTO responseCtx = (ExecutionContextTO) Conversion.convert(ctx, ExecutionContext.class, ExecutionContextTO.class);
                response.setExecutionContext(responseCtx);
                observer.onResult(new ExecutionResult(response));
            }
            catch (Exception e) {
                observer.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.ServiceRuntimeException,"error",e)));
            }
        }
        else if (key.equals(Common.echoFailureOperationDefinition.getOperationKey())) {
            if (!checkClientAuth(ctx,observer))
            {
                return;
            }
            EchoException ee = new EchoException(EchoExceptionErrorCodeEnum.GENERIC, (String)args[0]);
            observer.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.ServiceCheckedException,"error",ee)));
        }
        else {
            observer.onResult(new ExecutionResult(new CougarFrameworkException(ServerFaultCode.NoSuchOperation, key.toString())));
        }
    }
}
