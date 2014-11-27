package com.betfair.cougar.core.impl.tracing.zipkin;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.concurrent.ThreadLocalRandom;

@ManagedResource(description = "Zipkin tracing config", objectName = "Cougar:name=ZipkinConfig")
public class ZipkinConfig {

    private static final int MAX_LEVEL = 1000;
    private static final int MIN_LEVEL = 0;

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private int tracingLevel = 0;

    /**
     * Sampling strategy to determine whether a given request should be traced by Zipkin.
     *
     * @return true if the request should be traced by Zipkin.
     */
    public boolean shouldTrace() {
        // with short circuit so we don't go through the random generation process if the Zipkin tracing is disabled
        // (tracingLevel == 0)
        return tracingLevel > 0 && RANDOM.nextInt(MIN_LEVEL, MAX_LEVEL) < tracingLevel;
    }

    @ManagedAttribute
    public int getTracingLevel() {
        return tracingLevel;
    }

    @ManagedAttribute
    public void setTracingLevel(int tracingLevel) {
        if (tracingLevel >= MIN_LEVEL && tracingLevel <= MAX_LEVEL) {
            this.tracingLevel = tracingLevel;
        } else {
            throw new IllegalArgumentException("Tracing level " + tracingLevel + " is not in the range [" + MIN_LEVEL + ";" + MAX_LEVEL + "[");
        }
    }
}
