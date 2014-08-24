package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.jetty.SSLRequestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Default HTTP resolver for transport strength. Delegates to SSLRequestUtils.getTransportSecurityStrengthFactor.
 */
public class HttpTransportStrengthResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    private int unknownCipherKeyLength;

    public HttpTransportStrengthResolver(int unknownCipherKeyLength) {
        super(DehydratedExecutionContextComponent.TransportSecurityStrengthFactor);
        this.unknownCipherKeyLength = unknownCipherKeyLength;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        int keyLength = 0;
        if (httpCommand.getRequest().getScheme().equals("https")) {
            keyLength = SSLRequestUtils.getTransportSecurityStrengthFactor(httpCommand.getRequest(), unknownCipherKeyLength);
        }
        builder.setTransportSecurityStrengthFactor(keyLength);
    }
}
