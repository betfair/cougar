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

package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.ServiceVersion;
import junit.framework.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Unit test for JettyHandlerSpecification
 */
public class JettyHandlerSpecificationTest {
    IdentityTokenResolver resolver1 = new IdentityTokenResolver<HttpServletRequest, HttpServletResponse, String>() {
        public List<IdentityToken> resolve(HttpServletRequest input, String transportAuthTokens) { return null;}
        public void rewrite(List<IdentityToken> credentials, HttpServletResponse output) {}
        public boolean isRewriteSupported() { return false; }
    };

    IdentityTokenResolver resolver2 = new IdentityTokenResolver<HttpServletRequest, HttpServletResponse, String>() {
        public List<IdentityToken> resolve(HttpServletRequest input, String transportAuthTokens) { return null;}
        public void rewrite(List<IdentityToken> credentials, HttpServletResponse output) {}
        public boolean isRewriteSupported() { return false; }
    };

    private static final ServiceVersion version1 = new ServiceVersion("v1.2");
    private static final ServiceVersion version2 = new ServiceVersion("v1.3");
    private static final ServiceVersion version3 = new ServiceVersion("v2.5");

    @Test
    public void testContextRootConstruction() {
        JettyHandlerSpecification spec = new JettyHandlerSpecification("/protocolHeader", Protocol.SOAP, "/serviceContextRoot");
        Assert.assertEquals("/protocolHeader/serviceContextRoot", spec.getJettyContextRoot());

        spec = new JettyHandlerSpecification("/protocolHeader", Protocol.SOAP, "");
        Assert.assertEquals("/protocolHeader", spec.getJettyContextRoot());
    }

    @Test
    public void testAddIdentityTokenResolverNonJsonRPC() {
        //For Soap / rescript, we should be able to add more than one token resolver for each version

        JettyHandlerSpecification spec = new JettyHandlerSpecification("", Protocol.RESCRIPT, "");
        spec.addServiceVersionToTokenResolverEntry(version1, resolver1);
        spec.addServiceVersionToTokenResolverEntry(version2, resolver2);
        spec.addServiceVersionToTokenResolverEntry(version3, resolver2);

        //Should end up with a map here of 3 versions to 3 resolvers
        Map<ServiceVersion, IdentityTokenResolver> resolverMap = spec.getVersionToIdentityTokenResolverMap();
        Assert.assertTrue(resolverMap.size() == 3);
        Assert.assertEquals(resolver2, resolverMap.get(version3));
    }

    @Test
    public void testAddIdentityTokenResolverJsonRPC() {
        //JSON we can only add one token resolver for a given specification

        JettyHandlerSpecification spec = new JettyHandlerSpecification("", Protocol.JSON_RPC, "");
        spec.addServiceVersionToTokenResolverEntry(version1, resolver2);
        spec.addServiceVersionToTokenResolverEntry(version2, resolver2);

        //Should end up with a map with 1 entry
        Map<ServiceVersion, IdentityTokenResolver> resolverMap = spec.getVersionToIdentityTokenResolverMap();
        Assert.assertTrue(resolverMap.size() == 1);
        Assert.assertEquals(resolver2, resolverMap.values().iterator().next());
    }

}
