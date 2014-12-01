package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ManagedResource(description = "Zipkin tracing config", objectName = "Cougar:name=ZipkinManager")
public class ZipkinManager {

    public static final String TRACE_ID_KEY = "X-Trace";
    public static final String SPAN_ID_KEY = "X-Span";
    public static final String PARENT_SPAN_ID_KEY = "X-Parent-Span";

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

    public ZipkinRequestUUID createNewZipkinRequestUUID(@Nonnull RequestUUID cougarUuid, @Nullable String traceId,
                                                        @Nullable String spanId, @Nullable String parentSpanId) {
        Objects.requireNonNull(cougarUuid);

        ZipkinDataBuilder zipkinDataBuilder;

        if (traceId != null && spanId != null) {
            // a request with the fields is always traceable so we always propagate the tracing to the following calls

            zipkinDataBuilder = new ZipkinDataImpl.Builder()
                    .traceId(Long.valueOf(traceId))
                    .spanId(Long.valueOf(spanId))
                    .parentSpanId(parentSpanId == null ? null : Long.valueOf(parentSpanId));

        } else {

            if (shouldTrace()) {
                // starting point, we need to generate the ids if this request is to be sampled - we are the root

                UUID uuid = UUID.randomUUID();
                zipkinDataBuilder = new ZipkinDataImpl.Builder()
                        .traceId(uuid.getLeastSignificantBits())
                        .spanId(uuid.getMostSignificantBits())
                        .parentSpanId(null);

            } else {
                // otherwise leave them as null - this means Zipkin tracing will be disabled for this request
                zipkinDataBuilder = null;
            }

        }

        return new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);
    }
}
