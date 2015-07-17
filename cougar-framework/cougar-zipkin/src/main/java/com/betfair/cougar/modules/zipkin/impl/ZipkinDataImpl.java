/*
 * Copyright 2015, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A default implementation of ZipkinData.
 *
 * @see com.betfair.cougar.modules.zipkin.api.ZipkinData
 */
public class ZipkinDataImpl implements ZipkinData {

    private final long traceId;
    private final long spanId;
    private final Long parentSpanId;

    private final String spanName;
    private final short port;
    private final Long flags;


    private ZipkinDataImpl(@Nonnull Builder builder) {
        Objects.requireNonNull(builder);

        traceId = builder.traceId;
        spanId = builder.spanId;
        parentSpanId = builder.parentSpanId;
        spanName = builder.spanName;
        port = builder.port;
        flags = builder.flags;

        Objects.requireNonNull(spanName);
    }

    @Override
    public long getTraceId() {
        return traceId;
    }

    @Override
    public long getSpanId() {
        return spanId;
    }

    @Nullable
    @Override
    public Long getParentSpanId() {
        return parentSpanId;
    }

    @Nonnull
    @Override
    public String getSpanName() {
        return spanName;
    }

    @Override
    public short getPort() {
        return port;
    }

    @Nullable
    @Override
    public Long getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        return "ZipkinDataImpl{" +
                "traceId=" + traceId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", spanName='" + spanName + '\'' +
                ", port=" + port +
                ", flags=" + flags +
                '}';
    }

    /**
     * A ZipkinDataBuilder implementation to be used when instantiating new ZipkinDataImpl instances.
     *
     * @see com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder
     */
    public static final class Builder implements ZipkinDataBuilder {
        private long traceId;
        private long spanId;
        private Long parentSpanId;

        private String spanName;
        private short port;
        private Long flags;

        @Nonnull
        @Override
        public Builder traceId(long traceId) {
            this.traceId = traceId;
            return this;
        }

        @Nonnull
        @Override
        public Builder spanId(long spanId) {
            this.spanId = spanId;
            return this;
        }

        @Nonnull
        @Override
        public Builder parentSpanId(@Nullable Long parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        @Nonnull
        @Override
        public Builder spanName(@Nonnull String spanName) {
            this.spanName = spanName;
            return this;
        }

        @Nonnull
        @Override
        public Builder port(short port) {
            this.port = port;
            return this;
        }

        @Nonnull
        @Override
        public Builder flags(@Nullable Long flags) {
            this.flags = flags;
            return this;
        }

        @Nonnull
        @Override
        public ZipkinDataImpl build() {
            return new ZipkinDataImpl(this);
        }
    }
}
