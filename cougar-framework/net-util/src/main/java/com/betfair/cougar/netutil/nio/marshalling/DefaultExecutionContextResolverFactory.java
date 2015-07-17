/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.netutil.nio.marshalling;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.util.geolocation.GeoIPLocator;

import java.util.Date;
import java.util.Set;

/**
 * Default resolver factory for the socket transport
 */
public class DefaultExecutionContextResolverFactory implements DehydratedExecutionContextResolverFactory {

    private final GeoIPLocator geoIpLocator;
    private final RequestTimeResolver<Long> requestTimeResolver;

    public DefaultExecutionContextResolverFactory(GeoIPLocator geoIpLocator, RequestTimeResolver<Long> requestTimeResolver) {
        this.geoIpLocator = geoIpLocator;
        this.requestTimeResolver = requestTimeResolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, B> DehydratedExecutionContextResolver<T, B>[] resolvers(Protocol protocol) {
        if (protocol == Protocol.SOCKET) {
            return new DehydratedExecutionContextResolver[]{
                    new SocketResolver(),
                    new SocketRequestUuidResolver()
            };
        }
        return null;
    }

    private class SocketResolver implements DehydratedExecutionContextResolver<SocketContextResolutionParams, Void> {
        private Set<DehydratedExecutionContextComponent> handling;

        @Override
        public DehydratedExecutionContextComponent[] supportedComponents() {
            return new DehydratedExecutionContextComponent[]{
                    DehydratedExecutionContextComponent.IdentityTokens,
                    DehydratedExecutionContextComponent.Location,
                    DehydratedExecutionContextComponent.ReceivedTime,
                    DehydratedExecutionContextComponent.RequestedTime,
                    DehydratedExecutionContextComponent.TraceLoggingEnabled,
                    DehydratedExecutionContextComponent.TransportSecurityStrengthFactor
            };
        }

        @Override
        public void resolving(Set<DehydratedExecutionContextComponent> handling) {
            this.handling = handling;
        }

        @Override
        public void resolve(SocketContextResolutionParams params, Void ignore, DehydratedExecutionContextBuilder builder) {
            if (handling.contains(DehydratedExecutionContextComponent.IdentityTokens)) {
                builder.setIdentityTokens(params.getTokens());
            }
            if (handling.contains(DehydratedExecutionContextComponent.Location)) {
                GeoLocationParameters geoParams = params.getGeo();
                builder.setLocation(geoIpLocator.getGeoLocation(geoParams.getRemoteAddress(), geoParams.getAddressList(), geoParams.getInferredCountry()));
            }
            if (handling.contains(DehydratedExecutionContextComponent.ReceivedTime)) {
                builder.setReceivedTime(new Date());
            }
            if (handling.contains(DehydratedExecutionContextComponent.RequestedTime)) {
                builder.setRequestTime(requestTimeResolver.resolveRequestTime(params.getRequestTime()));
            }
            if (handling.contains(DehydratedExecutionContextComponent.TraceLoggingEnabled)) {
                builder.setTraceLoggingEnabled(params.isTraceEnabled());
            }
            if (handling.contains(DehydratedExecutionContextComponent.TransportSecurityStrengthFactor)) {
                builder.setTransportSecurityStrengthFactor(params.getTransportSecurityStrengthFactor());
            }
        }
    }

    @Override
    public String getName() {
        return "Default Socket ContextResolverFactory";
    }
}
