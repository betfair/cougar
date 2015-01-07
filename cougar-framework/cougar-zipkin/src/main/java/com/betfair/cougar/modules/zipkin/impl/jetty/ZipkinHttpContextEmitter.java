package com.betfair.cougar.modules.zipkin.impl.jetty;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.client.HttpContextEmitter;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
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
    public void emit(ExecutionContext ctx, HR request, List<Header> result) {
        super.emit(ctx, request, result);

        RequestUUID requestUUID = ctx.getRequestUUID();

        if (requestUUID instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) requestUUID;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                ZipkinRequestUUID newZipkinRequestUUID = (ZipkinRequestUUID) zipkinRequestUUID.getNewSubUUID();

                ZipkinData zipkinData = newZipkinRequestUUID.getZipkinData();

                appendZipkinHeaders(result, zipkinData);
            }

        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    private void appendZipkinHeaders(@Nonnull List<Header> result, @Nonnull ZipkinData zipkinData) {
        result.add(new BasicHeader(ZipkinManager.TRACE_ID_KEY, zipkinData.getTraceId().toString()));
        result.add(new BasicHeader(ZipkinManager.SPAN_ID_KEY, zipkinData.getSpanId().toString()));
        if (zipkinData.getParentSpanId() != null) {
            result.add(new BasicHeader(ZipkinManager.PARENT_SPAN_ID_KEY, zipkinData.getParentSpanId().toString()));
        }
    }
}
