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

/**
 * 
 */
package com.betfair.cougar.api;

import java.util.Date;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;

/**
 * Provides contextual data to a cougar service
 *
 */
public interface ExecutionContext {

	/**
	 * Get the physical location details from which the user is accessing the service 
	 * @return the user's physical location
	 */
	public GeoLocationDetails getLocation();
	
	/**
	 * Get the identity of the user. The multiple principals represent each
	 * link in the identity chain, e.g. User X (Principal 0) is using a third party
	 * application (Principal 1) to access the API (Principal 3)
	 * @return A list of principals to identify the user
	 */
	public IdentityChain getIdentity();

	public RequestUUID getRequestUUID();
	
	public Date getReceivedTime();

    public boolean traceLoggingEnabled();

    /**
     * Gets an indicator of the security strength of the transport this request was received over.
     * A value of zero indicates no security, a value of one indicates integrity checking. Any other value
     * gives the length of the encryption key in bits. See also <a href="http://docs.oracle.com/cd/E19082-01/819-2145/sasl.intro-44/index.html">SASL Security Strength Factor</a>.
     * @return The security strength factor for the transport
     */
    int getTransportSecurityStrengthFactor();

    /**
     * Convenience method for checking whether the transport this request was received over was secure.
     * Equivalent to <code>(getTransportSecurityStrengthFactor() &gt; 1)</code>
     */
    boolean isTransportSecure();
}
