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

package com.betfair.cougar.transport.api.protocol.http;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generate an execution context
 * @deprecated In favour of ExecutionContextBuilder
 */
@Deprecated
public class ExecutionContextFactory {

    public static final String TRACE_ME_HEADER_PARAM = "X-Trace-Me";


    public static DehydratedExecutionContext resolveExecutionContext(final HttpCommand command, final List<IdentityToken> tokens,
                                                       final String uuidHeader, final String uuidParentsHeader, GeoLocationDeserializer geoLocationDeserializer,
                                                       final GeoIPLocator geoIPLocator,
                                                       final String inferredCountry,
                                                       final int transportSecurityStrengthFactor, final boolean ignoreSubsequentWritesOfIdentity, Date requestTime) {
        final HttpServletRequest request = command.getRequest();
        String uuidString = request.getHeader(uuidHeader);
        String uuidParentsString = request.getHeader(uuidParentsHeader);

        return resolveExecutionContext(tokens, uuidString, uuidParentsString, request.getRemoteAddr(), geoLocationDeserializer.deserialize(request, request.getRemoteAddr()), geoIPLocator, inferredCountry, request.getHeader(TRACE_ME_HEADER_PARAM), transportSecurityStrengthFactor, ignoreSubsequentWritesOfIdentity, requestTime);
    }

    private static DehydratedExecutionContext resolveExecutionContext(List<IdentityToken> tokens,
                                                       final String uuidString,
                                                       final String uuidParentsString,
                                                       final String remoteAddress,
                                                       final List<String> resolvedAddresses,
                                                       final GeoIPLocator geoIPLocator,
                                                       final String inferredCountry,
                                                       final String traceMeHeaderParamValue,
                                                       final int transportSecurityStrengthFactor,
                                                       final boolean ignoreSubsequentWritesOfIdentity,
                                                       final Date requestTime) {
        if (tokens == null) {
            tokens = new ArrayList<IdentityToken>();
        }
        final Date receivedTime = new Date();
        final RequestUUID requestUUID;
        if (uuidString != null) {
            if (StringUtils.isNotBlank(uuidParentsString)) {
                requestUUID = new RequestUUIDImpl(uuidParentsString + UUIDGenerator.COMPONENT_SEPARATOR + uuidString);
            }
            else {
                requestUUID = new RequestUUIDImpl(uuidString);
            }
        } else {
            requestUUID = new RequestUUIDImpl();
        }
        final boolean traceEnabled = traceMeHeaderParamValue != null;

        GeoLocationDetails geoDetails = geoIPLocator.getGeoLocation(remoteAddress, resolvedAddresses, inferredCountry);

        return resolveExecutionContext(tokens, requestUUID, geoDetails, receivedTime, traceEnabled, transportSecurityStrengthFactor, requestTime, ignoreSubsequentWritesOfIdentity);
    }

    public static DehydratedExecutionContext resolveExecutionContext(final List<IdentityToken> tokens, final RequestUUID requestUUID, final GeoLocationDetails geoDetails, final Date receivedTime, final boolean traceEnabled, final int transportSecurityStrengthFactor, final Date requestTime, final boolean ignoreSubsequentWritesOfIdentity) {
        if (tokens == null) {
            throw new IllegalArgumentException("Tokens must not be null");
        }

        return new DehydratedExecutionContext() {

            private IdentityChain identityChain;

            @Override
            public List<IdentityToken> getIdentityTokens() {
                return tokens;
            }

            @Override
            public IdentityChain getIdentity() {
                return identityChain;
            }

            @Override
            public GeoLocationDetails getLocation() {
                return geoDetails;
            }

            @Override
            public RequestUUID getRequestUUID() {
                return requestUUID;
            }

            @Override
            public Date getReceivedTime() {
                return receivedTime;
            }

            @Override
            public boolean traceLoggingEnabled() {
                return traceEnabled;
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return transportSecurityStrengthFactor;
            }

            @Override
            public boolean isTransportSecure() {
                return transportSecurityStrengthFactor > 1;
            }

            @Override
            public void setIdentityChain(IdentityChain chain) {
                if (identityChain != null && !ignoreSubsequentWritesOfIdentity) {
                    throw new IllegalStateException("Can't overwrite identity chain once set");
                }
                identityChain = chain;
            }

            @Override
            public Date getRequestTime() {
                return requestTime;
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();

                sb.append("geoLocationDetails=").append(getLocation()).append("|");
                sb.append("tokens=").append(tokens).append("|");
                sb.append("requestUUID=").append(requestUUID).append("|");
                sb.append("receivedTime=").append(receivedTime).append("|");
                sb.append("requestTime=").append(requestTime).append("|");
                sb.append("traceLoggingEnabled=").append(traceEnabled);

                return sb.toString();
            }
        };
    }

    public static ExecutionContext resolveExecutionContext(final DehydratedExecutionContext tokenContext, final IdentityChain chain) {
        return new ExecutionContext() {
            @Override
            public GeoLocationDetails getLocation() {
                return tokenContext.getLocation();
            }

            @Override
            public IdentityChain getIdentity() {
                return chain;
            }

            @Override
            public RequestUUID getRequestUUID() {
                return tokenContext.getRequestUUID();
            }

            @Override
            public Date getReceivedTime() {
                return tokenContext.getReceivedTime();
            }

            @Override
            public boolean traceLoggingEnabled() {
                return tokenContext.traceLoggingEnabled();
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return tokenContext.getTransportSecurityStrengthFactor();
            }

            @Override
            public boolean isTransportSecure() {
                return tokenContext.isTransportSecure();
            }

            @Override
            public Date getRequestTime() {
                return tokenContext.getRequestTime();
            }
        };
    }
}
