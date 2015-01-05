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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.RequestUUIDImpl;
import org.apache.commons.lang.StringUtils;

/**
 * Default HTTP UUID resolver. Uses the uuid and uuidParents headers to resolve uuids.
 */
public class HttpRequestUuidResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    private final String uuidHeader;
    private final String uuidParentsHeader;

    public HttpRequestUuidResolver(String uuidHeader, String uuidParentsHeader) {
        super(DehydratedExecutionContextComponent.RequestUuid);
        this.uuidHeader = uuidHeader;
        this.uuidParentsHeader = uuidParentsHeader;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        RequestUUID requestUUID = resolve(httpCommand);
        builder.setRequestUUID(requestUUID);
    }

    protected RequestUUID resolve(HttpCommand httpCommand) {
        String uuidString = httpCommand.getRequest().getHeader(uuidHeader);
        String uuidParentsString = httpCommand.getRequest().getHeader(uuidParentsHeader);
        final RequestUUID requestUUID;
        if (StringUtils.isNotBlank(uuidString)) {
            if (StringUtils.isNotBlank(uuidParentsString)) {
                requestUUID = new RequestUUIDImpl(uuidParentsString + UUIDGenerator.COMPONENT_SEPARATOR + uuidString);
            }
            else {
                requestUUID = new RequestUUIDImpl(uuidString);
            }
        } else {
            requestUUID = new RequestUUIDImpl();
        }
        return requestUUID;
    }
}
