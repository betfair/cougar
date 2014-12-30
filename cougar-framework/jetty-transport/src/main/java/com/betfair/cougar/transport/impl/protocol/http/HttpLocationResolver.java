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

import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.InferredCountryResolver;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.geolocation.GeoIPLocator;

import javax.servlet.http.HttpServletRequest;

/**
 * Default HTTP location resolution. Delegates to a given geoip locator, passing a deserialised location and optionally an
 * inferred country.
 */
public class HttpLocationResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    private final GeoIPLocator geoIPLocator;
    private final GeoLocationDeserializer geoLocationDeserializer;
    private final InferredCountryResolver<HttpServletRequest> inferredCountryResolver;

    public HttpLocationResolver(GeoIPLocator geoIPLocator, GeoLocationDeserializer geoLocationDeserializer, InferredCountryResolver<HttpServletRequest> inferredCountryResolver) {
        super(DehydratedExecutionContextComponent.Location);
        this.geoIPLocator = geoIPLocator;
        this.geoLocationDeserializer = geoLocationDeserializer;
        this.inferredCountryResolver = inferredCountryResolver;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        String inferredCountry = null;
        if (inferredCountryResolver != null) {
            inferredCountry = inferredCountryResolver.inferCountry(httpCommand.getRequest());
        }
        GeoLocationDetails geoDetails = geoIPLocator.getGeoLocation(httpCommand.getRequest().getRemoteAddr(), geoLocationDeserializer.deserialize(httpCommand.getRequest(), httpCommand.getRequest().getRemoteAddr()), inferredCountry);
        builder.setLocation(geoDetails);
    }
}
