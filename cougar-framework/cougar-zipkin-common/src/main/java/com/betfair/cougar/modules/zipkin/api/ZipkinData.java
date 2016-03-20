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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ZipkinData provides methods used for retrieving the Zipkin data corresponding to a Zipkin span.
 * <p/>
 * This interface provides methods for retrieving the following data:
 * <li>Trace ID</li>
 * <li>Span ID</li>
 * <li>Parent Span ID</li>
 * <li>Span Name</li>
 * <li>Port</li>
 * <li>Flags</li>
 * <p/>
 * All the provided data follows standard Zipkin naming and conventions.
 */
public interface ZipkinData {

    /**
     * Retrieves the trace ID of the span.
     *
     * @return the trace ID of the span
     */
    long getTraceId();

    /**
     * Retrieves the ID of the span.
     *
     * @return the ID of the span
     */
    long getSpanId();

    /**
     * Retrieves the parent span ID of the span (i.e. the ID of the parent span, of which this span is a child of).
     * If this is the root span (i.e. the first span in the chain), the returned value will be null.
     *
     * @return the parent span ID of the span
     */
    @Nullable
    Long getParentSpanId();

    /**
     * Retrieves the name of the span.
     *
     * @return the name of the span
     */
    @Nonnull
    String getSpanName();

    /**
     * Retrieves the port associated with the span (i.e. the port associated with the RPC represented by the span).
     *
     * @return the port associated with the span
     */
    short getPort();

    /**
     * Retrieves the flags of the span.
     *
     * @return the flags of the span
     */
    @Nullable
    Long getFlags();
}
