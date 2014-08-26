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
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Test case for @see PostProcessingInterceptorWrapper class
 */
public class PostProcessingInterceptorWrapperTest {
    private ExecutionContext ctx;
    private OperationKey key;
    private Subscription subscription;
    private ExecutionPostProcessor mockedExecutionPostProcessor;

    @Before
    public void init() {
        ctx = Mockito.mock(ExecutionContext.class);
        key = Mockito.mock(OperationKey.class);
        subscription = Mockito.mock(Subscription.class);
        mockedExecutionPostProcessor = Mockito.mock(ExecutionPostProcessor.class);
    }

    @Test
    public void testSubscriptionExecutionResult() {

        ExecutionObserver observer = new ExecutionObserver() {

            @Override
            public void onResult(ExecutionResult executionResult) {
                assertEquals(ExecutionResult.ResultType.Subscription, executionResult.getResultType());
                assertNotNull(executionResult.getSubscription());
            }
        };

        PostProcessingInterceptorWrapper ppiw = new PostProcessingInterceptorWrapper(observer, Collections.<ExecutionPostProcessor>emptyList(), ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(subscription));
    }

    @Test
    public void testPPWrapperWithContinue() {

        List<ExecutionPostProcessor> pp = new ArrayList<ExecutionPostProcessor>();
        pp.add(mockedExecutionPostProcessor);

        when(mockedExecutionPostProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class),
                any(Object[].class), any(ExecutionResult.class))).thenReturn(new InterceptorResult(InterceptorState.CONTINUE));

        //An interceptor result of CONTINUE means proceed without modification....

        //Pass in a successful result, and confirm that by running an interceptor with Continue against it
        //that a SUCCESS onResult occurs - eg continue didn't change the ExecutionResult state
        VerifyingExecutionObserver obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Success);
        PostProcessingInterceptorWrapper ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult("String"));

        //Pass in an exception result
        CougarFrameworkException cfe = new CougarFrameworkException("Mr Blobby");
        VerifyingExecutionObserver exceptionObserver = new VerifyingExecutionObserver(ExecutionResult.ResultType.Fault);
        ppiw = new PostProcessingInterceptorWrapper(exceptionObserver, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(cfe));

        //Pass in a subscription result and check for same
        VerifyingExecutionObserver subObserver = new VerifyingExecutionObserver(ExecutionResult.ResultType.Subscription);
        ppiw = new PostProcessingInterceptorWrapper(subObserver, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(new DefaultSubscription()));
    }


    @Test
    public void testPPWrapperWithForceException() {

        List<ExecutionPostProcessor> pp = new ArrayList<ExecutionPostProcessor>();
        pp.add(mockedExecutionPostProcessor);

        when(mockedExecutionPostProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class),
                any(Object[].class), any(ExecutionResult.class))).thenReturn(new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION));

        //Pass in a successful result, and confirm that by running an interceptor with Force Exception
        //that that is the observed result
        VerifyingExecutionObserver obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Fault);
        PostProcessingInterceptorWrapper ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult("String"));

        //Pass in an exception result
        obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Fault);
        ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(new CougarFrameworkException("Mr Blobby")));

        //Pass in a subscription result and check for exception result
        obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Fault);
        ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(new DefaultSubscription()));
    }

    @Test
    public void testPPWrapperWithForceResult() {

        List<ExecutionPostProcessor> pp = new ArrayList<ExecutionPostProcessor>();
        pp.add(mockedExecutionPostProcessor);

        String betterResult = "ILL TAKE YOUR RESULT AND TRUMP IT!";
        when(mockedExecutionPostProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class),
                any(Object[].class), any(ExecutionResult.class))).thenReturn(new InterceptorResult(InterceptorState.FORCE_ON_RESULT, betterResult));

        //Pass in a successful result, and confirm that by running an interceptor with Force Exception
        //that that is the observed result
        VerifyingExecutionObserver obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Success, betterResult);
        PostProcessingInterceptorWrapper ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult("String"));

        //Pass in an exception result
        obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Success);
        ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(new CougarFrameworkException("Mr Blobby")));

        //Pass in a subscription result and check for exception result
        obs = new VerifyingExecutionObserver(ExecutionResult.ResultType.Success);
        ppiw = new PostProcessingInterceptorWrapper(obs, pp, ctx, key, new Object[] {});
        ppiw.onResult(new ExecutionResult(new DefaultSubscription()));
    }



    private static class VerifyingExecutionObserver implements ExecutionObserver {
        private ExecutionResult.ResultType expectedResultType;
        private Object expectedResult;

        public VerifyingExecutionObserver(ExecutionResult.ResultType expectedResultType) {
            this(expectedResultType, null);
        }

        public VerifyingExecutionObserver(ExecutionResult.ResultType expectedResultType, Object expectedResult) {
            this.expectedResultType = expectedResultType;
            this.expectedResult = expectedResult;
        }


        @Override
        public void onResult(ExecutionResult executionResult) {
            assertEquals(expectedResultType, executionResult.getResultType());
            switch (executionResult.getResultType()) {
                case Subscription:
                    assertNull(executionResult.getResult());
                    assertNull(executionResult.getFault());
                    assertNotNull(executionResult.getSubscription());
                    if (expectedResult != null) {
                        assertEquals(expectedResult, executionResult.getSubscription());
                    }
                    break;
                case Fault:
                    assertNull(executionResult.getResult());
                    assertNull(executionResult.getSubscription());
                    assertNotNull(executionResult.getFault());
                    if (expectedResult != null) {
                        assertEquals(expectedResult, executionResult.getFault());
                    }
                    break;
                case Success:
                    assertNull(executionResult.getSubscription());
                    assertNull(executionResult.getFault());
                    assertNotNull(executionResult.getResult());
                    if (expectedResult != null) {
                        assertEquals(expectedResult, executionResult.getResult());
                    }
                    break;
            }
        }
    };
}
