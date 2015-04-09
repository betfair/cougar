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
import com.betfair.cougar.modules.zipkin.impl.http.ZipkinHttpRequestUuidResolver;
import com.betfair.cougar.modules.zipkin.impl.socket.ZipkinSocketRequestUuidResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 * Zipkin factory for EC resolvers for HTTP and socket transports.
 */
public class ZipkinExecutionContextResolverFactory implements DehydratedExecutionContextResolverFactory {

    private String cougarUUIDHeader;
    private String uuidParentsHeader;
    private int socketServerPort = 0;
    private ZipkinManager zipkinManager;

    /**
     * Sets the value of the Cougar UUID header to be used by the EC resolver.
     *
     * @param cougarUUIDHeader The Cougar UUID header
     */
    public void setCougarUUIDHeader(String cougarUUIDHeader) {
        this.cougarUUIDHeader = cougarUUIDHeader;
    }

    /**
     * Sets the value of the Cougar UUID parents header to be used by the EC resolver.
     *
     * @param uuidParentsHeader The Cougar UUID parents header
     */
    public void setUuidParentsHeader(String uuidParentsHeader) {
        this.uuidParentsHeader = uuidParentsHeader;
    }

    /**
     * Sets the ZipkinManager to be used by the EC resolver.
     *
     * @param zipkinManager The ZipkinManager to be used
     */
    public void setZipkinManager(ZipkinManager zipkinManager) {
        this.zipkinManager = zipkinManager;
    }

    /**
     * Sets the port of the (socket) server.
     *
     * @param socketServerPort The server port
     */
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
            return new DehydratedExecutionContextResolver[]{
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