/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.core.impl.tracing;

import com.betfair.cougar.core.api.tracing.Tracer;

/**
 * Utility to aid in registering tracer implementations from within Spring.
 */
public class CompoundTracerRegistrationHelper {

    private final CompoundTracer tracer;
    private final Tracer impl;
    private final boolean add;

    public CompoundTracerRegistrationHelper(CompoundTracer tracer, Tracer impl) {
        this.tracer = tracer;
        this.impl = impl;
        this.add = true;
    }

    public CompoundTracerRegistrationHelper(CompoundTracer tracer, Tracer impl, boolean add) {
        this.tracer = tracer;
        this.impl = impl;
        this.add = add;
    }

    public void init() {
        if (add) {
            tracer.addTracer(impl);
        }
    }
}
