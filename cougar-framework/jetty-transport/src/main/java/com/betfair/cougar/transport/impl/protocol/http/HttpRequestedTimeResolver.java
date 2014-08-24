package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Default HTTP requested time resolver. Delegates to a RequestTimeResolver.
 */
public class HttpRequestedTimeResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    private RequestTimeResolver<HttpServletRequest> requestTimeResolver;

    public HttpRequestedTimeResolver(RequestTimeResolver<HttpServletRequest> requestTimeResolver) {
        super(DehydratedExecutionContextComponent.RequestedTime);
        this.requestTimeResolver = requestTimeResolver;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        Date requestTime = requestTimeResolver.resolveRequestTime(httpCommand.getRequest());
        builder.setRequestTime(requestTime);
    }
}
