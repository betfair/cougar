package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.zipkin.ZipkinData;
import com.github.kristofa.brave.zipkin.ZipkinSpanCollector;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.twitter.zipkin.gen.zipkinCoreConstants.SERVER_RECV;
import static com.twitter.zipkin.gen.zipkinCoreConstants.SERVER_SEND;

@Component(ZipkinEmitter.BEAN_NAME)
public class ZipkinEmitter {

    public static final String BEAN_NAME = "zipkinEmitter";

    private static final Logger LOG = LoggerFactory.getLogger(ZipkinEmitter.class);

    private static final int LOCALHOST_IP;

    private static final String LOCALHOST_NAME;

    @Resource
    private ZipkinSpanCollector zipkinSpanCollector;


    static {
        try {
            //TODO: find other way to get this info
            InetAddress localhost = InetAddress.getLocalHost();
            LOCALHOST_IP = new BigInteger(localhost.getAddress()).intValue();
            LOCALHOST_NAME = localhost.getHostName();
        } catch (UnknownHostException e) {
            //TODO: Should we throw this or just log?
            throw new RuntimeException(e);
        }
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
        annotation.setHost(new Endpoint(LOCALHOST_IP, zipkinData.getPort(), LOCALHOST_NAME));

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
}
