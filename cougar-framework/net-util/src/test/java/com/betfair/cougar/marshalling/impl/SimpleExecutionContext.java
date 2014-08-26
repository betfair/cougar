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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.util.RequestUUIDImpl;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * simple execution context impl for unit tests
 */
public class SimpleExecutionContext implements ExecutionContext {
    SimpleGeoLocationDetails gld = new SimpleGeoLocationDetails("9.8.7.6");

    @Override
    public GeoLocationDetails getLocation() {
        return gld;
    }

    @Override
    public IdentityChain getIdentity() {
        return new IdentityChain() {
            @Override public void addIdentity(Identity identity) {}
            @Override public List<Identity> getIdentities() { return new ArrayList<Identity>(); }
            @Override public <T extends Identity> List<T> getIdentities(Class<T> clazz) { return null; }
            @Override public boolean equals(Object that) {
                // Simplest possible implementation to satisfy tests that use this class
                return ((IdentityChain) that).getIdentities().size() == this.getIdentities().size();
            }
        };
    }

    @Override
    public RequestUUID getRequestUUID() {
        return new RequestUUIDImpl("trousers-1234567-7654321");
    }

    @Override
    public Date getReceivedTime() {
        return null;
    }

    @Override
    public Date getRequestTime() {
        return null;
    }

    @Override
    public boolean traceLoggingEnabled() {
        return false;
    }

    @Override
    public int getTransportSecurityStrengthFactor() {
        return 0;
    }

    @Override
    public boolean isTransportSecure() {
        return false;
    }

    public boolean equals(Object o) {
        ExecutionContext theOther = (ExecutionContext) o;
        return new EqualsBuilder()
                .append(getLocation(), theOther.getLocation())
                .append(getIdentity(), theOther.getIdentity())
                .append(getRequestUUID(), theOther.getRequestUUID())
                .append(getReceivedTime(), theOther.getReceivedTime())
                .append(traceLoggingEnabled(), theOther.traceLoggingEnabled())
                .isEquals();
    }
}

