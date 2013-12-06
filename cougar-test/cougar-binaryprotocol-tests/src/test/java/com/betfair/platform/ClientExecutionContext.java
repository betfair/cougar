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

package com.betfair.platform;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.Credential;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.util.RequestUUIDImpl;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A client side execution Context for connecting to a standard platform-style service.
 */
public class ClientExecutionContext implements ExecutionContext {

    private GeoLocationDetails geolocation;
    private Date receivedTime;
    private Date requestTime;
    private RequestUUIDImpl requestUUID;
    private boolean traceEnabled;
    private IdentityChainImpl identityChain;

    /* Package Private */ ClientExecutionContext(final String applicationKey,
                                                 final String customersIPAddress,
                                                 final String sessionToken,
                                                 final String adminSessionToken,
                                                 final Date receivedTime,
                                                 final Date requestTime,
                                                 final String geoLocatedCountry,
                                                 final String inferredCountry,
                                                 final boolean traceEnabled) {

        this.traceEnabled = traceEnabled;
        this.receivedTime = receivedTime;
        this.requestTime = requestTime;
        this.requestUUID = new RequestUUIDImpl();

        geolocation = new GeoLocationDetails() {
            public boolean isLowConfidenceGeoLocation() {
                return false; // decided by cougar on the client side
            }

            public String getCountry() {
                return geoLocatedCountry;
            }

            public String getLocation() {
                return null; // The location in the country
            }

            @Override
            public String getInferredCountry() {
                return inferredCountry;
            }

            public String getRemoteAddr() {
                return customersIPAddress;
            }

            public List<String> getResolvedAddresses() {
                return customersIPAddress == null ? Collections.<String>emptyList() : Collections.singletonList(customersIPAddress);
            }

        };
        identityChain = new IdentityChainImpl();
        if (sessionToken != null) {
            identityChain.addIdentity(new SimpleIdentity(Constants.AUTHENTICATION_HEADER, sessionToken));
        }
        if (adminSessionToken != null) {
            identityChain.addIdentity(new SimpleIdentity(Constants.ADMIN_AUTHENTICATION_HEADER, adminSessionToken));
        }
        if (applicationKey != null) {
            identityChain.addIdentity(new SimpleIdentity(Constants.APPLICATION_KEY_HEADER, applicationKey));
        }
        if (inferredCountry != null) {
            // We need to communicate this to the remote server, and the identity chain is the only way.
            // So create an identity with for the InferredCountry, that can be serialised by the Token Resolver
            // if required. This is a bit ugly as the country is not actually part of the identity
            identityChain.addIdentity(new SimpleIdentity(Constants.INFERRED_COUNTRY, inferredCountry));
        }
    }

    public IdentityChain getIdentity() {
        return identityChain;
    }

    public GeoLocationDetails getLocation() {
        return geolocation;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public RequestUUID getRequestUUID() {
        return requestUUID;
    }

    public boolean traceLoggingEnabled() {
        return traceEnabled;
    }

    @Override
    public int getTransportSecurityStrengthFactor() {
        return 0;
    }

    @Override
    public boolean isTransportSecure() {
        return false;
    }

    @Override
    public String toString() {
        return "ClientExecutionContext{" +
                "geolocation=" + geolocation +
                ", receivedTime=" + receivedTime +
                ", requestUUID=" + requestUUID +
                ", traceEnabled=" + traceEnabled +
                ", identityChain=" + identityChain +
                '}';
    }

    private static class SimpleIdentity implements Identity{
        private Credential credential;

        private SimpleIdentity(String name, String value) {
            this.credential = new SimpleCredential(name, value);
        }

        public Credential getCredential() {
            return credential;
        }

        public Principal getPrincipal() {
            return null;
        }

        private static class SimpleCredential implements Credential {
            private String value;
            private String name;

            public SimpleCredential(String name, String value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public Object getValue() {
                return value;
            }

            @Override
            public String toString() {
                return "SimpleCredential{" +
                        "value='" + value + '\'' +
                        ", name='" + name + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "SimpleIdentity{" +
                    "credential=" + credential +
                    '}';
        }
    }
}
