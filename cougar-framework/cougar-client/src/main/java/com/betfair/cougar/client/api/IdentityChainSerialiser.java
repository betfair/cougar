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

package com.betfair.cougar.client.api;

import com.betfair.cougar.api.security.IdentityChain;

/**
 *	a mechanism for applications using cougar client to serialise the identity chain.  Cougar client cannot know the concrete types of the Identities within
 *  the IdentityChain nor the appropriate wire format.  Applications must therefore supply their own serialiser.  Specialised interfaces are available for the
 *  various transport types
 */
public interface IdentityChainSerialiser<O> {

	public void writeIdentity(IdentityChain identityChain, O output);

}
