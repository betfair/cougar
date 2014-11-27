package com.betfair.cougar.core.impl.tracing.zipkin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component(ZipkinConfig.BEAN_NAME)
@ManagedResource(description = "Zipkin tracing config", objectName = "Cougar:name=ZipkinConfig")
public class ZipkinConfig {
    public static final String BEAN_NAME = "zipkinConfig";

    private static final int MAX_LEVEL = 1000;
    private static final int MIN_LEVEL = 0;

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    @Value("$COUGAR_ZIPKIN{zipkin.tracing.level}")
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
        }
    }
}
