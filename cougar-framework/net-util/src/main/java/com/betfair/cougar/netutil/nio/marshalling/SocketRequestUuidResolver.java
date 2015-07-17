package com.betfair.cougar.netutil.nio.marshalling;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.util.RequestUUIDImpl;

/**
 * Default Socket UUID resolver.
 */
public class SocketRequestUuidResolver<Void> extends SingleComponentResolver<SocketContextResolutionParams, Void> {

    public SocketRequestUuidResolver() {
        super(DehydratedExecutionContextComponent.RequestUuid);
    }

    @Override
    public void resolve(SocketContextResolutionParams params, Void ignore, DehydratedExecutionContextBuilder builder) {
        RequestUUID requestUUID = resolve(params);
        builder.setRequestUUID(requestUUID);
    }

    protected RequestUUID resolve(SocketContextResolutionParams params) {
        RequestUUID requestUUID;
        if (params.getUuid() != null) {
            requestUUID = new RequestUUIDImpl(params.getUuid());
        } else {
            requestUUID = new RequestUUIDImpl();
        }
        return requestUUID;
    }
}