package com.betfair.cougar.transport.impl;

import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 * Utility to aid registering of DehydratedExecutionContextFactorys.
 */
public class DehydratedExecutionContextFactoryRegisterer {
    private DehydratedExecutionContextResolution resolution;
    private DehydratedExecutionContextResolverFactory factory;
    private boolean enabled;

    public DehydratedExecutionContextFactoryRegisterer(DehydratedExecutionContextResolution resolution, DehydratedExecutionContextResolverFactory factory) {
        this(resolution, factory, true);
    }

    public DehydratedExecutionContextFactoryRegisterer(DehydratedExecutionContextResolution resolution, DehydratedExecutionContextResolverFactory factory, boolean enabled) {
        this.resolution = resolution;
        this.factory = factory;
        this.enabled = enabled;
    }

    public void init() {
        if (enabled) {
            resolution.registerFactory(factory);
        }
    }
}
