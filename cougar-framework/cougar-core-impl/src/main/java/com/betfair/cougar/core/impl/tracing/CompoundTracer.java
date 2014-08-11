package com.betfair.cougar.core.impl.tracing;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.tracing.Tracer;

import java.util.ArrayList;
import java.util.List;

/**
 * Compound tracer, enables running with 0 or more tracers.
 */
public class CompoundTracer extends AbstractTracer {
    private List<Tracer> tracers = new ArrayList<>();

    @Override
    public void start(RequestUUID uuid) {
        for (Tracer t : tracers) {
            t.start(uuid);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, arg1);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, arg1, arg2);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, arg1, arg2, arg3);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, args);
        }
    }

    @Override
    public void end(RequestUUID uuid) {
        for (Tracer t : tracers) {
            t.end(uuid);
        }
    }

    public void addTracer(Tracer impl) {
        tracers.add(impl);
    }
}
