/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.client;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;

import java.util.Date;

/**
 * Elements of the ExecutionContext which are required information for a client call.
 * @see com.betfair.cougar.api.ExecutionContext
 */
public interface ClientCallContext {
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

    /**
     * The RequestUUID for the request. This will either be a sub-uuid of the uuid in the parent
     * ExecutionContext (the one passed into the client call) or a new uuid (if that was null).
     */
    public RequestUUID getRequestUUID();

    /**
     * The time this request was emitted by the client.
     */
    public Date getRequestTime();

    /**
     * Whether to enable trace logging on the service receiving this request.
     */
    public boolean traceLoggingEnabled();
}
