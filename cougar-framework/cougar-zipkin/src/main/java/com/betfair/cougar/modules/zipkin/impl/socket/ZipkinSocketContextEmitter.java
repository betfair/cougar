package com.betfair.cougar.modules.zipkin.impl.socket;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.SocketContextEmitter;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Zipkin context emitter for use with socket client transports (based on ZipkinHttpContextEmitter)
 */
public class ZipkinSocketContextEmitter<C> extends SocketContextEmitter<C> {

    public ZipkinSocketContextEmitter(GeoLocationSerializer geoLocationSerializer, String uuidHeader,
                                      String uuidParentsHeader,
                                      CompoundContextEmitter<Map<String, String>, C> compoundContextEmitter) {
        super(geoLocationSerializer, uuidHeader, uuidParentsHeader);
        compoundContextEmitter.addEmitter(this);
    }

    @Override
    public void emit(ClientCallContext ctx, @Nonnull Map<String, String> additionalData, @Nullable C ignore) {
        super.emit(ctx, additionalData, ignore);

        RequestUUID requestUUID = ctx.getRequestUUID();

        if (requestUUID instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) requestUUID;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

                appendZipkinHeaders(additionalData, zipkinData);
            } else {
                // disabling sampling for the entire request chain
                additionalData.put(ZipkinKeys.SAMPLED, ZipkinKeys.DO_NOT_SAMPLE_VALUE);
            }

        } // else ignore
    }

    private static void appendZipkinHeaders(@Nonnull Map<String, String> additionalData,
                                            @Nonnull ZipkinData zipkinData) {
        // enabling sampling for the entire request chain
        additionalData.put(ZipkinKeys.SAMPLED, ZipkinKeys.DO_SAMPLE_VALUE);
        additionalData.put(ZipkinKeys.TRACE_ID, Long.toHexString(zipkinData.getTraceId()));
        additionalData.put(ZipkinKeys.SPAN_ID, Long.toHexString(zipkinData.getSpanId()));

        if (zipkinData.getParentSpanId() != null) {
            additionalData.put(ZipkinKeys.PARENT_SPAN_ID, Long.toHexString(zipkinData.getParentSpanId()));
        }
        if (zipkinData.getFlags() != null) {
            additionalData.put(ZipkinKeys.FLAGS, zipkinData.getFlags().toString());
        }
    }
}
