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

package com.betfair.cougar.api.geolocation;


import java.util.List;

public interface GeoLocationDetails {
	/**
	 * The direct address the request originated from
	 */
	String getRemoteAddr();

	/**
	 * <p>The originating IP address and the IP addresses of any proxies the request has passed through</p>
     * <p>The originating IP address (the customer) will be the first entry in the list, subsequent proxies will append to the list.</p>
     * <p>This information may obtained from places such as the X-Forwarded-For header (if present) when using HTTP. If no alternate source exists then there will be one entry that is equal to {@link #getRemoteAddr()}</p>
	 */
	List<String> getResolvedAddresses();

	/**
	 * The country in which the resolved address has been located
	 */
	String getCountry();

	/**
	 * @return true if we don't have much confidence in the geoLocated Country.  This can
	 * happen due to ISPs such as AOL that span countries, or if we can't resolve the remote address
	 */
	boolean isLowConfidenceGeoLocation();


	/**
	 * The accurate location in which the resolved address has been located
	 */
	String getLocation();


    /**
     * Returns the inferred country
     */
    String getInferredCountry();
}
