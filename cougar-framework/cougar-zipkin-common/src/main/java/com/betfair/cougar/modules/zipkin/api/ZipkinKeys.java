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

import javax.annotation.Nullable;

/**
 * ZipkinKeys represents the names of the headers used for Zipkin tracing.
 * <p/>
 * Additionally, this class provides two helper methods for converting/parsing headers between Booleans and Strings.
 */
public final class ZipkinKeys {

    public static final String TRACE_ID = "X-B3-TraceId";
    public static final String SPAN_ID = "X-B3-SpanId";
    public static final String PARENT_SPAN_ID = "X-B3-ParentSpanId";
    public static final String SAMPLED = "X-B3-Sampled";
    public static final String FLAGS = "X-B3-Flags";

    public static final String DO_SAMPLE_VALUE = "1";
    public static final String DO_NOT_SAMPLE_VALUE = "0";

    private ZipkinKeys() {
    }

    /**
     * Converts a sampled (Boolean) value to its String representation.
     * <p/>
     * Note: If this method is invoked with a null value, the returned value will also be null.
     *
     * @param sampled The original value
     * @return The converted sampled value
     */
    @Nullable
    public static String sampledToString(@Nullable Boolean sampled) {
        return sampled == null ? null : (sampled ? DO_SAMPLE_VALUE : DO_NOT_SAMPLE_VALUE);
    }

    /**
     * Converts a sampled (String) value to its Boolean representation.
     * <p/>
     * Note: If this method is invoked with a null value, the returned value will also be null.
     *
     * @param sampled The original value
     * @return The converted sampled value
     */
    @Nullable
    public static Boolean sampledToBoolean(@Nullable String sampled) {
        return sampled == null ? null : DO_SAMPLE_VALUE.equals(sampled);
    }
}
