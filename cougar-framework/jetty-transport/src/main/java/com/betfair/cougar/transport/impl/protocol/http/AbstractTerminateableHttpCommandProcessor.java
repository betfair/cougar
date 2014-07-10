/*
 * Copyright 2013, The Sporting Exchange Limited
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

import com.betfair.cougar.api.security.InferredCountryResolver;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.util.geolocation.GeoIPLocator;

import javax.servlet.http.HttpServletRequest;

/**
 * Used to form the abstract base of a command processor that will cease execution
 * of commands as soon as an error condition is encountered.
 */
public abstract class AbstractTerminateableHttpCommandProcessor extends AbstractHttpCommandProcessor {
    /**
	 *
	 * @param geoIPLocator
	 *            Used for resolving the GeoLocationDetails
	 * @param deserializer
	 *            the bean to extract the Http Header(s) containing the users's IP address(es)
	 * @param uuidHeader
	 *            the key of the Http Header containing the unique id for a request
	 */
	public AbstractTerminateableHttpCommandProcessor(GeoIPLocator geoIPLocator,
                                                     GeoLocationDeserializer deserializer, String uuidHeader, String uuidParentsHeader, InferredCountryResolver<HttpServletRequest> countryResolver,
                                                     String requestTimeoutHeader, RequestTimeResolver requestTimeResolver) {
        super(geoIPLocator, deserializer, uuidHeader, uuidParentsHeader, requestTimeoutHeader, requestTimeResolver, countryResolver);
	}
}
