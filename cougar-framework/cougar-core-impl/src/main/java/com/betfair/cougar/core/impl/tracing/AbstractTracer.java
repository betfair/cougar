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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.tracing.Tracer;

/**
 * Convenience base class for Tracer implementations.
 */
public abstract class AbstractTracer implements Tracer {

    @Override
    public void trace(ExecutionContext ctx, String msg, Object... args) {
        if (ctx.traceLoggingEnabled()) {
            trace(ctx.getRequestUUID(), msg, args);
        }
    }

    @Override
    public void trace(ExecutionContext ctx, String msg) {
        if (ctx.traceLoggingEnabled()) {
            trace(ctx.getRequestUUID(), msg);
        }
    }

    @Override
    public void trace(ExecutionContext ctx, String msg, Object arg1) {
        if (ctx.traceLoggingEnabled()) {
            trace(ctx.getRequestUUID(), msg, arg1);
        }
    }

    @Override
    public void trace(ExecutionContext ctx, String msg, Object arg1, Object arg2) {
        if (ctx.traceLoggingEnabled()) {
            trace(ctx.getRequestUUID(), msg, arg1, arg2);
        }
    }

    @Override
    public void trace(ExecutionContext ctx, String msg, Object arg1, Object arg2, Object arg3) {
        if (ctx.traceLoggingEnabled()) {
            trace(ctx.getRequestUUID(), msg, arg1, arg2, arg3);
        }
    }
}
