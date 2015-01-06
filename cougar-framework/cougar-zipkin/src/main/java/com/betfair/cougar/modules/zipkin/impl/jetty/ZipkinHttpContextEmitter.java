package com.betfair.cougar.modules.zipkin.impl.jetty;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.client.HttpContextEmitter;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.betfair.cougar.modules.zipkin.impl.ZipkinEmitter;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Zipkin context emitter for use with http client transports
 */
public class ZipkinHttpContextEmitter<HR> extends HttpContextEmitter<HR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpContextEmitter.class);

    private final ZipkinEmitter zipkinEmitter;

    public ZipkinHttpContextEmitter(GeoLocationSerializer geoLocationSerializer, String uuidHeader, String uuidParentsHeader,
                                    ZipkinEmitter zipkinEmitter) {
        super(geoLocationSerializer, uuidHeader, uuidParentsHeader);
        this.zipkinEmitter = zipkinEmitter;
    }


    @Override
    public void emit(ExecutionContext ctx, HR request, List<Header> result) {
        super.emit(ctx, request, result);

        RequestUUID requestUUID = ctx.getRequestUUID();

        if (requestUUID instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) requestUUID;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                ZipkinRequestUUID newZipkinRequestUUID = (ZipkinRequestUUID) zipkinRequestUUID.getNewSubUUID();

                //TODO: Set span name
                newZipkinRequestUUID.setZipkinSpanName("SOMETHING NEW SPECIFIC TO THIS CLIENT CALL");

                ZipkinData zipkinData = newZipkinRequestUUID.getZipkinData();

                appendZipkinHeaders(result, zipkinData);

                zipkinEmitter.emitClientSendSpan(zipkinData);
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
