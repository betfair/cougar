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
import org.junit.Test;

import javax.naming.NamingException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

public class SSLAwareTokenResolverTest {

    X509Certificate[] createCert(final boolean hasPrincipal, final String subject) {
        return new X509Certificate[]{new sun.security.x509.X509CertImpl() {
            @Override
            public Principal getSubjectDN() {
                if (hasPrincipal) {
                    return new Principal() {
                        @Override
                        public String getName() {
                            return subject;
                        }
                    };
                }
                return null;
            }
        }};
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullResolver() {
        new SSLAwareTokenResolver<Object,Object,Object>(null) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
    }

    @Test
    public void noCredentials() throws NamingException {
        SSLAwareTokenResolver tr = new SSLAwareTokenResolver<Object,Object,Object>(new CommonNameCertInfoExtractor()) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
        assertNull(tr.findCertInfo(null));
    }

    @Test
    public void emptyCredentials() throws NamingException {
        SSLAwareTokenResolver tr = new SSLAwareTokenResolver<Object,Object,Object>(new CommonNameCertInfoExtractor()) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
        assertNull(tr.findCertInfo(new X509Certificate[]{}));
    }

    @Test
    public void certWithNoSubject() throws NamingException {
        SSLAwareTokenResolver tr = new SSLAwareTokenResolver<Object,Object,Object>(new CommonNameCertInfoExtractor()) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
        assertNull(tr.findCertInfo(createCert(false, null)));
    }

    @Test
    public void certWithNoSubjectName() throws NamingException {
        SSLAwareTokenResolver tr = new SSLAwareTokenResolver<Object,Object,Object>(new CommonNameCertInfoExtractor()) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
        assertNull(tr.findCertInfo(createCert(true, null)));
    }

    @Test
    public void certWithBadSubjectName() throws NamingException {
        SSLAwareTokenResolver tr = new SSLAwareTokenResolver<Object,Object,Object>(new CommonNameCertInfoExtractor()) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
        assertNull(tr.findCertInfo(createCert(true, "BISCUITS")));
    }

    @Test
    public void certsWithSubjectMatch() throws NamingException {
        SSLAwareTokenResolver tr = new SSLAwareTokenResolver<Object,Object,Object>(new CommonNameCertInfoExtractor()) {
            public List<IdentityToken> resolve(Object input, Object transportAuthTokens) { return null; }
            public void rewrite(List<IdentityToken> credentials, Object output) {}
            public boolean isRewriteSupported() { return false; }
        };
        assertEquals("ClientName", tr.findCertInfo(createCert(true, "CN=ClientName, OU=Betfair, O=Betfair, ST=London, C=UK")));
        assertEquals("ClientName", tr.findCertInfo(createCert(true, "C=BE, ST=Brussel, L=Brussel, O=Vereniging van VlaamseBalies, OU=Vereniging van Vlaamse Balies,CN=ClientName")));
        assertEquals("ClientName", tr.findCertInfo(createCert(true, "C=BE, fCN=foo, CN=ClientName, O=Vereniging")));
        assertEquals("ClientName", tr.findCertInfo(createCert(true, "C=BE,CN=ClientName")));
        assertEquals("ClientName", tr.findCertInfo(createCert(true, "C=BE,cn=ClientName,fo=bat")));
        assertEquals("ClientName", tr.findCertInfo(createCert(true, "CN=ClientName")));
    }
}
