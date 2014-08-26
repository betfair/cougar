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

package com.betfair.cougar.transport.api.protocol;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityTokenResolver;

/**
 * A protocol binding is used to describe the relationship between a Cougar http transport @see Protocol
 * the context root it will accessed from and the identitytokenResolver to use on auth tokens
 * being received on that channel
 */
public class ProtocolBinding {
    private Protocol protocol;

    private String contextRoot;

    private IdentityTokenResolver<?, ?, ?> identityTokenResolver;

    public ProtocolBinding(String contextRoot, IdentityTokenResolver<?, ?, ?> identityTokenResolver, Protocol protocol) {
        if (contextRoot != null) {
            this.contextRoot = !contextRoot.startsWith("/") ? "/" + contextRoot : contextRoot;
            this.contextRoot = this.contextRoot.endsWith("/") ? this.contextRoot.substring(0, this.contextRoot.length()-1) : this.contextRoot;
        } else {
            this.contextRoot = "";
        }

        if (protocol == null) {
            throw new NullPointerException("Protocol must be set!");
        }

        this.identityTokenResolver = identityTokenResolver;
        this.protocol = protocol;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public IdentityTokenResolver<?, ?, ?> getIdentityTokenResolver() {
        return identityTokenResolver;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof ProtocolBinding) {
            ProtocolBinding another = (ProtocolBinding)o;
            return (this.contextRoot + protocol).equals(another.contextRoot + protocol);
        }
        return equal;
    }

    public int hashCode() {
        return contextRoot.hashCode()*13+protocol.hashCode();
    }
}
