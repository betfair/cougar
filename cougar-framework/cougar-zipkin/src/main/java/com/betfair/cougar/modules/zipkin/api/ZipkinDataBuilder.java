package com.betfair.cougar.modules.zipkin.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ZipkinDataBuilder {

    @Nonnull
    ZipkinDataBuilder traceId(@Nonnull Long traceId);

    @Nonnull
    ZipkinDataBuilder spanId(@Nonnull Long spanId);

    @Nonnull
    ZipkinDataBuilder parentSpanId(@Nullable Long parentSpanId);

    @Nonnull
    ZipkinDataBuilder spanName(@Nonnull String spanName);

    @Nonnull
    ZipkinDataBuilder port(short port);

    @Nonnull
    ZipkinData build();
}
