package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Default resolver for HTTP based transports for identity tokens. Delegates to an IdentityTokenResolver which accepts
 * the http request and a certificate array as parameters.
 */
public class HttpIdentityTokenResolver<Ignore> extends X509IdentityTokenResolver<HttpCommand, Ignore> {

    public HttpIdentityTokenResolver() {
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        List<IdentityToken> tokens = new ArrayList<>();
        if (httpCommand.getIdentityTokenResolver() != null) {
            X509Certificate[] certificateChain = resolveCertificates(httpCommand.getRequest());
            tokens = httpCommand.getIdentityTokenResolver().resolve(httpCommand.getRequest(), certificateChain);
        }
        builder.setIdentityTokens(tokens);
    }
}
