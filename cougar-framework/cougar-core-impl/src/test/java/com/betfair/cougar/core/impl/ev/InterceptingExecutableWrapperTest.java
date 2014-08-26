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
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import org.slf4j.LoggerFactory;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for InterceptingExecutableWrapper
 */

public class InterceptingExecutableWrapperTest {
    private List<ExecutionPreProcessor> preExecutionInterceptorList = new ArrayList<ExecutionPreProcessor>();
    private List<ExecutionPostProcessor> postExecutionInterceptorList = new ArrayList<ExecutionPostProcessor>();

    private final InterceptorResult SUCCESS = new InterceptorResult(InterceptorState.CONTINUE);
    private final InterceptorResult FAILURE_UNCHECKED = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, new CougarFrameworkException(ServerFaultCode.ServiceRuntimeException, "Broken"));
    private final InterceptorResult FAILURE_CHECKED = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, new CougarFrameworkException(ServerFaultCode.ServiceCheckedException, "Broken"));

    private Executable executable;
    private ExecutionContext ctx;
    private ExecutionObserver observer;
    private ExecutionVenue ev;
    private OperationKey key;

    @Before
    public void before() {
        preExecutionInterceptorList.clear();
        postExecutionInterceptorList.clear();

        executable = Mockito.mock(Executable.class);
        ctx = Mockito.mock(ExecutionContext.class);
        observer = Mockito.mock(ExecutionObserver.class);
        ev = Mockito.mock(ExecutionVenue.class);
        key = Mockito.mock(OperationKey.class);
    }



    @Test
    public void testPreInterceptorHappyCase() {
        ExecutionPreProcessor preIntercerptor = Mockito.mock(ExecutionPreProcessor.class);
        when(preIntercerptor.getExecutionRequirement()).thenReturn(ExecutionRequirement.EXACTLY_ONCE);
        when(preIntercerptor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(SUCCESS);

        preExecutionInterceptorList.add(preIntercerptor);

        InterceptingExecutableWrapper executableWrapper = new InterceptingExecutableWrapper(executable, preExecutionInterceptorList, postExecutionInterceptorList);
        executableWrapper.execute(ctx, key, new Object[]{}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        verify(preIntercerptor).invoke(eq(ctx), eq(key), any(Object[].class));

        verify(executable).execute(eq(ctx), eq(key), any(Object[].class), any(ExecutionObserver.class), eq(ev), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
    }

    @Test
    public void testPreInterceptorsOneGoodOneBadUnchecked() {
        ExecutionPreProcessor preIntercerptorGood = Mockito.mock(ExecutionPreProcessor.class);
        when(preIntercerptorGood.getExecutionRequirement()).thenReturn(ExecutionRequirement.EXACTLY_ONCE);
        when(preIntercerptorGood.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(SUCCESS);

        ExecutionPreProcessor preIntercerptorBad = Mockito.mock(ExecutionPreProcessor.class);
        when(preIntercerptorBad.getExecutionRequirement()).thenReturn(ExecutionRequirement.EXACTLY_ONCE);
        when(preIntercerptorBad.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(FAILURE_UNCHECKED);

        preExecutionInterceptorList.add(preIntercerptorGood);
        preExecutionInterceptorList.add(preIntercerptorBad);


        InterceptingExecutableWrapper executableWrapper = new InterceptingExecutableWrapper(executable, preExecutionInterceptorList, postExecutionInterceptorList);
        executableWrapper.execute(ctx, key, new Object[]{}, observer, ev,DefaultTimeConstraints.NO_CONSTRAINTS);

        //Firstly check that the first good one worked correctly
        verify(preIntercerptorGood).invoke(eq(ctx), eq(key), any(Object[].class));


        //For the second failing interceptor
        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());
        ExecutionResult result = executionResultArgumentCaptor.getValue();
        assertTrue(result.isFault());
        CougarException ex = result.getFault();
        assertNotNull(ex);
        assertEquals(ex.getServerFaultCode(), ServerFaultCode.ServiceRuntimeException);

        //Check that the ev WAS NOT called - should never happen
        verify(ev, never()).execute(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class), any(ExecutionObserver.class), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
    }

    @Test
    public void testPreInterceptorsOneBadCheckedOneGood() {
        ExecutionPreProcessor preIntercerptorBad = Mockito.mock(ExecutionPreProcessor.class);
        when(preIntercerptorBad.getExecutionRequirement()).thenReturn(ExecutionRequirement.EXACTLY_ONCE);
        when(preIntercerptorBad.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(FAILURE_CHECKED);

        ExecutionPreProcessor preIntercerptorGood = Mockito.mock(ExecutionPreProcessor.class);
        when(preIntercerptorGood.getExecutionRequirement()).thenReturn(ExecutionRequirement.EXACTLY_ONCE);

        preExecutionInterceptorList.add(preIntercerptorBad);
        preExecutionInterceptorList.add(preIntercerptorGood);

        InterceptingExecutableWrapper executableWrapper = new InterceptingExecutableWrapper(executable, preExecutionInterceptorList, postExecutionInterceptorList);
        executableWrapper.execute(ctx, key, new Object[]{}, observer, ev,DefaultTimeConstraints.NO_CONSTRAINTS);

        //For the second failing interceptor
        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());
        ExecutionResult result = executionResultArgumentCaptor.getValue();
        assertTrue(result.isFault());
        CougarException ex = result.getFault();
        assertNotNull(ex);
        assertEquals(ex.getServerFaultCode(), ServerFaultCode.ServiceCheckedException);

        //Confirm also that the second interceptor WAS NOT called
        verify(preIntercerptorGood, never()).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));

        //Check that the ev WAS NOT called - should never happen
        verify(ev, never()).execute(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class), any(ExecutionObserver.class), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
    }

    @Test
    public void testPostInterceptorHappyCase() {
        postExecutionInterceptorList.add(epp);

        InterceptingExecutableWrapper executableWrapper = new InterceptingExecutableWrapper(new Executable() {
            @Override
            public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
                assertTrue("Observer should be wrapped by PostProcessorInterceptorWrapper", observer instanceof PostProcessingInterceptorWrapper);

                PostProcessingInterceptorWrapper ppiw = (PostProcessingInterceptorWrapper) observer;
                ppiw.onResult(new ExecutionResult());
            }
        }, preExecutionInterceptorList, postExecutionInterceptorList);
        executableWrapper.execute(ctx, key, new Object[]{Boolean.TRUE}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());

        ExecutionResult executionResult = executionResultArgumentCaptor.getValue();
        assertNotNull(executionResult);
        assertFalse(executionResult.isFault());
    }

    @Test
    public void testPostInterceptorOnException() {
        postExecutionInterceptorList.add(epp);

        InterceptingExecutableWrapper executableWrapper = new InterceptingExecutableWrapper(new Executable() {
            @Override
            public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
                assertTrue("Observer should be wrapped by PostProcessorInterceptorWrapper", observer instanceof PostProcessingInterceptorWrapper);

                PostProcessingInterceptorWrapper ppiw = (PostProcessingInterceptorWrapper) observer;
                ppiw.onResult(new ExecutionResult());
            }
        }, preExecutionInterceptorList, postExecutionInterceptorList);
        executableWrapper.execute(ctx, key, new Object[]{Boolean.FALSE}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());

        ExecutionResult executionResult = executionResultArgumentCaptor.getValue();
        assertNotNull(executionResult);
        assertTrue(executionResult.isFault());
        CougarException ce = executionResult.getFault();
        assertNotNull(ce);
        assertEquals(ce.getServerFaultCode(), ServerFaultCode.ServiceRuntimeException);
    }


    private ExecutionPostProcessor epp = new ExecutionPostProcessor() {
        @Override
        public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionResult result) {
            Boolean b = (Boolean)args[0];
            if (b) {
                return SUCCESS;
            } else {
                return FAILURE_UNCHECKED;
            }
        }

        @Override
        public String getName() {
            return "epp-test";
        }
    };


}
