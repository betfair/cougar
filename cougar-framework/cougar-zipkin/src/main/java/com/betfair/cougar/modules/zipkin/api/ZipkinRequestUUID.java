package com.betfair.cougar.modules.zipkin.api;

import com.betfair.cougar.api.RequestUUID;

import javax.annotation.Nonnull;

/**
 * ZipkinRequestUUID extends a RequestUUID with Zipkin-related data.
 *
 * @see com.betfair.cougar.api.RequestUUID
 */
public interface ZipkinRequestUUID extends RequestUUID {

    /**
     * Obtains Zipkin data if the object was already created.
     *
     * @return ZipkinData object or null if Zipkin is not enabled for this request.
     * @throws IllegalStateException if the ZipkinData object isn't yet finalized (isZipkinTracingReady == false) or
     *                               if Zipkin tracing is not enabled for this request (isZipkinTracingEnabled == false).
     */
    @Nonnull
    ZipkinData getZipkinData();

    /**
     * States whether zipkin tracing is enabled for this request or not.
     *
     * @return whether Zipkin tracing is enabled for this request or not
     */
    boolean isZipkinTracingEnabled();

    /**
     * States whether Zipkin tracing is ready for this request or not. Zipkin tracing becomes ready once the underlying
     * ZipkinData becomes available (i.e. once the object holding Zipkin data for this request has been created).
     *
     * @return whether Zipkin tracing is ready for this request or not
     */
    boolean isZipkinTracingReady();


    /**
     * Builds ZipkinData for this request, tagging the span with a span name.
     * <p/>
     * Note: We need this because we only have the span name after the ZipkinHttpRequestUuidResolver pointcut.
     *
     * @param spanName The name of the span
     * @return the newly-built ZipkinData
     */
    ZipkinData buildZipkinData(@Nonnull String spanName);
}
