package com.betfair.cougar.api.zipkin;

import com.betfair.cougar.api.RequestUUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ZipkinRequestUUID extends RequestUUID {

    /**
     * Obtain Zipkin data if the object was already created.
     * @return ZipkinData object or null if Zipkin is not enabled for this request.
     * @throws IllegalStateException if the ZipkinData object isn't yet finalized or if Zipkin tracing is not enabled for this request.
     */
    @Nonnull
    ZipkinData getZipkinData();

    boolean isZipkinTracingEnabled();

    void setZipkinSpanName(@Nonnull String spanName);
}
