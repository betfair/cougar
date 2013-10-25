/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.baseline.security;

import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.impl.security.CommonNameCertInfoExtractor;
import com.betfair.cougar.core.impl.security.SSLAwareTokenResolver;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptIdentityTokenResolver;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class SimpleRescriptIdentityTokenResolver extends SSLAwareTokenResolver<HttpServletRequest, HttpServletResponse, X509Certificate[]> implements RescriptIdentityTokenResolver {

    public SimpleRescriptIdentityTokenResolver() {
        super(new CommonNameCertInfoExtractor());
    }

    @Override
    public List<IdentityToken> resolve(HttpServletRequest input, X509Certificate[] certificateChain) {
        List<IdentityToken> tokens = new ArrayList<IdentityToken>();
        try {
            attachCertInfo(tokens, certificateChain);
        }
        catch (NamingException ne) {
            throw new CougarFrameworkException("Unable to resolve cert info", ne);
        }
        for (SimpleIdentityTokenName t: SimpleIdentityTokenName.values()) {
            String val = input.getHeader("X-Token-"+t.name());
            if (val != null && val.length() > 0) {
                tokens.add(new IdentityToken(t.name(), val));
            }
        }
        return tokens;
    }

    @Override
    public void rewrite(List<IdentityToken> credentials, HttpServletResponse output) {
        for (IdentityToken ik: credentials) {
            if (ik == null) {
                System.err.println(credentials);
            }
            output.addHeader("X-Token-"+ik.getName(), ik.getValue());
        }
    }

    @Override
    public boolean isRewriteSupported() {
        return true;
    }
}
