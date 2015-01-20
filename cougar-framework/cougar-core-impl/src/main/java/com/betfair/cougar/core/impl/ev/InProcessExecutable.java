/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.impl.tracing.TracingEndObserver;

import java.util.Date;

/**
 * Simple pass-through executable which ensures which client calls which go via the internal in-process transport
 * (ie not via a network based transport) get passed a correct ExecutionContext and the correct tracer spi calls are made.
 */
public class InProcessExecutable implements Executable {

    private Tracer tracer;

    public InProcessExecutable(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
        OperationKey noNamespaceKey = new OperationKey(key, null);
        RequestUUID subUuid = ctx.getRequestUUID().getNewSubUUID();
        tracer.startCall(ctx.getRequestUUID(), subUuid, noNamespaceKey);

        // this call uses the same thread so that the callers expectations regarding threading model are met
        executionVenue.execute(subContext(ctx,subUuid),noNamespaceKey,args,new TracingEndObserver(tracer,observer,ctx.getRequestUUID(),subUuid,noNamespaceKey),timeConstraints);
    }

    private ExecutionContext subContext(final ExecutionContext orig, final RequestUUID subUuid) {
        final Date requestTime = new Date();
        return new ExecutionContext() {
            @Override
            public GeoLocationDetails getLocation() {
                return orig.getLocation();
            }

            @Override
            public IdentityChain getIdentity() {
                return orig.getIdentity();
            }

            @Override
            public RequestUUID getRequestUUID() {
                return subUuid;
            }

            @Override
            public Date getReceivedTime() {
                // received time is request time, we assume near-zero latency for a couple of method calls
                return requestTime;
            }

            @Override
            public Date getRequestTime() {
                return requestTime;
            }

            @Override
            public boolean traceLoggingEnabled() {
                return orig.traceLoggingEnabled();
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                // we assume that in-process calls have maximum possible security since they don't go over
                // a network call
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isTransportSecure() {
                return true;
            }
        };
    }
}
