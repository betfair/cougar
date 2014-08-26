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

import java.util.Set;

/**
 * Maintains a set of ProtocolBindings .
 * The means of resolving IdentityTokens may differ from channel to channel, so this interface
 * exists to document the bindings between each protocol, uri and identityTokenResolver
 *
 * @see com.betfair.cougar.api.security.IdentityTokenResolver
 *
 */
public interface ProtocolBindingRegistry {

	/**
	 * Add a IdentityTokenResolver to the registry.
	 */
	public void addProtocolBinding(ProtocolBinding binding);

    /**
     * @return Returns all the ProtocolBindings contained in this registry
     */
    public Set<ProtocolBinding> getProtocolBindings();
}
