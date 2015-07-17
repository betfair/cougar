/*
 * Copyright 2015, The Sporting Exchange Limited
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

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.util.RequestUUIDImpl;

/**
 * Default Socket UUID resolver.
 */
public class SocketRequestUuidResolver<Void> extends SingleComponentResolver<SocketContextResolutionParams, Void> {

    public SocketRequestUuidResolver() {
        super(DehydratedExecutionContextComponent.RequestUuid);
    }

    @Override
    public void resolve(SocketContextResolutionParams params, Void ignore, DehydratedExecutionContextBuilder builder) {
        RequestUUID requestUUID = resolve(params);
        builder.setRequestUUID(requestUUID);
    }

    protected RequestUUID resolve(SocketContextResolutionParams params) {
        RequestUUID requestUUID;
        if (params.getUuid() != null) {
            requestUUID = new RequestUUIDImpl(params.getUuid());
        } else {
            requestUUID = new RequestUUIDImpl();
        }
        return requestUUID;
    }
}