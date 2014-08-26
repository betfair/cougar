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

package com.betfair.cougar.core.api.ev.processors;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.OperationKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.*;

public class PreMatchingInterceptorTest {

    private ExecutionPreProcessor processor;
    private PreMatchingInterceptor matchingInterceptor;
    private Matcher matcher;
    private OperationKey key;

    @Before
    public void setup() {
    	processor = mock(ExecutionPreProcessor.class);
        matchingInterceptor = new PreMatchingInterceptor(processor);
        key = new OperationKey(new ServiceVersion(1, 2), "serviceName", "operationName", OperationKey.Type.Request);
        matcher = mock(Matcher.class);
        matchingInterceptor.setMatcher(matcher);
    }

    @Test
    public void invokedWhenMatcherNotSet() {
    	matchingInterceptor.setMatcher(null);
    	matchingInterceptor.invoke(null, key, null);
        verify(processor).invoke(null, key, null);
    }

    @Test
    public void invokedWhenMatcherMatches() {
        when(matcher.matches(Matchers.<ExecutionContext>any(), Matchers.<OperationKey>any(), Matchers.<Object[]>any())).thenReturn(true);
    	matchingInterceptor.invoke(null, key, null);
        verify(processor).invoke(null, key, null);
    }

    @Test
    public void notInvokedWhenMatcherDoesntMatch() {
        when(matcher.matches(Matchers.<ExecutionContext>any(), Matchers.<OperationKey>any(), Matchers.<Object[]>any())).thenReturn(false);
    	matchingInterceptor.invoke(null, key, null);
        verify(processor, never()).invoke(null, key, null);
    }
}