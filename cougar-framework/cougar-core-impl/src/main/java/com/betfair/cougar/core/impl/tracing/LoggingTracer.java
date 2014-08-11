package com.betfair.cougar.core.impl.tracing;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.logging.CougarLoggingUtils;

/**
 * Simple tracer implementation which writes trace messages to a trace log.
 */
public class LoggingTracer extends AbstractTracer {

    @Override
    public void start(RequestUUID uuid) {
        // no-op
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, arg1);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, arg1, arg2);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, arg1, arg2, arg3);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, args);
    }

    @Override
    public void end(RequestUUID uuid) {
        // no-op
    }
}
