package com.betfair.cougar.modules.zipkin.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ZipkinData {

    @Nonnull
    Long getTraceId();

    /**
     * Represents Zipkin's current span name.
     *
     * @return String representing the name of this current span (typically service or host name).
     */
    @Nonnull
    Long getSpanId();

    @Nullable
    Long getParentSpanId();

    @Nonnull
    String getSpanName();

    short getPort();
}
