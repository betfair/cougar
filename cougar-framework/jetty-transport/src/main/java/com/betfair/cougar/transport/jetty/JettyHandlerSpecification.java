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
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.ServiceVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class defines all that is going to be needed to create a JettyHandler.
 * It includes the protocolBindingUri prefix (like www, or api), the service's context root
 * and the protocol (RESCRIPT/SOAP/JSON_RPC)
 *
 * It also contains a map of service version to IdentityTokenResolver, though if you're
 * not using security, then this will remain empty.
 */
public class JettyHandlerSpecification {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyHandlerSpecification.class);

    private static final ServiceVersion JSON_VERSION_AGNOSTIC_KEY = new ServiceVersion("v-1.-1");

    private String protocolBindingUriPrefix;
    private String serviceContextRoot;
    private Protocol protocol;

    private Map<ServiceVersion, IdentityTokenResolver> versionToIdentityTokenResolverMap =
            new HashMap<ServiceVersion, IdentityTokenResolver>();

    public JettyHandlerSpecification(String protocolBindingUriPrefix, Protocol protocol, String serviceContextRoot) {
        this.protocolBindingUriPrefix = protocolBindingUriPrefix;
        this.protocol = protocol;
        this.serviceContextRoot = serviceContextRoot;
    }

    public void addServiceVersionToTokenResolverEntry(ServiceVersion serviceVersion, IdentityTokenResolver resolver) {
        //JSON RPC can only have one IdentityTokenResolver as it is not a versioned service
        //Different protocol versions can be accessed via the invoke-by-name model of the protocol
        if (protocol == Protocol.JSON_RPC) {
            IdentityTokenResolver mapResolverEntry = versionToIdentityTokenResolverMap.get(JSON_VERSION_AGNOSTIC_KEY);
            if (mapResolverEntry == null) {
                versionToIdentityTokenResolverMap.put(JSON_VERSION_AGNOSTIC_KEY, resolver);
            } else {
                if (!mapResolverEntry.equals(resolver)) {
                    LOGGER.warn("You can only have one IdentityTokenResolver wired for JSON-RPC - ignoring [{}]",
                            resolver.getClass().getName());
                }
            }
        } else {
            versionToIdentityTokenResolverMap.put(serviceVersion, resolver);
        }
    }

    public String getProtocolBindingUriPrefix() {
        return protocolBindingUriPrefix;
    }

    public String getServiceContextRoot() {
        return serviceContextRoot;
    }

    public String getJettyContextRoot() {
        return protocolBindingUriPrefix == null ? serviceContextRoot : protocolBindingUriPrefix + serviceContextRoot;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Map<ServiceVersion, IdentityTokenResolver> getVersionToIdentityTokenResolverMap() {
        return versionToIdentityTokenResolverMap;
    }
}
