package com.betfair.cougar.core.api.tracing;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;

/**
 * SPI for providing tracing implementations.
 */
public interface Tracer {

    void start(RequestUUID uuid);

    void trace(RequestUUID uuid, String msg);
    void trace(RequestUUID uuid, String msg, Object arg1);
    void trace(RequestUUID uuid, String msg, Object arg1, Object arg2);
    void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3);
    void trace(RequestUUID uuid, String msg, Object... args);

    void trace(ExecutionContext ctx, String msg);
    void trace(ExecutionContext ctx, String msg, Object arg1);
    void trace(ExecutionContext ctx, String msg, Object arg1, Object arg2);
    void trace(ExecutionContext ctx, String msg, Object arg1, Object arg2, Object arg3);
    void trace(ExecutionContext ctx, String msg, Object... args);

    void end(RequestUUID uuid);
}
