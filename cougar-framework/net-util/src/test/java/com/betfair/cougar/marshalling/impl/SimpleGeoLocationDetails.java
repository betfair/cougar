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

package com.betfair.cougar.marshalling.impl;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;

import java.util.Collections;
import java.util.List;

public class SimpleGeoLocationDetails implements GeoLocationDetails {
    private List<String> resolvedAddress;
    private String inferredCountry;

    public SimpleGeoLocationDetails(String resolvedAddress) {
        this.resolvedAddress = Collections.singletonList(resolvedAddress);
    }

    public SimpleGeoLocationDetails(List<String> resolvedAddresses) {
        this.resolvedAddress = resolvedAddresses;
    }

    public SimpleGeoLocationDetails(List<String> resolvedAddress, String inferredCountry) {
        this.resolvedAddress = resolvedAddress;
        this.inferredCountry = inferredCountry;
    }

    public String getRemoteAddr() {
        throw new IllegalStateException("Should not be called");
    }

    @Override
    public List<String> getResolvedAddresses() {
        return resolvedAddress;
    }

    public String getCountry() {
        throw new IllegalStateException("Should not be called");
    }

    public String getLocation() {
        throw new IllegalStateException("Should not be called");
    }

    @Override
    public String getInferredCountry() {
        return inferredCountry;
    }

    public boolean isLowConfidenceGeoLocation() {
        throw new IllegalStateException("Should not be called");
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoLocationDetails)) return false;

        SimpleGeoLocationDetails that = (SimpleGeoLocationDetails) o;

        if (resolvedAddress != null ? !resolvedAddress.equals(that.resolvedAddress) : that.resolvedAddress != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return resolvedAddress != null ? resolvedAddress.hashCode() : 0;
    }
}
