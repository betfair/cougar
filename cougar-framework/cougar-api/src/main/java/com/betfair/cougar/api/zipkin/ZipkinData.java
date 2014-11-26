package com.betfair.cougar.api.zipkin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ZipkinData {

    @Nonnull
    public Long getTraceId();

    /**
     * Represents Zipkin's current span name.
     *
     * @return String representing the name of this current span (typically service or host name).
     */
    @Nonnull
    public Long getSpanId();

    @Nullable
    public Long getParentSpanId();

    @Nonnull
    public String getSpanName();

    public short getPort();
}
