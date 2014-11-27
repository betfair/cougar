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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 * Zipkin factory for EC resolvers for HTTP transports.
 */
public class ZipkinExecutionContextResolverFactory implements DehydratedExecutionContextResolverFactory {

    private String cougarUUIDHeader;

    public void setCougarUUIDHeader(String cougarUUIDHeader) {
        this.cougarUUIDHeader = cougarUUIDHeader;
    }

    @Override
    public <T, B> DehydratedExecutionContextResolver<T, B>[] resolvers(Protocol protocol) {
        if (protocol == Protocol.SOAP) {
            return new DehydratedExecutionContextResolver[]{
                    (DehydratedExecutionContextResolver<T, B>) new ZipkinHttpRequestUuidResolver<>(cougarUUIDHeader),
            };
        }
        if (protocol.underlyingTransportIsHttp()) { // && HttpServletRequest.class.isAssignableFrom(transportClass)) { todo: #82: do we need this?
            return new DehydratedExecutionContextResolver[]{
                    (DehydratedExecutionContextResolver<T, B>) new ZipkinHttpRequestUuidResolver<>(cougarUUIDHeader),
            };
        }
        // i can't handle other protocols
        return null;
    }

    @Override
    public String getName() {
        return "Zipkin HTTP ContextResolverFactory";
    }
}