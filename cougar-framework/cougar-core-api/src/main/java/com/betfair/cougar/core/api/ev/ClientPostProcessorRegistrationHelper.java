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

import java.util.List;

/**
 *
 */
public class ClientPostProcessorRegistrationHelper  {
    //
    private List<ExecutionPostProcessor> referenceInterceptorList;

    private List<ExecutionPostProcessor> interceptorList;

    private ExecutionPostProcessor interceptor;


    public void setInterceptor(ExecutionPostProcessor interceptor) {
        this.interceptor = interceptor;
    }

    public void setInterceptorList(List<ExecutionPostProcessor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    public void setReferenceInterceptorList(List<ExecutionPostProcessor> referenceInterceptorList) {
        this.referenceInterceptorList = referenceInterceptorList;
    }

    public void register() {
        if (interceptor != null) {
            referenceInterceptorList.add(0, interceptor);
        }
        if (interceptorList != null) {
            for (ExecutionPostProcessor epp : interceptorList) {
                referenceInterceptorList.add(0, epp);
            }
        }
    }
}
