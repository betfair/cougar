package com.betfair.cougar.transport.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Handy base class for resolvers which handle only a single component.
 */
public abstract class SingleComponentResolver<Transport,Body> implements DehydratedExecutionContextResolver<Transport,Body> {
    private final DehydratedExecutionContextComponent component;

    protected SingleComponentResolver(DehydratedExecutionContextComponent component) {
        this.component = component;
    }

    @Override
    public void resolving(Set<DehydratedExecutionContextComponent> handling) {
        if (handling.size() > 1 || (handling.size() == 0 && !handling.contains(component))) {
            Set<DehydratedExecutionContextComponent> cantHandle = new HashSet<>(handling);
            cantHandle.remove(component);
            throw new IllegalArgumentException("I don't know how to handle: "+cantHandle);
        }
    }

    @Override
    public DehydratedExecutionContextComponent[] supportedComponents() {
        return new DehydratedExecutionContextComponent[] { component };
    }
}