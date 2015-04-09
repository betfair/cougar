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
