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
