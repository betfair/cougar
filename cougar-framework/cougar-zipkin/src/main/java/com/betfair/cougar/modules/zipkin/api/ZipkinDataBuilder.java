package com.betfair.cougar.modules.zipkin.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ZipkinDataBuilder {

    @Nonnull
    ZipkinDataBuilder traceId(long traceId);

    @Nonnull
    ZipkinDataBuilder spanId(long spanId);

    @Nonnull
    ZipkinDataBuilder parentSpanId(@Nullable Long parentSpanId);

    @Nonnull
    ZipkinDataBuilder spanName(@Nonnull String spanName);

    @Nonnull
    ZipkinDataBuilder port(short port);

    @Nonnull
    ZipkinDataBuilder flags(Long flags);

    @Nonnull
    ZipkinData build();
}
