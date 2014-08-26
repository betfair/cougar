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
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionRequirement;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationKey;

/**
 * If the  provided Matcher does in fact match the inbound invocation, the
 * provided ExecutionPreProcessor is invoked, otherwise InterceptorState.CONTINUE is returned.
 *
 * If no Matcher is provided, ALL inbound requests will be deemed to match
 * (which is ok as long as you intend to add a matcher later, otherwise there is no need to use this interceptor!)
 */
public class PreMatchingInterceptor implements ExecutionPreProcessor {

    private Matcher matcher;
    private ExecutionPreProcessor preProcessor;

    public PreMatchingInterceptor(ExecutionPreProcessor preProcessor) {
        if (preProcessor == null) {
            throw new IllegalArgumentException("preProcessor cannot be null");
        }
        this.preProcessor = preProcessor;
    }

    @Override
    public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
        if (matcher == null || matcher.matches(ctx, key, args)) {
            return preProcessor.invoke(ctx, key, args);
        }
        return new InterceptorResult(InterceptorState.CONTINUE);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName()+" ["+preProcessor+"] ["+matcher+"]";
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public ExecutionRequirement getExecutionRequirement() {
        return preProcessor.getExecutionRequirement();
    }
}
