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

package com.betfair.cougar.api;

import java.util.Date;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;

/**
 *
 */
public class ExecutionContextImpl implements ExecutionContext {

    private GeoLocationDetails geoLocationDetails;
    private IdentityChain identity;
    private RequestUUID requestUUID;
    private Date receivedTime;
    private Date requestTime;
    private boolean traceLoggingEnabled;
    private int transportSecurityStrengthFactor;

    public ExecutionContextImpl() {
    }

    public ExecutionContextImpl(ExecutionContext ctx) {
        this.geoLocationDetails = ctx.getLocation();
        this.identity = ctx.getIdentity();
        this.requestUUID = ctx.getRequestUUID();
        this.receivedTime = ctx.getReceivedTime();
        this.requestTime = ctx.getRequestTime();
        this.traceLoggingEnabled = ctx.traceLoggingEnabled();
        this.transportSecurityStrengthFactor = ctx.getTransportSecurityStrengthFactor();
    }

    @Override
    public GeoLocationDetails getLocation() {
        return geoLocationDetails;
    }

    @Override
    public IdentityChain getIdentity() {
        return identity;
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

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public void setGeoLocationDetails(GeoLocationDetails geoLocationDetails) {
        this.geoLocationDetails = geoLocationDetails;
    }

    public void setIdentity(IdentityChain identity) {
        this.identity = identity;
    }

    public void setRequestUUID(RequestUUID requestUUID) {
        this.requestUUID = requestUUID;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public void setTraceLoggingEnabled(boolean traceLoggingEnabled) {
        this.traceLoggingEnabled = traceLoggingEnabled;
    }

    public void setTransportSecurityStrengthFactor(int transportSecurityStrengthFactor) {
        this.transportSecurityStrengthFactor = transportSecurityStrengthFactor;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("geoLocationDetails=").append(geoLocationDetails).append("|");
        sb.append("identityChain=").append(identity).append("|");
        sb.append("requestUUID=").append(requestUUID).append("|");
        sb.append("receivedTime=").append(receivedTime).append("|");
        sb.append("requestTime=").append(requestTime).append("|");
        sb.append("traceLoggingEnabled=").append(traceLoggingEnabled).append("|");
        sb.append("transportSecurityStrengthFactor=").append(transportSecurityStrengthFactor);

        return sb.toString();
    }
}


