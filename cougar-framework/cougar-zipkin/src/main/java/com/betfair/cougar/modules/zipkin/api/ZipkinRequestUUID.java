package com.betfair.cougar.modules.zipkin.api;

import com.betfair.cougar.api.RequestUUID;

import javax.annotation.Nonnull;

public interface ZipkinRequestUUID extends RequestUUID {

    /**
     * Obtain Zipkin data if the object was already created.
     * @return ZipkinData object or null if Zipkin is not enabled for this request.
     * @throws IllegalStateException if the ZipkinData object isn't yet finalized (isZipkinTracingReady == false) or
     * if Zipkin tracing is not enabled for this request (isZipkinTracingEnabled == false).
     */
    @Nonnull
    ZipkinData getZipkinData();

    boolean isZipkinTracingEnabled();

    boolean isZipkinTracingReady();

    // We need this because we only have the span name after the ZipkinHttpRequestUuidResolver pointcut
    ZipkinData buildZipkinData(@Nonnull String spanName);
}
