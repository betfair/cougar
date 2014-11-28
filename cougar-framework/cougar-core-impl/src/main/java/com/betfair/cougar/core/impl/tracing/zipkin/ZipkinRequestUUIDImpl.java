package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.zipkin.ZipkinData;
import com.betfair.cougar.api.zipkin.ZipkinDataBuilder;
import com.betfair.cougar.api.zipkin.ZipkinRequestUUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.UUID;

public class ZipkinRequestUUIDImpl implements ZipkinRequestUUID {

    private RequestUUID cougarUuid;

    private ZipkinData zipkinData;

    private ZipkinDataBuilder zipkinDataBuilder;

    public ZipkinRequestUUIDImpl(@Nonnull RequestUUID cougarUuid) {
        this(cougarUuid, null);
    }

    /**
     * Constuct a Cougar/Zipkin Request object.
     * @param cougarUuid Traditional Cougar RequestUUID.
     * @param zipkinDataBuilder Zipkin data builder object to be populated later with the span name.
     *                          Passing null here means Zipkin tracing is not enabled for this request.
     */
    public ZipkinRequestUUIDImpl(@Nonnull RequestUUID cougarUuid, @Nullable ZipkinDataBuilder zipkinDataBuilder) {
        Objects.requireNonNull(cougarUuid);

        this.cougarUuid = cougarUuid;
        this.zipkinData = null;
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
                    .spanId(UUID.randomUUID().getMostSignificantBits())
                    .parentSpanId(zipkinData.getSpanId())
                    .port(zipkinData.getPort());

            return new ZipkinRequestUUIDImpl(cougarSubUuid, newZipkinDataBuilder);
        } else {
            // If this request is not being traced by Zipkin, the next request can't be traced either.
            return new ZipkinRequestUUIDImpl(cougarSubUuid);
        }
    }

    /**
     * Obtain Zipkin data if the object was already created.
     * @return ZipkinData object or null if Zipkin is not enabled for this request.
     * @throws IllegalStateException if the ZipkinData object isn't yet finalized or if Zipkin tracing is not enabled for this request.
     */
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

    // We need this because we only have the span name after the ZipkinHttpRequestUuidResolver pointcut
    @Override
    public void setZipkinSpanName(@Nonnull String spanName) {
        Objects.requireNonNull(spanName);

        if (zipkinData == null) {
            zipkinData = zipkinDataBuilder.spanName(spanName).build();
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
    public String toString() {
        // currently i think this is used for the logs, and I think it needs to
        // change to use getUUID which should be renamed to be more explicit
        return zipkinData + getUUID();
    }

    /**
     * Part o Externalizable interface
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException(); // assuming we can kill these
    }

    /**
     * Part of Externalizable interface
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException(); // assuming we can kill these
    }
}