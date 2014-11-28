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
import com.betfair.cougar.api.zipkin.ZipkinDataBuilder;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.core.impl.tracing.zipkin.ZipkinConfig;
import com.betfair.cougar.core.impl.tracing.zipkin.ZipkinDataImpl;
import com.betfair.cougar.core.impl.tracing.zipkin.ZipkinRequestUUIDImpl;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Zipkin HTTP UUID resolver. Extends default Cougar HTTP Resolver.
 */
public class ZipkinHttpRequestUuidResolver<Ignore> extends HttpRequestUuidResolver<Ignore> {
    public static final String TRACE_ID_HEADER = "X-Trace";
    public static final String SPAN_ID_HEADER = "X-Span";
    public static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span";
    private final ZipkinConfig zipkinConfig;

    public ZipkinHttpRequestUuidResolver(String uuidHeader, String uuidParentsHeader, ZipkinConfig zipkinConfig) {
        super(uuidHeader, uuidParentsHeader);
        this.zipkinConfig = zipkinConfig;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder contextBuilder) {
        super.resolve(httpCommand, ignore, contextBuilder);
        RequestUUID cougarUuid = (RequestUUID) contextBuilder.getRequestUUID();

        HttpServletRequest request = httpCommand.getRequest();

        String traceId = request.getHeader(TRACE_ID_HEADER);
        String spanId = request.getHeader(SPAN_ID_HEADER);
        String parentSpanId = request.getHeader(PARENT_SPAN_ID_HEADER);

        ZipkinDataBuilder zipkinDataBuilder;

        if (traceId != null && spanId != null) {
            // a request with the fields is always traceable so we always propagate the tracing to the following calls

            zipkinDataBuilder = new ZipkinDataImpl.Builder()
                    .traceId(Long.valueOf(traceId))
                    .spanId(Long.valueOf(spanId))
                    .parentSpanId(parentSpanId == null ? null : Long.valueOf(parentSpanId));

        } else {

            if (zipkinConfig.shouldTrace()) {
                // starting point, we need to generate the ids if this request is to be sampled - we are the root

                UUID uuid = UUID.randomUUID();
                zipkinDataBuilder = new ZipkinDataImpl.Builder()
                        .traceId(uuid.getLeastSignificantBits())
                        .spanId(uuid.getMostSignificantBits())
                        .parentSpanId(null);

            } else {
                // otherwise leave them as null - this means Zipkin tracing will be disabled for this request
                zipkinDataBuilder = null;
            }

        }

        RequestUUID requestUUID = new ZipkinRequestUUIDImpl(cougarUuid, zipkinDataBuilder);
        contextBuilder.setRequestUUID(requestUUID);
    }
}