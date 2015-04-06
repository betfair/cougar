package com.betfair.cougar.client;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import org.apache.http.Header;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Standard context emitter for use with socket client transports (based on HttpContextEmitter)
 */
public class SocketContextEmitter<C> implements ContextEmitter<Map<String, String>, C> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();
    private final String uuidHeader;
    private final String uuidParentsHeader;
    private final GeoLocationSerializer geoLocationSerializer;

    public SocketContextEmitter(GeoLocationSerializer geoLocationSerializer, String uuidHeader, String uuidParentsHeader) {
        this.geoLocationSerializer = geoLocationSerializer;
        this.uuidHeader = uuidHeader;
        this.uuidParentsHeader = uuidParentsHeader;
    }


    @Override
    public void emit(ClientCallContext ctx, @Nonnull Map<String, String> additionalData, @Nullable C ignore) {
        if (ctx.traceLoggingEnabled()) {
            additionalData.put("X-Trace-Me", "true");
        }

        GeoLocationDetails gld = ctx.getLocation();
        if (gld != null) {
            List<Header> headers = new ArrayList<>();
            geoLocationSerializer.serialize(gld, headers);

            for (Header header : headers) {
                additionalData.put(header.getName(), header.getValue());
            }
        }

        if (uuidHeader != null) {
            RequestUUID requestUUID = ctx.getRequestUUID();
            additionalData.put(uuidHeader, requestUUID.getLocalUUIDComponent());
            if (uuidParentsHeader != null && requestUUID.getRootUUIDComponent() != null) {
                additionalData.put(uuidParentsHeader, requestUUID.getRootUUIDComponent() + UUIDGenerator.COMPONENT_SEPARATOR + requestUUID.getParentUUIDComponent());
            }
        }

        additionalData.put("X-RequestTime", DATE_TIME_FORMATTER.print(System.currentTimeMillis()));
    }
}
