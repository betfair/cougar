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

import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HttpIdentityTokenResolverTest {

    private HttpIdentityTokenResolver identityTokenResolver;
    private HttpCommand httpCommand;
    private HttpServletRequest request;
    private DehydratedExecutionContextBuilder builder;

    @Before
    public void init() {
        identityTokenResolver = new HttpIdentityTokenResolver();
        httpCommand = mock(HttpCommand.class);
        request = mock(HttpServletRequest.class);
        when(httpCommand.getRequest()).thenReturn(request);
        builder = new DehydratedExecutionContextBuilder();
    }

    @Test
    public void resolveCallsTokenResolver() {
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        when(httpCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        List<IdentityToken> tokens = Arrays.asList(new IdentityToken("SomeToken","SomeValue"));
        when(tokenResolver.resolve(request, null)).thenReturn(tokens);

        identityTokenResolver.resolve(httpCommand, null, builder);

        verify(tokenResolver, times(1)).resolve(request, null);
        assertEquals(tokens, builder.getIdentityTokens());
    }
    @Test
    public void certificatesObtained() {
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        when(httpCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(tokenResolver.resolve(request, null)).thenReturn(new ArrayList<>());
        X509Certificate cert = mock(X509Certificate.class);
        X509Certificate[] certs = new X509Certificate[] { cert };
        when(request.getAttribute(X509IdentityTokenResolver.CERTIFICATE_ATTRIBUTE_NAME)).thenReturn(certs);

        identityTokenResolver.resolve(httpCommand, null, builder);

        verify(tokenResolver, times(1)).resolve(request, certs);
    }
}
