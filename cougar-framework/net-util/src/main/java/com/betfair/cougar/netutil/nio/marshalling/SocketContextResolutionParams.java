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

import com.betfair.cougar.api.security.IdentityToken;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Parameters for resolving the execution context for the socket transport. Order of reading data from the transport is critical to maintain
 * backwards compatibility of the protocol, so all data is extracted by the marshaller and only then provided to the context resolvers.
 */
public class SocketContextResolutionParams {
    private final List<IdentityToken> tokens;
    private final String uuid;
    private final GeoLocationParameters geo;
    private final Date receivedTime;
    private final boolean traceEnabled;
    private final int transportSecurityStrengthFactor;
    private final Long requestTime;
    private final Map<String, String> additionalData;

    public SocketContextResolutionParams(List<IdentityToken> tokens, String uuid, GeoLocationParameters geo, Date receivedTime, boolean traceEnabled, int transportSecurityStrengthFactor, Long requestTime, Map<String,String> additionalData) {
        this.tokens = tokens;
        this.uuid = uuid;
        this.geo = geo;
        this.receivedTime = receivedTime;
        this.traceEnabled = traceEnabled;
        this.transportSecurityStrengthFactor = transportSecurityStrengthFactor;
        this.requestTime = requestTime;
        this.additionalData = additionalData;
    }

    public List<IdentityToken> getTokens() {
        return tokens;
    }

    public String getUuid() {
        return uuid;
    }

    public GeoLocationParameters getGeo() {
        return geo;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public int getTransportSecurityStrengthFactor() {
        return transportSecurityStrengthFactor;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }
}
