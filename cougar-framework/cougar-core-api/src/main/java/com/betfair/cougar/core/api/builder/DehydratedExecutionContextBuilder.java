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

package com.betfair.cougar.core.api.builder;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;

import java.util.Date;
import java.util.List;

/**
 * Builds DehydratedExecutionContext
 */
public class DehydratedExecutionContextBuilder extends BaseExecutionContextBuilder<DehydratedExecutionContextBuilder> implements Builder<DehydratedExecutionContext> {
    private List<IdentityToken> tokens;

    @Override
    protected int getNumSpecificComponents() {
        return 1;
    }

    public DehydratedExecutionContextBuilder setIdentityTokens(List<IdentityToken> tokens) {
        this.tokens = tokens;
        beenSet(0);
        return this;
    }

    public DehydratedExecutionContextBuilder setIdentityTokens(ListBuilder<IdentityToken> tokens) {
        this.tokens = tokens.build();
        beenSet(0);
        return this;
    }

    public List<IdentityToken> getIdentityTokens() {
        return tokens;
    }

    @Override
    public DehydratedExecutionContext build() {
        checkReady();
        return new DehydratedExecutionContextImpl();
    }

    private class DehydratedExecutionContextImpl implements DehydratedExecutionContext {
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
            return location;
        }

        @Override
        public RequestUUID getRequestUUID() {
            return uuid;
        }

        @Override
        public Date getReceivedTime() {
            return receivedTime;
        }

        @Override
        public boolean traceLoggingEnabled() {
            return traceLoggingEnabled;
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
            if (identityChain != null) {
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
            sb.append("requestUUID=").append(uuid).append("|");
            sb.append("receivedTime=").append(receivedTime).append("|");
            sb.append("requestTime=").append(requestTime).append("|");
            sb.append("traceLoggingEnabled=").append(traceLoggingEnabled);

            return sb.toString();
        }
    }
}
