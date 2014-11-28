package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.zipkin.ZipkinData;
import com.betfair.cougar.api.zipkin.ZipkinDataBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ZipkinDataImpl implements ZipkinData {

    private final Long traceId;
    private final Long spanId;
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

        Objects.requireNonNull(traceId);
        Objects.requireNonNull(spanId);
        Objects.requireNonNull(spanName);
    }

    @Nonnull
    public Long getTraceId() {
        return traceId;
    }

    @Nonnull
    public Long getSpanId() {
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
        private Long traceId;
        private Long spanId;
        private Long parentSpanId;

        private String spanName;
        private short port;

        @Nonnull
        public Builder traceId(@Nonnull Long traceId) {
            this.traceId = traceId;
            return this;
        }

        @Nonnull
        public Builder spanId(@Nonnull Long spanId) {
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
