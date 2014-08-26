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

import com.betfair.cougar.core.api.ev.ClientExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.ParameterType;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for @see InvocationResponseImpl
 */
public class InvocationResponseImplTest {

    @Test
    public void testSuccess() {
        final String result = "result";

        ExecutionObserver successObserver = new ExecutionObserver() {
            @Override
            public void onResult(ExecutionResult executionResult) {
                assertTrue(executionResult.getResultType() == ExecutionResult.ResultType.Success);
                assertEquals(executionResult.getResult(), result);
                assertEquals(((ClientExecutionResult)executionResult).getResultSize(), 45);
            }
        };

        InvocationResponseImpl impl = new InvocationResponseImpl(result);
        impl.recreate(successObserver, new ParameterType(String.class, null), 45);
    }

    @Test
    public void testException() {
        final CougarException ex = new CougarServiceException(ServerFaultCode.FrameworkError, "service is down");

        ExecutionObserver exceptionObserver = new ExecutionObserver() {
            @Override
            public void onResult(ExecutionResult executionResult) {
                assertTrue(executionResult.getResultType() == ExecutionResult.ResultType.Fault);
                assertEquals(executionResult.getFault(), ex);
                assertEquals(((ClientExecutionResult)executionResult).getResultSize(), 65);
            }
        };


        InvocationResponseImpl impl = new InvocationResponseImpl(null, ex);

        impl.recreate(exceptionObserver, new ParameterType(String.class, null), 65);
    }
}
