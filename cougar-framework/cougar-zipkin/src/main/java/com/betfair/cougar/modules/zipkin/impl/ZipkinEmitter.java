package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import com.github.kristofa.brave.zipkin.ZipkinSpanCollector;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.twitter.zipkin.gen.zipkinCoreConstants.SERVER_RECV;
import static com.twitter.zipkin.gen.zipkinCoreConstants.SERVER_SEND;

public class ZipkinEmitter {

    private static final int LOCALHOST_IP;

    private String cougarAppName;

    private ZipkinSpanCollector zipkinSpanCollector;


    static {
        LOCALHOST_IP = RemoteAddressUtils.getIPv4AsInteger();
    }

    public void emitServerStartSpan(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, SERVER_RECV);
    }

    public void emitServerStopSpan(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, SERVER_SEND);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, String s) {
        Objects.requireNonNull(zipkinData);

        long annotationTimeMicro = TimeUnit.MICROSECONDS.toMicros(new Date().getTime());

        Annotation annotation = new Annotation(annotationTimeMicro, s);
        annotation.setHost(new Endpoint(LOCALHOST_IP, zipkinData.getPort(), cougarAppName));

        Span span = generateSpan(zipkinData, Arrays.asList(annotation), null);

        zipkinSpanCollector.collect(span);
    }

    @Nonnull
    private Span generateSpan(@Nonnull ZipkinData zipkinData, @Nullable List<Annotation> annotations,
                              @Nullable List<BinaryAnnotation> binaryAnnotations) {
        Span span = new Span(zipkinData.getTraceId(), zipkinData.getSpanName(), zipkinData.getSpanId(), annotations,
                binaryAnnotations);
        if (zipkinData.getParentSpanId() != null) {
            span.setParent_id(zipkinData.getParentSpanId());
        }
        return span;
    }

    public void setZipkinSpanCollector(@Nonnull ZipkinSpanCollector zipkinSpanCollector) {
        Objects.requireNonNull(zipkinSpanCollector);
        this.zipkinSpanCollector = zipkinSpanCollector;
    }

    public void setCougarAppName(@Nonnull String cougarAppName) {
        Objects.requireNonNull(cougarAppName);
        this.cougarAppName = cougarAppName;
    }
}