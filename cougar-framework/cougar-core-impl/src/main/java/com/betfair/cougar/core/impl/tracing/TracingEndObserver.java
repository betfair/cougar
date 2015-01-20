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

package com.betfair.cougar.core.impl.tracing;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.tracing.Tracer;

/**
*
*/
public class TracingEndObserver implements ExecutionObserver {
    private final Tracer tracer;
    private final ExecutionObserver obs;
    private final RequestUUID parentUuid;
    private final RequestUUID callUuid;
    private final OperationKey key;

    public TracingEndObserver(Tracer tracer, ExecutionObserver obs, RequestUUID parentUuid, RequestUUID callUuid, OperationKey key) {
        this.tracer = tracer;
        this.obs = obs;
        this.parentUuid = parentUuid;
        this.callUuid = callUuid;
        this.key = key;
    }

    @Override
    public void onResult(ExecutionResult executionResult) {
        tracer.endCall(parentUuid, callUuid, key);
        obs.onResult(executionResult);
    }

    public Tracer getTracer() {
        return tracer;
    }

    public ExecutionObserver getObs() {
        return obs;
    }

    public RequestUUID getParentUuid() {
        return parentUuid;
    }

    public RequestUUID getCallUuid() {
        return callUuid;
    }

    public OperationKey getKey() {
        return key;
    }
}
