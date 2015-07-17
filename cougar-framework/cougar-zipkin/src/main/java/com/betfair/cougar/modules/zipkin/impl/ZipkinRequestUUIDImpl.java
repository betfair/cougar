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

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Base implementation of ZipkinRequestUUID.
 *
 * @see com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID
 */
public class ZipkinRequestUUIDImpl implements ZipkinRequestUUID {

    private RequestUUID cougarUuid;

    private ZipkinData zipkinData;

    private ZipkinDataBuilder zipkinDataBuilder;

    public ZipkinRequestUUIDImpl(@Nonnull RequestUUID cougarUuid) {
        this(cougarUuid, null);
    }

    /**
     * Constuct a Cougar/Zipkin Request object.
     *
     * @param cougarUuid        Traditional Cougar RequestUUID.
     * @param zipkinDataBuilder Zipkin data builder object to be populated later with the span name.
     *                          Passing null here means Zipkin tracing is not enabled for this request.
     */
    public ZipkinRequestUUIDImpl(@Nonnull RequestUUID cougarUuid, @Nullable ZipkinDataBuilder zipkinDataBuilder) {
        Objects.requireNonNull(cougarUuid);

        this.cougarUuid = cougarUuid;
        this.zipkinDataBuilder = zipkinDataBuilder;
    }

    @Override
    public String getRootUUIDComponent() {
        return cougarUuid.getRootUUIDComponent();
    }

    @Override
    public String getParentUUIDComponent() {
        return cougarUuid.getParentUUIDComponent();
    }

    @Override
    public String getLocalUUIDComponent() {
        return cougarUuid.getLocalUUIDComponent();
    }

    @Override
    @Nonnull
    public RequestUUID getNewSubUUID() {
        RequestUUID cougarSubUuid = cougarUuid.getNewSubUUID();

        if (isZipkinTracingEnabled()) {
            // Creating a child zipkin data builder object.
            // The child span name will still need to be set after, as it happened with the original zipkinDataBuilder.
            ZipkinDataBuilder newZipkinDataBuilder = new ZipkinDataImpl.Builder()
                    .traceId(zipkinData.getTraceId())
                    .spanId(ZipkinManager.getRandomLong())
                    .parentSpanId(zipkinData.getSpanId())
                    .port(zipkinData.getPort());

            return new ZipkinRequestUUIDImpl(cougarSubUuid, newZipkinDataBuilder);
        } else {
            // If this request is not being traced by Zipkin, the next request can't be traced either.
            return new ZipkinRequestUUIDImpl(cougarSubUuid);
        }
    }

    @Override
    @Nonnull
    public ZipkinData getZipkinData() {
        if (zipkinData == null) {
            if (isZipkinTracingEnabled()) {
                throw new IllegalStateException("Zipkin Data is still incomplete");
            } else {
                throw new IllegalStateException("Zipkin tracing is not enabled for this request");
            }
        } else {
            return zipkinData;
        }
    }

    @Override
    public boolean isZipkinTracingEnabled() {
        return zipkinDataBuilder != null;
    }

    @Override
    public boolean isZipkinTracingReady() {
        return zipkinData != null;
    }

    @Override
    public ZipkinData buildZipkinData(@Nonnull String spanName) {
        Objects.requireNonNull(spanName);

        if (zipkinData == null) {
            zipkinData = zipkinDataBuilder.spanName(spanName).build();
            return zipkinData;
        } else {
            throw new IllegalStateException("Span name was already set for this request.");
        }
    }

    /**
     * Returns standard conforming Cougar UUID, letting you use your own generator without affecting Zipkin specific
     * fields.
     *
     * @return String representing the Cougar request uuid
     */
    @Override
    public String getUUID() {
        return cougarUuid.getUUID();
    }

    @Override
    public String toCougarLogString() {
        return cougarUuid.toCougarLogString();
    }

    @Override
    public String toString() {
        return "ZipkinRequestUUIDImpl{" +
                "cougarUuid=" + getUUID() +
                ", zipkinData=" + zipkinData +
                '}';
    }
}