package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Default HTTP received time resolver. Sets the received time as 'now'.
 */
public class HttpReceivedTimeResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    public HttpReceivedTimeResolver() {
        super(DehydratedExecutionContextComponent.ReceivedTime);
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        builder.setReceivedTime(new Date());
    }
}
