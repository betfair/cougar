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

package com.betfair.cougar.transport.impl.protocol;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.transport.api.protocol.ProtocolBinding;
import com.betfair.cougar.transport.api.protocol.ProtocolBindingRegistry;

/**
 * Used to join a ProtocolBinding to the ProtocolBindingRegistry
 */
public class ProtocolBindingHelper {
    private ProtocolBindingRegistry registry;

    private Protocol protocol;

    private String contextRoot;

    private IdentityTokenResolver<?, ?, ?> identityTokenResolver;

    private boolean enabled = true;

    public void init() {
        if (enabled) {
            registry.addProtocolBinding(new ProtocolBinding(contextRoot, identityTokenResolver, protocol));
        }
    }

    public void setRegistry(ProtocolBindingRegistry registry) {
        this.registry = registry;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public void setIdentityTokenResolver(IdentityTokenResolver<?, ?, ?> identityTokenResolver) {
        this.identityTokenResolver = identityTokenResolver;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
