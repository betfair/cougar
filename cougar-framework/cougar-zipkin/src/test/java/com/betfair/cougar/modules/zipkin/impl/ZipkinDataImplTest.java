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

package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinDataBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ZipkinDataImplTest {

    private ZipkinDataBuilder victim;

    @Before
    public void init() {
        victim = new ZipkinDataImpl.Builder();
    }

    @Test(expected = NullPointerException.class)
    public void build_WhenSpanNameIsNull_ShouldThrowNPE() {
        victim.build();
    }

    @Test
    public void build_WhenSpanNameExists_ShouldBuildZipkinData() {
        String spanName = "Span Name";
        victim.spanName(spanName);

        ZipkinData result = victim.build();

        assertNotNull(result);
        assertEquals(result.getSpanName(), spanName);
        assertNull(result.getParentSpanId());
        assertNull(result.getFlags());
        assertEquals(result.getTraceId(), 0L);
        assertEquals(result.getSpanId(), 0L);
        assertEquals(result.getPort(), 0);
    }

    @Test
    public void build_WhenAllFieldsExist_ShouldBuildFullyPopulatedZipkinData() {
        long traceId = 123456789;
        long spanId = 987654321;
        Long parentSpanId = 543216789L;
        String spanName = "Span Name";
        short port = 9101;
        Long flags = 327L;
        String expectedStringRepresentation = "ZipkinDataImpl{" +
                "traceId=" + traceId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", spanName='" + spanName + '\'' +
                ", port=" + port +
                ", flags=" + flags +
                '}';

        victim.traceId(traceId);
        victim.spanId(spanId);
        victim.parentSpanId(parentSpanId);
        victim.spanName(spanName);
        victim.port(port);
        victim.flags(flags);

        ZipkinData result = victim.build();

        assertNotNull(result);
        assertEquals(result.getTraceId(), traceId);
        assertEquals(result.getSpanId(), spanId);
        assertEquals(result.getParentSpanId(), parentSpanId);
        assertEquals(result.getSpanName(), spanName);
        assertEquals(result.getPort(), port);
        assertEquals(result.getFlags(), flags);
        assertEquals(expectedStringRepresentation, result.toString());
    }
}
