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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.InferredCountryResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * Default factory for EC resolvers for HTTP transports.
 */
public class DefaultExecutionContextResolverFactory implements DehydratedExecutionContextResolverFactory {

    private int unknownCipherKeyLength;
    private RequestTimeResolver<HttpServletRequest> requestTimeResolver;
    private GeoIPLocator geoIPLocator;
    private GeoLocationDeserializer geoLocationDeserializer;
    private InferredCountryResolver<HttpServletRequest> inferredCountryResolver;
    private String uuidHeader;
    private String uuidParentsHeader;

    @ManagedAttribute
    public int getUnknownCipherKeyLength() {
        return unknownCipherKeyLength;
    }

    public void setUnknownCipherKeyLength(int unknownCipherKeyLength) {
        this.unknownCipherKeyLength = unknownCipherKeyLength;
    }

    public void setRequestTimeResolver(RequestTimeResolver<HttpServletRequest> requestTimeResolver) {
        this.requestTimeResolver = requestTimeResolver;
    }

    public void setGeoIPLocator(GeoIPLocator geoIPLocator) {
        this.geoIPLocator = geoIPLocator;
    }

    public void setGeoLocationDeserializer(GeoLocationDeserializer geoLocationDeserializer) {
        this.geoLocationDeserializer = geoLocationDeserializer;
    }

    public void setInferredCountryResolver(InferredCountryResolver<HttpServletRequest> inferredCountryResolver) {
        this.inferredCountryResolver = inferredCountryResolver;
    }

    public void setUuidHeader(String uuidHeader) {
        this.uuidHeader = uuidHeader;
    }

    public void setUuidParentsHeader(String uuidParentsHeader) {
        this.uuidParentsHeader = uuidParentsHeader;
    }

    @Override
    public <T,B> DehydratedExecutionContextResolver<T, B>[] resolvers(Protocol protocol) {
        if (protocol == Protocol.SOAP) {
            return new DehydratedExecutionContextResolver[] {
                (DehydratedExecutionContextResolver<T, B>) new SoapIdentityTokenResolver(),
                (DehydratedExecutionContextResolver<T, B>) new HttpLocationResolver<>(geoIPLocator, geoLocationDeserializer, inferredCountryResolver),
                (DehydratedExecutionContextResolver<T, B>) new HttpReceivedTimeResolver<>(),
                (DehydratedExecutionContextResolver<T, B>) new HttpRequestedTimeResolver<>(requestTimeResolver),
                (DehydratedExecutionContextResolver<T, B>) new HttpRequestUuidResolver<>(uuidHeader, uuidParentsHeader),
                (DehydratedExecutionContextResolver<T, B>) new HttpTraceLoggingResolver<>(),
                (DehydratedExecutionContextResolver<T, B>) new HttpTransportStrengthResolver<>(unknownCipherKeyLength)
            };
        }
        if (protocol.underlyingTransportIsHttp()) { // && HttpServletRequest.class.isAssignableFrom(transportClass)) { todo: #82: do we need this?
            return new DehydratedExecutionContextResolver[] {
                    (DehydratedExecutionContextResolver<T, B>) new HttpIdentityTokenResolver(),
                    (DehydratedExecutionContextResolver<T, B>) new HttpLocationResolver<>(geoIPLocator, geoLocationDeserializer, inferredCountryResolver),
                    (DehydratedExecutionContextResolver<T, B>) new HttpReceivedTimeResolver<>(),
                    (DehydratedExecutionContextResolver<T, B>) new HttpRequestedTimeResolver<>(requestTimeResolver),
                    (DehydratedExecutionContextResolver<T, B>) new HttpRequestUuidResolver<>(uuidHeader, uuidParentsHeader),
                    (DehydratedExecutionContextResolver<T, B>) new HttpTraceLoggingResolver<>(),
                    (DehydratedExecutionContextResolver<T, B>) new HttpTransportStrengthResolver<>(unknownCipherKeyLength)
            };
        }
        // i can't handle other protocols
        return null;
    }

    @Override
    public String getName() {
        return "Default HTTP ContextResolverFactory";
    }

}
