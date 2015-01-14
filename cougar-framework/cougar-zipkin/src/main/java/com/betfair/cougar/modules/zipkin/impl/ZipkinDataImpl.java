package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ZipkinDataImpl implements ZipkinData {

    private final long traceId;
    private final long spanId;
    private final Long parentSpanId;

    private final String spanName;
    private final short port;


    private ZipkinDataImpl(@Nonnull Builder builder) {
        Objects.requireNonNull(builder);

        traceId = builder.traceId;
        spanId = builder.spanId;
        parentSpanId = builder.parentSpanId;
        spanName = builder.spanName;
        port = builder.port;

        Objects.requireNonNull(spanName);
    }

    public long getTraceId() {
        return traceId;
    }

    public long getSpanId() {
        return spanId;
    }

    @Nullable
    public Long getParentSpanId() {
        return parentSpanId;
    }

    @Nonnull
    public String getSpanName() {
        return spanName;
    }

    public short getPort() {
        return port;
    }

    public static final class Builder implements ZipkinDataBuilder {
        private long traceId;
        private long spanId;
        private Long parentSpanId;

        private String spanName;
        private short port;

        @Nonnull
        public Builder traceId(long traceId) {
            this.traceId = traceId;
            return this;
        }

        @Nonnull
        public Builder spanId(long spanId) {
            this.spanId = spanId;
            return this;
        }

        @Nonnull
        public Builder parentSpanId(@Nullable Long parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        @Nonnull
        public Builder spanName(@Nonnull String spanName) {
            this.spanName = spanName;
            return this;
        }

        @Nonnull
        public Builder port(short port) {
            this.port = port;
            return this;
        }

        @Nonnull
        public ZipkinDataImpl build() {
            return new ZipkinDataImpl(this);
        }
    }
}
