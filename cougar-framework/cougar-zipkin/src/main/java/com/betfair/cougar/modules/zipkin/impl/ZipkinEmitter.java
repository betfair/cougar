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
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import com.betfair.cougar.util.time.Clock;
import com.github.kristofa.brave.zipkin.ZipkinSpanCollector;
import com.twitter.zipkin.gen.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.twitter.zipkin.gen.zipkinCoreConstants.*;

/**
 * An emitter capable of emitting ZipkinData information to a ZipkinSpanCollector. ZipkinEmitter should be instantiated
 * per service, as it stores information about the service (attaching it to the emitted spans transparently).
 *
 * @see com.betfair.cougar.modules.zipkin.api.ZipkinData
 */
@ManagedResource
public class ZipkinEmitter implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipkinEmitter.class);

    private int serviceIPv4;

    private String serviceName;

    private ZipkinSpanCollector zipkinSpanCollector;

    private final Clock clock;

    private BlockingQueue<?> zipkinSpanCollectorInternalQueue;

    /**
     * Creates a new ZipkinEmitter. This constructor overload obtains the service IPv4 through
     * com.betfair.cougar.util.geolocation.RemoteAddressUtils.
     *
     * @param serviceName         The name of the service the emitter will correspond to
     * @param zipkinSpanCollector The brave ZipkinSpanCollector to be used for emitting spans
     * @param clock               The clock to be used when obtaining timestamps
     */
    public ZipkinEmitter(@Nonnull String serviceName, @Nonnull ZipkinSpanCollector zipkinSpanCollector,
                         @Nonnull Clock clock) {
        this(serviceName, zipkinSpanCollector, clock, RemoteAddressUtils.getLocalhostAsIPv4Integer());
    }

    /**
     * Creates a new ZipkinEmitter.
     *
     * @param serviceName         The name of the service the emitter will correspond to
     * @param zipkinSpanCollector The brave ZipkinSpanCollector to be used for emitting spans
     * @param clock               The clock to be used when obtaining timestamps
     * @param serviceIPv4         The IPv4 of the service the emitter will correspond to
     */
    public ZipkinEmitter(@Nonnull String serviceName, @Nonnull ZipkinSpanCollector zipkinSpanCollector,
                         @Nonnull Clock clock, int serviceIPv4) {
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(zipkinSpanCollector);
        Objects.requireNonNull(clock);

        this.serviceIPv4 = serviceIPv4;
        this.serviceName = serviceName;
        this.zipkinSpanCollector = zipkinSpanCollector;
        this.clock = clock;
    }

    /**
     * Emits a Server Receive annotation for a particular span.
     *
     * @param zipkinData The ZipkinData representing the span
     */
    public void emitServerReceive(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, SERVER_RECV);
    }

    /**
     * Emits a Server Send annotation for a particular span.
     *
     * @param zipkinData The ZipkinData representing the span
     */
    public void emitServerSend(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, SERVER_SEND);
    }

    /**
     * Emits a Client Send annotation for a particular span.
     *
     * @param zipkinData The ZipkinData representing the span
     */
    public void emitClientSend(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, CLIENT_SEND);
    }

    /**
     * Emits a Client Receive annotation for a particular span.
     *
     * @param zipkinData The ZipkinData representing the span
     */
    public void emitClientReceive(@Nonnull ZipkinData zipkinData) {
        emitAnnotation(zipkinData, CLIENT_RECV);
    }

    /**
     * Builds an annotations store for a specific ZipkinData object.
     * <p/>
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
     * Emits a pre-populated storage of annotations to Zipkin.
     *
     * @param zipkinAnnotationsStore Zipkin annotations storage representing the entire list of annotations to emit
     */
    public void emitAnnotations(@Nonnull ZipkinAnnotationsStore zipkinAnnotationsStore) {
        Objects.requireNonNull(zipkinAnnotationsStore);
        zipkinSpanCollector.collect(zipkinAnnotationsStore.generate());
    }


    // Single annotation emission methods

    /**
     * Emits a single annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param s          The annotation to emit
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String s) {
        long timestampMillis = clock.millis();
        long timestampMicros = TimeUnit.MILLISECONDS.toMicros(timestampMillis);

        ZipkinAnnotationsStore store = prepareEmission(zipkinData, s).addAnnotation(timestampMicros, s);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) string annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, @Nonnull String value) {
        Objects.requireNonNull(value);
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) short annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, short value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) int annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, int value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) long annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, long value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) double annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, double value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) boolean annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
    public void emitAnnotation(@Nonnull ZipkinData zipkinData, @Nonnull String key, boolean value) {
        ZipkinAnnotationsStore store = prepareEmission(zipkinData, key).addAnnotation(key, value);
        emitAnnotations(store);
    }

    /**
     * Emits a single (binary) byte array annotation to Zipkin.
     *
     * @param zipkinData Zipkin request data
     * @param key        The annotation key
     * @param value      The annotation value
     */
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

    @Override
    public void afterPropertiesSet() {
        try {
            Field field = zipkinSpanCollector.getClass().getDeclaredField("spanQueue");
            field.setAccessible(true);
            zipkinSpanCollectorInternalQueue = (BlockingQueue<?>) field.get(zipkinSpanCollector);
        } catch (Exception e) {
            LOGGER.warn("Unable to obtain ZipkinSpanCollector's internal queue", e);
        }
    }

    /**
     * Gets the current size of the underlying queue (of spans). This method returns -1 if ZipkinEmitter was unable to
     * obtain a reference to the queue.
     *
     * @return The current size of the underlying queue
     */
    @ManagedAttribute
    public int getCurrentQueueSize() {
        return zipkinSpanCollectorInternalQueue != null ? zipkinSpanCollectorInternalQueue.size() : -1;
    }

    /**
     * Gets the remaining capacity of the underlying queue (of spans). This method returns -1 if ZipkinEmitter was
     * unable to obtain a reference to the queue.
     *
     * @return The remaining capacity of the underlying queue
     */
    @ManagedAttribute
    public int getRemainingQueueCapacity() {
        return zipkinSpanCollectorInternalQueue != null ? zipkinSpanCollectorInternalQueue.remainingCapacity() : -1;
    }
}
