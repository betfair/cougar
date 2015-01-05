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
