package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class ZipkinRequestUUIDImpl implements RequestUUID {
    // Example: 2uhjsd-asdasjdaf-fss-jasd-asjd;2131415:3123124:23141515
    public static final String SERIALIZATION_SEPARATOR = ";";
    public static final String ZIPKIN_SERIALIZATION_SEPARATOR = ":";

    private static UUIDGenerator generator;
    private static ZipkinConfig zipkinConfig;


    private String cougarUUID;

    private ZipkinData zipkinData;

    private String serialization = "";


    public ZipkinRequestUUIDImpl() {
        cougarUUID = generator.getNextUUID();
    }

    /**
     * Construct a ZipkinRequestUUIDImpl with the specified UUID. If the given UUID is a traditional Cougar UUID then
     * Zipkin fields (including root UUID, parent UUID, local UUID), will NOT be derived from the given UUID and will be
     * randomly generated following Zipkin's contract. If the given UUID is a Cougar-Zipkin serialization following the
     * cougar-uuid;zipkin-traceid:zipkin-spanid:zipkin-parentspanid
     *
     * @param uuid complete traditional Cougar's uuid string
     */
    public ZipkinRequestUUIDImpl(@Nonnull String uuid) {
        Objects.requireNonNull(uuid);
        setUuidRaw(uuid);
    }

    /**
     * Construct a ZipkinRequestUUIDImpl for a specific request.
     *
     * @param cougarUUID   Traditional cougar id (preserved for compatibility reasons). If null a new cougar id is
     *                     generated based on the active UUIDGenerator.
     * @param traceId      Zipkin traceId. If null a new trace id is generated.
     * @param spanId       Zipkin spanId. If null a new span id is generated.
     * @param parentSpanId Zipkin parent span id.
     * @param spanName     Zipkin span name. Used to identify the current RPC node/service.
     */
    public ZipkinRequestUUIDImpl(@Nullable String cougarUUID, @Nullable String traceId, @Nullable String spanId,
                                 @Nullable String parentSpanId, @Nonnull String spanName) {
        setup(cougarUUID, traceId, spanId, parentSpanId, spanName);
    }

    /**
     * Note, this sets the system wide generator, not just the local one for this instance, nasty due to spring.
     * In this case, as we are using Zipkin which has its own contract for trace and span ids, we only apply the
     * given generator for compatibility purposes on the creation and validation of the traditional Cougar UUID
     * (getUUID())
     */
    public static void setGenerator(@Nonnull UUIDGenerator generator) {
        Objects.requireNonNull(generator);
        ZipkinRequestUUIDImpl.generator = generator;
    }

    /**
     * Sets the static system-wide Zipkin configuration that contains the rules to determine whether we should trace
     * the following requests or not.
     *
     * @param zipkinConfig zipkin static configurations
     */
    public static void setZipkinConfig(@Nonnull ZipkinConfig zipkinConfig) {
        Objects.requireNonNull(zipkinConfig);
        ZipkinRequestUUIDImpl.zipkinConfig = zipkinConfig;
    }

    /**
     * Returns standard conforming Cougar UUID, letting you use your own generator without affecting Zipkin specific
     * fields.
     *
     * @return String representing the Cougar request uuid
     */
    @Override
    @Nonnull
    public String getUUID() {
        return cougarUUID;
    }

    /**
     * Represents Zipkin's trace id. One per request and propagated across the entire infrastructure.
     *
     * @return String representing the randomly generated long
     */
    @Override
    @Nullable
    public String getRootUUIDComponent() {
        return zipkinData == null ? null : String.valueOf(zipkinData.getTraceId());
    }

    /**
     * Represents Zipkin's parent span id. Previous RPC span.
     *
     * @return String representing the randomly generated long
     */
    @Override
    @Nullable
    public String getParentUUIDComponent() {
        return zipkinData == null ? null : String.valueOf(zipkinData.getParentSpanId());
    }

    /**
     * Represents Zipkin's current span id. Current RPC span.
     *
     * @return String representing the randomly generated long
     */
    @Override
    @Nullable
    public String getLocalUUIDComponent() {
        return zipkinData == null ? null : String.valueOf(zipkinData.getSpanId());
    }

    /**
     * Represents Zipkin's current span name.
     *
     * @return String representing the name of this current span (typically service or host name).
     */
    @Nullable
    public String getSpanName() {
        return zipkinData == null ? null : zipkinData.getSpanName();
    }

    /**
     * Represents Zipkin's data.
     * @return object will all Zipkin data, or null if it wasn't set already.
     */
    @Nullable
    public ZipkinData getZipkinData() {
        return zipkinData;
    }

    @Override
    @Nonnull
    public RequestUUID getNewSubUUID() {
        if (zipkinData == null) {
            // means that no uuid was passed into construction -> we are the root
            return new ZipkinRequestUUIDImpl(null, null, null, null, "SPAN-NAME");
        }
        return new ZipkinRequestUUIDImpl(cougarUUID, String.valueOf(zipkinData.getTraceId()),
                String.valueOf(UUID.randomUUID().getMostSignificantBits()),
                String.valueOf(zipkinData.getSpanId()), "SPAN-NAME");
    }

    public void setup(@Nullable String cougarUUID, @Nullable String traceId, @Nullable String spanId,
                      @Nullable String parentSpanId, @Nonnull String spanName) {
        Objects.requireNonNull(cougarUUID);
        Objects.requireNonNull(spanName);

        if (cougarUUID == null) {
            this.cougarUUID = generator.getNextUUID();
        } else {
            this.cougarUUID = cougarUUID;
        }

        ZipkinData.Builder zipkinDataBuilder = new ZipkinData.Builder();

        if (traceId != null && spanId != null) {
            // a request with the fields is always traceable so we always propagate the tracing to the following calls
            zipkinDataBuilder.traceId(Long.valueOf(traceId));
            zipkinDataBuilder.spanId(Long.valueOf(spanId));
            zipkinDataBuilder.parentSpanId(parentSpanId == null ? null : Long.valueOf(parentSpanId));
            zipkinDataBuilder.spanName(spanName);

            this.zipkinData = zipkinDataBuilder.build();

        } else {

            if (zipkinConfig.shouldTrace()) {
                // starting point, we need to generate the ids if this request is to be sampled - we are the root
                UUID uuid = UUID.randomUUID();
                zipkinDataBuilder.traceId(uuid.getLeastSignificantBits());
                zipkinDataBuilder.spanId(uuid.getMostSignificantBits());
                zipkinDataBuilder.parentSpanId(null);
                zipkinDataBuilder.spanName(spanName);

                this.zipkinData = zipkinDataBuilder.build();

            } else {
                // otherwise leave them as null
                this.zipkinData = null;

            }

        }

        updateSerialization();
    }

    private void setUuidRaw(String rawUuid) {
        String[] split = rawUuid.split(Pattern.quote(SERIALIZATION_SEPARATOR));

        switch (split.length) {
            case 2:
                // traditional Cougar UUID and Zipkin UUIDs
                String[] zipkinComponents = split[1].split(Pattern.quote(ZIPKIN_SERIALIZATION_SEPARATOR));
                setup(split[0], zipkinComponents[0], zipkinComponents[1], zipkinComponents[2], "SPAN-NAME");
                break;

            case 1:
                // only traditional Cougar UUID
                setup(split[0], null, null, null, "servicename");
        }
    }

    /**
     * Part o Externalizable interface
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            setUuidRaw((String) in.readObject());
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    /**
     * Part of Externalizable interface
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(toString());
    }

    private void updateSerialization() {
        if (zipkinData != null) {
            this.serialization = getUUID();
        } else {
            this.serialization = getUUID() + SERIALIZATION_SEPARATOR +
                    StringUtils.join(
                            new String[]{
                                    String.valueOf(zipkinData.getTraceId()),
                                    String.valueOf(zipkinData.getSpanName()),
                                    String.valueOf(zipkinData.getParentSpanId())
                            },
                            ZIPKIN_SERIALIZATION_SEPARATOR);
        }
    }

    @Override
    public String toString() {
        return serialization;
    }
}
