package com.betfair.cougar.transport.api;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 *
 */
public interface DehydratedExecutionContextResolution {
    void registerFactory(DehydratedExecutionContextResolverFactory factory);

    <T,C> DehydratedExecutionContext resolveExecutionContext(Protocol protocol, T transport, C credentialsContainer);
}
