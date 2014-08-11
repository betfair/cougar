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
