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

package com.betfair.cougar.modules.zipkin.impl.http;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.impl.ZipkinExecutionContextResolverFactory;
import com.betfair.cougar.modules.zipkin.impl.ZipkinManager;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.impl.protocol.http.HttpRequestUuidResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Zipkin HTTP UUID resolver. Extends default Cougar HTTP Resolver.
 *
 * @see HttpRequestUuidResolver
 */
public class ZipkinHttpRequestUuidResolver<Ignore> extends HttpRequestUuidResolver<Ignore> {
    private final ZipkinManager zipkinManager;

    public ZipkinHttpRequestUuidResolver(ZipkinExecutionContextResolverFactory resolverFactory,
                                         ZipkinManager zipkinManager,
                                         String uuidHeader, String uuidParentsHeader) {
        super(uuidHeader, uuidParentsHeader);
        this.zipkinManager = zipkinManager;
        resolverFactory.setHttpRequestResolver(this);
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        RequestUUID cougarUuid = resolve(httpCommand);

        HttpServletRequest request = httpCommand.getRequest();

        String traceId = request.getHeader(ZipkinKeys.TRACE_ID);
        String spanId = request.getHeader(ZipkinKeys.SPAN_ID);
        String parentSpanId = request.getHeader(ZipkinKeys.PARENT_SPAN_ID);
        String sampled = request.getHeader(ZipkinKeys.SAMPLED);
        String flags = request.getHeader(ZipkinKeys.FLAGS);
        int port = request.getLocalPort();

        RequestUUID requestUUID = zipkinManager.createNewZipkinRequestUUID(cougarUuid, traceId, spanId, parentSpanId,
                sampled, flags, port);
        builder.setRequestUUID(requestUUID);
    }
}
