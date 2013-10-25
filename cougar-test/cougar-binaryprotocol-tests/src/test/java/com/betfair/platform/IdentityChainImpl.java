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

package com.betfair.platform;

import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A default implementation of an identity chain.
 *
 * Identities are always added to the end of the list, so if multiple identities of
 * a type are specified, by convention the furthest one away from the current application
 * will be at a lower index.
 *
 * Logically, therefore, the external identity (I.E. the one a suer hit the system with)
 * is at index 0.
 */
public class IdentityChainImpl implements IdentityChain {
	
	private List<Identity> identities = new CopyOnWriteArrayList<Identity>();

	@Override
	public List<Identity> getIdentities() {
		return identities;
	}

	@Override
	public <T extends Identity> List<T> getIdentities(Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		for (Identity identity : identities) {
			if (clazz.isAssignableFrom(identity.getClass())) {
				result.add((T) identity);
			}
			
		}
		return result;
	}

	public void addIdentity(Identity identity) {
		identities.add(identity);
	}
}
