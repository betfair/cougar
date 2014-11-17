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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.core.impl.tracing.zipkin.ZipkinRequestUUIDImpl;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.servlet.http.HttpServletRequest;

/**
 * Default HTTP UUID resolver. Uses the uuid and uuidParents headers to resolve uuids.
 */
public class ZipkinHttpRequestUuidResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    public static final String TRACE_ID_HEADER = "X-Trace";
    public static final String SPAN_ID_HEADER = "X-Span";
    public static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span";
    private final String cougarUUIDHeader;

    public ZipkinHttpRequestUuidResolver(String cougarUUIDHeader) {
        super(DehydratedExecutionContextComponent.RequestUuid);
        this.cougarUUIDHeader = cougarUUIDHeader;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        HttpServletRequest request = httpCommand.getRequest();

        String cougarId = request.getHeader(cougarUUIDHeader);
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String spanId = request.getHeader(SPAN_ID_HEADER);
        String parentSpanId = request.getHeader(PARENT_SPAN_ID_HEADER);

        // TODO Span Name
        RequestUUID requestUUID = new ZipkinRequestUUIDImpl(cougarId, traceId, spanId, parentSpanId, "SPAN-NAME");
        builder.setRequestUUID(requestUUID);
    }
}
