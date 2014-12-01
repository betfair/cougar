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

package com.betfair.cougar.modules.zipkin.impl.jetty;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.impl.protocol.http.HttpRequestUuidResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Zipkin HTTP UUID resolver. Extends default Cougar HTTP Resolver.
 */
public class ZipkinHttpRequestUuidResolver<Ignore> extends HttpRequestUuidResolver<Ignore> {
    private final ZipkinManager zipkinManager;

    public ZipkinHttpRequestUuidResolver(String uuidHeader, String uuidParentsHeader, ZipkinManager zipkinManager) {
        super(uuidHeader, uuidParentsHeader);
        this.zipkinManager = zipkinManager;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder contextBuilder) {
        RequestUUID cougarUuid = resolve(httpCommand);

        HttpServletRequest request = httpCommand.getRequest();

        String traceId = request.getHeader(ZipkinManager.TRACE_ID_KEY);
        String spanId = request.getHeader(ZipkinManager.SPAN_ID_KEY);
        String parentSpanId = request.getHeader(ZipkinManager.PARENT_SPAN_ID_KEY);

        RequestUUID requestUUID = zipkinManager.createNewZipkinRequestUUID(cougarUuid, traceId, spanId, parentSpanId);
        contextBuilder.setRequestUUID(requestUUID);
    }
}