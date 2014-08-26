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

package com.betfair.cougar.transport.socket;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;

import java.util.Date;
import java.util.List;

/**
 */
public class SocketRequestContextImpl implements RequestContext, DehydratedExecutionContext {
    private DehydratedExecutionContext wrapped;
    private LogExtension connectedObjectLogExtension;

    public SocketRequestContextImpl(DehydratedExecutionContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void trace(String msg, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEventLogRecord(LoggableEvent record) {
        // don't care
    }

    @Override
    public void setRequestLogExtension(LogExtension extension) {
        // don't care
    }

    @Override
    public void setConnectedObjectLogExtension(LogExtension extension) {
        this.connectedObjectLogExtension = extension;
    }

    @Override
    public LogExtension getConnectedObjectLogExtension() {
        return connectedObjectLogExtension;
    }

    @Override
    public GeoLocationDetails getLocation() {
        return wrapped.getLocation();
    }

    @Override
    public IdentityChain getIdentity() {
        return wrapped.getIdentity();
    }

    @Override
    public RequestUUID getRequestUUID() {
        return wrapped.getRequestUUID();
    }

    @Override
    public Date getReceivedTime() {
        return wrapped.getReceivedTime();
    }

    @Override
    public Date getRequestTime() {
        return wrapped.getRequestTime();
    }

    @Override
    public boolean traceLoggingEnabled() {
        return wrapped.traceLoggingEnabled();
    }

    @Override
    public List<IdentityToken> getIdentityTokens() {
        return wrapped.getIdentityTokens();
    }

    @Override
    public void setIdentityChain(IdentityChain chain) {
        wrapped.setIdentityChain(chain);
    }

    @Override
    public int getTransportSecurityStrengthFactor() {
        return wrapped.getTransportSecurityStrengthFactor();
    }

    @Override
    public boolean isTransportSecure() {
        return wrapped.isTransportSecure();
    }
}
