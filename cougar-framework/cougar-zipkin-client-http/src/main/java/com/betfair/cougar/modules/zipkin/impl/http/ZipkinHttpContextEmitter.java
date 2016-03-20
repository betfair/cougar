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

package com.betfair.cougar.modules.zipkin.impl.http;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Zipkin context emitter for use with http client transports.
 *
 * @see ContextEmitter
 */
public class ZipkinHttpContextEmitter<HR> implements ContextEmitter<HR, List<Header>> {

    public ZipkinHttpContextEmitter(CompoundContextEmitter<HR, List<Header>> compoundContextEmitter) {
        compoundContextEmitter.addEmitter(this);
    }

    @Override
    public void emit(ClientCallContext ctx, HR request, List<Header> result) {

        RequestUUID requestUUID = ctx.getRequestUUID();

        if (requestUUID instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) requestUUID;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

                appendZipkinHeaders(result, zipkinData);
            } else {
                // disabling sampling for the entire request chain
                appendHeader(result, ZipkinKeys.SAMPLED, ZipkinKeys.DO_NOT_SAMPLE_VALUE);
            }

        } // else ignore
    }

    private static void appendZipkinHeaders(@Nonnull List<Header> result, @Nonnull ZipkinData zipkinData) {
        // enabling sampling for the entire request chain
        appendHeader(result, ZipkinKeys.SAMPLED, ZipkinKeys.DO_SAMPLE_VALUE);
        appendHeader(result, ZipkinKeys.TRACE_ID, Long.toHexString(zipkinData.getTraceId()));
        appendHeader(result, ZipkinKeys.SPAN_ID, Long.toHexString(zipkinData.getSpanId()));
        if (zipkinData.getParentSpanId() != null) {
            appendHeader(result, ZipkinKeys.PARENT_SPAN_ID, Long.toHexString(zipkinData.getParentSpanId()));
        }
        if (zipkinData.getFlags() != null) {
            appendHeader(result, ZipkinKeys.FLAGS, zipkinData.getFlags().toString());
        }
    }

    private static void appendHeader(@Nonnull List<Header> result, String key, String value) {
        result.add(new BasicHeader(key, value));
    }
}
