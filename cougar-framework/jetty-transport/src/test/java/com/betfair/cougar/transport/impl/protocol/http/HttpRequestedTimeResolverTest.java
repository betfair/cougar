package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpRequestedTimeResolverTest {

    private HttpRequestedTimeResolver requestedTimeResolver;
    private HttpCommand httpCommand;
    private HttpServletRequest request;
    private DehydratedExecutionContextBuilder builder;
    private RequestTimeResolver requestTimeResolver;

    @Before
    public void init() {
        requestTimeResolver = mock(RequestTimeResolver.class);
        requestedTimeResolver = new HttpRequestedTimeResolver(requestTimeResolver);
        httpCommand = mock(HttpCommand.class);
        request = mock(HttpServletRequest.class);
        when(httpCommand.getRequest()).thenReturn(request);
        builder = new DehydratedExecutionContextBuilder();
    }

    @Test
    public void requestTimeResolverCalled() {
        Date d = new Date();
        when(requestTimeResolver.resolveRequestTime(request)).thenReturn(d);

        requestedTimeResolver.resolve(httpCommand, null, builder);
        assertEquals(d, builder.getRequestTime());
    }
}
