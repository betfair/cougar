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
