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
