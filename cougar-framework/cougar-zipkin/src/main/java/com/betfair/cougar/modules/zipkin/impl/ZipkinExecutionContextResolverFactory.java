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
import com.betfair.cougar.modules.zipkin.impl.jetty.ZipkinHttpRequestUuidResolver;
import com.betfair.cougar.modules.zipkin.impl.socket.ZipkinSocketRequestUuidResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 * Zipkin factory for EC resolvers for HTTP transports.
 */
public class ZipkinExecutionContextResolverFactory implements DehydratedExecutionContextResolverFactory {

    private String cougarUUIDHeader;
    private String uuidParentsHeader;
    private int socketServerPort = 0;
    private ZipkinManager zipkinManager;

    public void setCougarUUIDHeader(String cougarUUIDHeader) {
        this.cougarUUIDHeader = cougarUUIDHeader;
    }

    public void setUuidParentsHeader(String uuidParentsHeader) {
        this.uuidParentsHeader = uuidParentsHeader;
    }

    public void setZipkinManager(ZipkinManager zipkinManager) {
        this.zipkinManager = zipkinManager;
    }

    public void setSocketServerPort(int socketServerPort) {
        this.socketServerPort = socketServerPort;
    }

    @Override
    public <T, B> DehydratedExecutionContextResolver<T, B>[] resolvers(Protocol protocol) {
        if (protocol.underlyingTransportIsHttp()) {
            return new DehydratedExecutionContextResolver[]{
                    (DehydratedExecutionContextResolver<T, B>) new ZipkinHttpRequestUuidResolver<>(cougarUUIDHeader, uuidParentsHeader, zipkinManager)
            };
        }
        if (protocol == Protocol.SOCKET) {
            return new DehydratedExecutionContextResolver[] {
                    (DehydratedExecutionContextResolver<T, B>) new ZipkinSocketRequestUuidResolver<>(zipkinManager, socketServerPort)
            };
        }
        // I can't handle other protocols
        return null;
    }

    @Override
    public String getName() {
        return "Zipkin ContextResolverFactory";
    }
}