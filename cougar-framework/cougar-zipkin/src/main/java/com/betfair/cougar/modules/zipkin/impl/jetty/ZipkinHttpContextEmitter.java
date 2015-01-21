package com.betfair.cougar.modules.zipkin.impl.jetty;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.HttpContextEmitter;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Zipkin context emitter for use with http client transports
 */
public class ZipkinHttpContextEmitter<HR> extends HttpContextEmitter<HR> {

    public ZipkinHttpContextEmitter(GeoLocationSerializer geoLocationSerializer, String uuidHeader, String uuidParentsHeader,
                                    CompoundContextEmitter<HR, List<Header>> compoundContextEmitter) {
        super(geoLocationSerializer, uuidHeader, uuidParentsHeader);
        compoundContextEmitter.addEmitter(this);
    }

    @Override
    public void emit(ClientCallContext ctx, HR request, List<Header> result) {
        super.emit(ctx, request, result);

        RequestUUID requestUUID = ctx.getRequestUUID();

        if (requestUUID instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) requestUUID;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                ZipkinRequestUUID newZipkinRequestUUID = (ZipkinRequestUUID) zipkinRequestUUID.getNewSubUUID();

                ZipkinData zipkinData = newZipkinRequestUUID.getZipkinData();

                appendZipkinHeaders(result, zipkinData);
            } else {
                // disabling sampling for the entire request chain
                appendHeader(result, ZipkinKeys.SAMPLED, ZipkinKeys.DO_NOT_SAMPLE_VALUE);
            }

        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    private static void appendZipkinHeaders(@Nonnull List<Header> result, @Nonnull ZipkinData zipkinData) {
        // enabling sampling for the entire request chain
        appendHeader(result, ZipkinKeys.SAMPLED, ZipkinKeys.DO_SAMPLE_VALUE);
        appendHeader(result, ZipkinKeys.TRACE_ID, String.valueOf(zipkinData.getTraceId()));
        appendHeader(result, ZipkinKeys.SPAN_ID, String.valueOf(zipkinData.getSpanId()));
        if (zipkinData.getParentSpanId() != null) {
            appendHeader(result, ZipkinKeys.PARENT_SPAN_ID, zipkinData.getParentSpanId().toString());
        }
        if (zipkinData.getFlags() != null) {
            appendHeader(result, ZipkinKeys.FLAGS, zipkinData.getFlags().toString());
        }
    }

    private static void appendHeader(@Nonnull List<Header> result, String key, String value) {
        result.add(new BasicHeader(key, value));
    }
}
