package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.servlet.http.HttpServletRequest;

/**
 * Default HTTP trace logging resolver. Checks for existence of the 'X-Trace-Me' HTTP header.
 */
public class HttpTraceLoggingResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {

    public static final String TRACE_ME_HEADER_PARAM = "X-Trace-Me";

    public HttpTraceLoggingResolver() {
        super(DehydratedExecutionContextComponent.TraceLoggingEnabled);
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        builder.setTraceLoggingEnabled(httpCommand.getRequest().getHeader(TRACE_ME_HEADER_PARAM)!=null);
    }
}
