package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.github.kristofa.brave.zipkin.ZipkinSpanCollector;
import com.twitter.zipkin.gen.Endpoint;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.twitter.zipkin.gen.zipkinCoreConstants.*;

public class ZipkinEmitter {

    private int serviceIPv4;

    private String serviceName;

    private ZipkinSpanCollector zipkinSpanCollector;


    public ZipkinEmitter(int serviceIPv4, @Nonnull String serviceName, @Nonnull ZipkinSpanCollector zipkinSpanCollector) {
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(zipkinSpanCollector);

        this.serviceIPv4 = serviceIPv4;
        this.serviceName = serviceName;
        this.zipkinSpanCollector = zipkinSpanCollector;
    }

    public void emitServerReceive(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, SERVER_RECV);
    }

    public void emitServerSend(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, SERVER_SEND);
    }

    public void emitClientSend(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, CLIENT_SEND);
    }

    public void emitClientReceive(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, CLIENT_RECV);
    }

    /**
     * The returning object should be used to emit more than 1 annotation at once. After adding your annotations you
     * will need to call emitAnnotations in order to send the span with all the annotations to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @return Zipkin annotations storage capable of merging multiple annotations per emission
     */
    @Nonnull
    public ZipkinAnnotationsStore buildAnnotationsStore(@Nonnull ZipkinData zipkinData) {
        Objects.requireNonNull(zipkinData);
        Endpoint endpoint = generateEndpoint(zipkinData);
        return new ZipkinAnnotationsStore(zipkinData).defaultEndpoint(endpoint);
    }

    /**
     * Emit a pre-populated storage of annotations to Zipkin
     *
     * @param zipkinAnnotationsStore Zipkin annotations storage representing the entire list of annotations to emit
     */
    public void emitAnnotations(@Nonnull ZipkinAnnotationsStore zipkinAnnotationsStore) {
        Objects.requireNonNull(zipkinAnnotationsStore);
        zipkinSpanCollector.collect(zipkinAnnotationsStore.generate());
    }


    // Single annotation emission methods

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String s) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, s).addAnnotation(s);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, @Nonnull String value) {
        Objects.requireNonNull(value);
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, short value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, int value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, long value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, double value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, boolean value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, byte[] value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    @Nonnull
    private ZipkinAnnotationsStore prepareEmission(@Nonnull ZipkinData zipkinData, @Nonnull String s) {
        Objects.requireNonNull(s);
        return buildAnnotationsStore(zipkinData);
    }

    @Nonnull
    private Endpoint generateEndpoint(@Nonnull ZipkinData zipkinData) {
        return new Endpoint(serviceIPv4, zipkinData.getPort(), serviceName);
    }
}
