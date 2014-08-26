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

package com.betfair.cougar.util.geolocation;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;

import java.util.List;

public interface GeoIPLocator {


    /**
     * Return a GeoLocationDetails object for the provided parameters.
     *
     * @param remoteIP The direct remote address of the client
     * @param resolvedIPs The customer and proxy addresses (if any) used to connect to the server.  the customer's address is always the first
     * @param inferredCountry the country inferred from the InferredCountryResolver implementation, can be null
     */
    public GeoLocationDetails getGeoLocation(final String remoteIP, final List<String> resolvedIPs, final String inferredCountry);
}


