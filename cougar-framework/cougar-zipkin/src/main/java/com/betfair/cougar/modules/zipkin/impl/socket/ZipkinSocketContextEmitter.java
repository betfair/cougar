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

package com.betfair.cougar.modules.zipkin.impl.socket;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.api.CompoundContextEmitter;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinKeys;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Zipkin context emitter for use with socket client transports (based on ZipkinHttpContextEmitter)
 */
public class ZipkinSocketContextEmitter<C> implements ContextEmitter<Map<String, String>, C> {

    public ZipkinSocketContextEmitter(CompoundContextEmitter<Map<String, String>, C> compoundContextEmitter) {
        compoundContextEmitter.addEmitter(this);
    }

    @Override
    public void emit(ClientCallContext ctx, @Nonnull Map<String, String> additionalData, @Nullable C ignore) {

        RequestUUID requestUUID = ctx.getRequestUUID();

        if (requestUUID instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) requestUUID;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

                appendZipkinHeaders(additionalData, zipkinData);
            } else {
                // disabling sampling for the entire request chain
                additionalData.put(ZipkinKeys.SAMPLED, ZipkinKeys.DO_NOT_SAMPLE_VALUE);
            }

        } // else ignore
    }

    private static void appendZipkinHeaders(@Nonnull Map<String, String> additionalData,
                                            @Nonnull ZipkinData zipkinData) {
        // enabling sampling for the entire request chain
        additionalData.put(ZipkinKeys.SAMPLED, ZipkinKeys.DO_SAMPLE_VALUE);
        additionalData.put(ZipkinKeys.TRACE_ID, Long.toHexString(zipkinData.getTraceId()));
        additionalData.put(ZipkinKeys.SPAN_ID, Long.toHexString(zipkinData.getSpanId()));

        if (zipkinData.getParentSpanId() != null) {
            additionalData.put(ZipkinKeys.PARENT_SPAN_ID, Long.toHexString(zipkinData.getParentSpanId()));
        }
        if (zipkinData.getFlags() != null) {
            additionalData.put(ZipkinKeys.FLAGS, zipkinData.getFlags().toString());
        }
    }
}
