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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.util.RequestUUIDImpl;

import java.util.Date;

/**
 *
 */
public class CallContextFactory {
    public static ClientCallContext createSubContext(final ExecutionContext ctx) {
        final RequestUUID subUuid = ctx.getRequestUUID() != null ? ctx.getRequestUUID().getNewSubUUID() : new RequestUUIDImpl();
        final Date requestTime = new Date();
        return new ClientCallContext() {
            @Override
            public GeoLocationDetails getLocation() {
                return ctx.getLocation();
            }

            @Override
            public IdentityChain getIdentity() {
                return ctx.getIdentity();
            }

            @Override
            public RequestUUID getRequestUUID() {
                return subUuid;
            }

            @Override
            public Date getRequestTime() {
                return requestTime;
            }

            @Override
            public boolean traceLoggingEnabled() {
                return ctx.traceLoggingEnabled();
            }
        };
    }
}
