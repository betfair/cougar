package com.betfair.cougar.modules.zipkin.impl.socket;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import com.betfair.cougar.netutil.nio.marshalling.SocketContextResolutionParams;
import com.betfair.cougar.netutil.nio.marshalling.SocketRequestUuidResolver;

import java.util.Map;

/**
 * Default HTTP UUID resolver. Uses the uuid and uuidParents headers to resolve uuids.
 */
public class ZipkinSocketRequestUuidResolver<Void> extends SocketRequestUuidResolver<Void> {

    private final ZipkinManager zipkinManager;

    private final int serverPort;

    public ZipkinSocketRequestUuidResolver(ZipkinManager zipkinManager, int socketServerPort) {
        this.zipkinManager = zipkinManager;
        this.serverPort = socketServerPort;
    }

    @Override
    public void resolve(SocketContextResolutionParams params, Void ignore, DehydratedExecutionContextBuilder builder) {
        RequestUUID cougarUUID = super.resolve(params);

        String traceId = null;
        String spanId = null;
        String parentSpanId = null;
        String sampled = null;
        String flags = null;
        Map<String, String> additionalData = params.getAdditionalData();

        if (additionalData != null) {
            traceId = additionalData.get(ZipkinKeys.TRACE_ID);
            spanId = additionalData.get(ZipkinKeys.SPAN_ID);
            parentSpanId = additionalData.get(ZipkinKeys.PARENT_SPAN_ID);
            sampled = additionalData.get(ZipkinKeys.SAMPLED);
            flags = additionalData.get(ZipkinKeys.FLAGS);
        }

        RequestUUID requestUUID = zipkinManager.createNewZipkinRequestUUID(cougarUUID, traceId, spanId, parentSpanId,
                sampled, flags, serverPort);
        builder.setRequestUUID(requestUUID);
    }
}
