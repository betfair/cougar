/*
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

package com.betfair.cougar.core.impl.security;

import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract class to deal with loading application keys from the Common name of a 2-way SSL encryption.
 */
public abstract class SSLAwareTokenResolver<I,O,C> implements IdentityTokenResolver<I,O,C> {

    public static final String SSL_CERT_INFO = "X-SSL-Cert-Info";

    private final CertInfoExtractor certInfoExtractor;

    protected SSLAwareTokenResolver(CertInfoExtractor certInfoExtractor) {
        if (certInfoExtractor == null) {
            throw new IllegalArgumentException("CertInfoExtractor cannot be null");
        }
        this.certInfoExtractor = certInfoExtractor;
    }

    protected void attachCertInfo(List<IdentityToken> credentials, X509Certificate[] certificateChain) throws NamingException {
        // Check if a SSL Common name has appeared
        String sSLCertInfo = findCertInfo(certificateChain);
        if (sSLCertInfo != null) {
            credentials.add(new IdentityToken(SSL_CERT_INFO, sSLCertInfo));
        }
    }

    /**
     * Find an the info from the cert chain provided. <code>null</code> if none found
     */
    protected String findCertInfo(X509Certificate[] x509certificates) throws NamingException {
        if (x509certificates != null && x509certificates.length != 0) {
            // Only ever use the first certificate, as this si the client supplied one.
            // Further ones are trust stores and CAs that have signed the first cert.
            Principal subject = x509certificates[0].getSubjectDN();
            if (subject != null && subject.getName() != null) {
                List<Rdn> rdns;
                try {
                    rdns = new LdapName(subject.getName()).getRdns();
                }
                catch (InvalidNameException ine) {
                    return null;
                }
                return certInfoExtractor.extractCertInfo(rdns);
            }
        }
        return null;
    }
}
