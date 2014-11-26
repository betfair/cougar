package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.zipkin.ZipkinData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ZipkinDataImpl implements ZipkinData {

    private Long traceId;
    private Long spanId;
    private Long parentSpanId;

    private String spanName;
    private short port;


    private ZipkinDataImpl(@Nonnull Long traceId, @Nonnull Long spanId, @Nullable Long parentSpanId,
                           @Nonnull String spanName, short port) {
        Objects.requireNonNull(traceId);
        Objects.requireNonNull(spanId);
        Objects.requireNonNull(spanName);

        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.spanName = spanName;
        this.port = port;
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

    public static final class Builder {
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
            return new ZipkinDataImpl(traceId, spanId, parentSpanId, spanName, port);
        }
    }

//    @Nonnull
//    public static ZipkinDataImpl createInstance(@Nonnull String traceIdStr, @Nonnull String spanIdStr,
//                                            @Nullable String parentSpanIdStr, @Nonnull String spanName, int port) {
//        Objects.requireNonNull(traceIdStr);
//        Objects.requireNonNull(spanIdStr);
//        Objects.requireNonNull(spanName);
//
//        // mandatory
//        Long traceId = Long.valueOf(traceIdStr);
//        Long spanId = Long.valueOf(spanIdStr);
//
//        // non mandatory
//        Long parentSpanId = parentSpanIdStr == null ? null : Long.valueOf(parentSpanIdStr);
//
//        return new ZipkinDataImpl(traceId, spanId, parentSpanId, spanName, (short) port);
//    }
}
