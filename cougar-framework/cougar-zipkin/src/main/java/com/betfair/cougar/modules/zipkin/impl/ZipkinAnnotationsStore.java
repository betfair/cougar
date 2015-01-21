package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.twitter.zipkin.gen.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class ZipkinAnnotationsStore {

    public static final int INT_SIZE_B = Integer.SIZE / 8;
    public static final int LONG_SIZE_B = Long.SIZE / 8;
    public static final int DOUBLE_SIZE_B = Double.SIZE / 8;

    private ZipkinData zipkinData;
    private List<Annotation> annotations;
    private List<BinaryAnnotation> binaryAnnotations;
    private Endpoint defaultEndpoint;

    ZipkinAnnotationsStore(@Nonnull ZipkinData zipkinData) {
        this.zipkinData = zipkinData;
        this.annotations = new ArrayList<>();
        this.binaryAnnotations = new ArrayList<>();
    }


    // PUBLIC METHODS

    /**
     * Append annotation with value s and timestamp now.
     *
     * @param s Annotation value to emit
     * @return this object
     */
    @Nonnull
    public ZipkinAnnotationsStore addAnnotation(@Nonnull String s) {
        long timestampMicro = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
        return addAnnotation(timestampMicro, s, defaultEndpoint);
    }

    @Nonnull
    public ZipkinAnnotationsStore addAnnotation(@Nonnull String key, @Nonnull String value) {
        return addBinaryAnnotation(key, value, defaultEndpoint);
    }

    @Nonnull
    public ZipkinAnnotationsStore addAnnotation(@Nonnull String key, int value) {
        return addBinaryAnnotation(key, value, defaultEndpoint);
    }

    @Nonnull
    public ZipkinAnnotationsStore addAnnotation(@Nonnull String key, long value) {
        return addBinaryAnnotation(key, value, defaultEndpoint);
    }

    @Nonnull
    public ZipkinAnnotationsStore addAnnotation(@Nonnull String key, double value) {
        return addBinaryAnnotation(key, value, defaultEndpoint);
    }


    // PACKAGE-PRIVATE METHODS

    @Nonnull
    ZipkinAnnotationsStore defaultEndpoint(@Nonnull Endpoint defaultEndpoint) {
        this.defaultEndpoint = defaultEndpoint;
        return this;
    }

    @Nonnull
    ZipkinAnnotationsStore addAnnotation(long timestampMicro, @Nonnull String s, @Nullable Endpoint endpoint) {
        Objects.requireNonNull(s);
        Annotation annotation = new Annotation(timestampMicro, s);
        if (endpoint != null) {
            // endpoint is optional - current version of zipkin web doesn't show spans without host though
            annotation.setHost(endpoint);
        }
        annotations.add(annotation);
        return this;
    }

    @Nonnull
    ZipkinAnnotationsStore addBinaryAnnotation(@Nonnull String key, @Nonnull String value, @Nonnull Endpoint endpoint) {
        // Using default charset
        ByteBuffer wrappedValue = ByteBuffer.wrap(value.getBytes());
        return addBinaryAnnotation(key, wrappedValue, AnnotationType.STRING, endpoint);
    }

    @Nonnull
    ZipkinAnnotationsStore addBinaryAnnotation(@Nonnull String key, int value, @Nonnull Endpoint endpoint) {
        ByteBuffer wrappedValue = ByteBuffer.allocate(INT_SIZE_B).putInt(value);
        return addBinaryAnnotation(key, wrappedValue, AnnotationType.I32, endpoint);
    }

    @Nonnull
    ZipkinAnnotationsStore addBinaryAnnotation(@Nonnull String key, long value, @Nonnull Endpoint endpoint) {
        ByteBuffer wrappedValue = ByteBuffer.allocate(LONG_SIZE_B).putLong(value);
        return addBinaryAnnotation(key, wrappedValue, AnnotationType.I64, endpoint);
    }

    @Nonnull
    ZipkinAnnotationsStore addBinaryAnnotation(@Nonnull String key, double value, @Nonnull Endpoint endpoint) {
        ByteBuffer wrappedValue = ByteBuffer.allocate(DOUBLE_SIZE_B).putDouble(value);
        return addBinaryAnnotation(key, wrappedValue, AnnotationType.DOUBLE, endpoint);
    }

    @Nonnull
    Span generate() {
        Span span = new Span(zipkinData.getTraceId(), zipkinData.getSpanName(), zipkinData.getSpanId(), annotations,
                binaryAnnotations);
        if (zipkinData.getParentSpanId() != null) {
            span.setParent_id(zipkinData.getParentSpanId());
        }
        return span;
    }


    // PRIVATE METHODS

    @Nonnull
    private ZipkinAnnotationsStore addBinaryAnnotation(@Nonnull String key, @Nonnull ByteBuffer byteBuffer,
                                                       @Nonnull AnnotationType annotationType, @Nullable Endpoint endpoint) {
        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(key, byteBuffer, annotationType);
        if (endpoint != null) {
            // endpoint is optional - current version of zipkin web doesn't show spans without host though
            binaryAnnotation.setHost(endpoint);
        }
        binaryAnnotations.add(binaryAnnotation);
        return this;
    }
}
