/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 * Zipkin factory for EC resolvers for HTTP and socket transports.
 */
public class ZipkinExecutionContextResolverFactory implements DehydratedExecutionContextResolverFactory {

    private DehydratedExecutionContextResolver httpRequestResolver;
    private DehydratedExecutionContextResolver socketRequestResolver;

    public void setHttpRequestResolver(DehydratedExecutionContextResolver httpRequestResolver) {
        if (this.httpRequestResolver != null && httpRequestResolver != null) {
            throw new IllegalStateException("Zipkin HTTP Request Resolver is already assigned");
        }
        this.httpRequestResolver = httpRequestResolver;
    }

    public void setSocketRequestResolver(DehydratedExecutionContextResolver socketRequestResolver) {
        if (this.socketRequestResolver != null && socketRequestResolver != null) {
            throw new IllegalStateException("Zipkin Socket Request Resolver is already assigned");
        }
        this.socketRequestResolver = socketRequestResolver;
    }

    @Override
    public <T, B> DehydratedExecutionContextResolver<T, B>[] resolvers(Protocol protocol) {
        if (protocol.underlyingTransportIsHttp() && httpRequestResolver != null) {
            return new DehydratedExecutionContextResolver[]{ httpRequestResolver };
        }
        if (protocol == Protocol.SOCKET && socketRequestResolver != null) {
            return new DehydratedExecutionContextResolver[]{ socketRequestResolver };
        }
        // I can't handle other protocols
        return null;
    }

    @Override
    public String getName() {
        return "Zipkin ContextResolverFactory";
    }
}
