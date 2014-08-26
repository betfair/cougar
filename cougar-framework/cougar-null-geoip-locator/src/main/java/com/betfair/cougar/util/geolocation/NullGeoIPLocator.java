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
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.List;

/**
 *
 */
@ManagedResource(description = "No-op implementation of geo-location")
public class NullGeoIPLocator implements GeoIPLocator {
    @Override
    public GeoLocationDetails getGeoLocation(final String remoteIP, final List<String> resolvedIPs, final String inferredCountry) {
        return new GeoLocationDetails() {
            @Override
            public String getRemoteAddr() {
                return remoteIP;
            }

            @Override
            public List<String> getResolvedAddresses() {
                return resolvedIPs;
            }

            @Override
            public String getCountry() {
                return inferredCountry;
            }

            @Override
            public boolean isLowConfidenceGeoLocation() {
                return false;
            }

            @Override
            public String getLocation() {
                return inferredCountry;
            }

            @Override
            public String getInferredCountry() {
                return inferredCountry;
            }
        };
    }
}
