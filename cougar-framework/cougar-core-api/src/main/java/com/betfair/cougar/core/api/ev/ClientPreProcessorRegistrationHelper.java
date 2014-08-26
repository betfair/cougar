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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;

import java.util.List;

/**
 * Helper to let consumers register a pre-invocation interceptor
 */
public class ClientPreProcessorRegistrationHelper {

    private List<ExecutionPreProcessor> referenceInterceptorList;

    private ExecutionPreProcessor interceptor;

    private List<ExecutionPreProcessor> interceptorList;

    public void setInterceptorList(List<ExecutionPreProcessor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    public void setInterceptor(ExecutionPreProcessor interceptor) {
        this.interceptor = interceptor;
    }

    public void setReferenceInterceptorList(List<ExecutionPreProcessor> referenceInterceptorList) {
        this.referenceInterceptorList = referenceInterceptorList;
    }

    public void register() {
        if (interceptor != null) {
            referenceInterceptorList.add(interceptor);
        }
        if (interceptorList != null) {
            for (ExecutionPreProcessor epp : interceptorList) {
                referenceInterceptorList.add(epp);
            }
        }
    }
}
