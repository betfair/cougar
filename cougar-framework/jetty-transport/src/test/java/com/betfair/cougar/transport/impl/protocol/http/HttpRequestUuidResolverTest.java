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

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpRequestUuidResolverTest {
    private HttpCommand httpCommand;
    private HttpServletRequest request;
    private DehydratedExecutionContextBuilder builder;
    private HttpRequestUuidResolver uuidResolver;

    @Before
    public void init() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
        uuidResolver = new HttpRequestUuidResolver("X-UUID","X-UUID-Parents");
        httpCommand = mock(HttpCommand.class);
        request = mock(HttpServletRequest.class);
        when(httpCommand.getRequest()).thenReturn(request);
        builder = new DehydratedExecutionContextBuilder();
    }

    @Test
    public void noHeadersFound() {
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
    }

    @Test
    public void blankMainHeader() {
        when(request.getHeader("X-UUID")).thenReturn("");
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
    }

    @Test
    public void validMainHeader() {
        when(request.getHeader("X-UUID")).thenReturn("123456789-77777777-foo");
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
        assertEquals("123456789-77777777-foo",builder.getRequestUUID().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMainHeader() {
        when(request.getHeader("X-UUID")).thenReturn("abc");
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
    }

    @Test
    public void validMainHeaderBlankParentHeader() {
        when(request.getHeader("X-UUID")).thenReturn("123456789-77777777-foo");
        when(request.getHeader("X-UUID-Parents")).thenReturn("");
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
        assertEquals("123456789-77777777-foo", builder.getRequestUUID().toString());
    }

    @Test
    public void validMainHeaderValidParentHeader() {
        when(request.getHeader("X-UUID")).thenReturn("123456789-77777777-foo");
        when(request.getHeader("X-UUID-Parents")).thenReturn("123456789-77777777-root:123456789-77777777-parent");
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
        assertEquals("123456789-77777777-root:123456789-77777777-parent:123456789-77777777-foo", builder.getRequestUUID().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validMainHeaderInvalidParentHeader() {
        when(request.getHeader("X-UUID")).thenReturn("123456789-77777777-foo");
        when(request.getHeader("X-UUID-Parents")).thenReturn("abc");
        uuidResolver.resolve(httpCommand, null, builder);
        assertNotNull(builder.getRequestUUID());
    }
}
