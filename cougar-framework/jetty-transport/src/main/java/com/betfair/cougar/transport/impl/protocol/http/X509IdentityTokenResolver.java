package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

/**
 * Abstract helper for resolving X509 certificates from an HTTP request.
 */
public abstract class X509IdentityTokenResolver<Body,Transport> extends SingleComponentResolver<Body, Transport> {
    public static final String CERTIFICATE_ATTRIBUTE_NAME = "javax.servlet.request.X509Certificate";

    protected X509IdentityTokenResolver() {
        super(DehydratedExecutionContextComponent.IdentityTokens);
    }

    protected X509Certificate[] resolveCertificates(HttpServletRequest httpServletRequest) {
        Object o = httpServletRequest.getAttribute(CERTIFICATE_ATTRIBUTE_NAME);
        X509Certificate[] certificateChain = null;
        if (o != null && o instanceof X509Certificate[]) {
            certificateChain = (X509Certificate[]) o;
        }
        return certificateChain;
    }
}
