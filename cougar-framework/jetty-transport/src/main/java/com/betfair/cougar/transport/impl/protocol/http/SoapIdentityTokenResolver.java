package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.apache.axiom.om.OMElement;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Default resolver for SOAP for identity tokens. Delegates to an IdentityTokenResolver which accepts
 * and OmElement and a certificate array as parameters.
 */
public class SoapIdentityTokenResolver extends X509IdentityTokenResolver<HttpCommand, OMElement> {

    public SoapIdentityTokenResolver() {
    }

    @Override
    public void resolve(HttpCommand httpCommand, OMElement omElement, DehydratedExecutionContextBuilder builder) {
        List<IdentityToken> tokens = new ArrayList<>();
        if (httpCommand.getIdentityTokenResolver() != null) {
            X509Certificate[] certificateChain = resolveCertificates(httpCommand.getRequest());
            tokens = httpCommand.getIdentityTokenResolver().resolve(omElement, certificateChain);
        }
        builder.setIdentityTokens(tokens);
    }
}
