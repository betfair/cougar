package com.betfair.cougar.transport.api;

import com.betfair.cougar.api.export.Protocol;

/**
 *
 */
public interface DehydratedExecutionContextResolverFactory {
    <T,B> DehydratedExecutionContextResolver<T, B>[] resolvers(Protocol protocol);
    String getName();
}
