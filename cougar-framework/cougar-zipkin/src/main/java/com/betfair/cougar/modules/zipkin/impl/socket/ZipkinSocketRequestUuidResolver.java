package com.betfair.cougar.modules.zipkin.impl.socket;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import com.betfair.cougar.netutil.nio.marshalling.SocketContextResolutionParams;
import com.betfair.cougar.netutil.nio.marshalling.SocketRequestUuidResolver;

import java.util.Map;

/**
 * Default HTTP UUID resolver. Uses the uuid and uuidParents headers to resolve uuids.
 */
public class ZipkinSocketRequestUuidResolver<Void> extends SocketRequestUuidResolver<Void> {
    private final ZipkinManager zipkinManager;

    public ZipkinSocketRequestUuidResolver(ZipkinManager zipkinManager) {
        this.zipkinManager = zipkinManager;
    }

    @Override
    public void resolve(SocketContextResolutionParams params, Void ignore, DehydratedExecutionContextBuilder builder) {
        RequestUUID cougarUuid = super.resolve(params);

        String traceId = null;
        String spanId = null;
        String parentSpanId = null;
        Map<String, String> additionalData = params.getAdditionalData();

        if (additionalData != null) {
            traceId = additionalData.get(ZipkinManager.TRACE_ID_KEY);
            spanId = additionalData.get(ZipkinManager.SPAN_ID_KEY);
            parentSpanId = additionalData.get(ZipkinManager.PARENT_SPAN_ID_KEY);
        }

        RequestUUID requestUUID = zipkinManager.createNewZipkinRequestUUID(cougarUuid, traceId, spanId, parentSpanId);
        builder.setRequestUUID(requestUUID);
    }
}