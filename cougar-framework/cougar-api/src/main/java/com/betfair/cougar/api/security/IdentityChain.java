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

package com.betfair.cougar.api.security;

import java.util.List;

/**
 * A chain of Identities that together form the chain of trust that has authenticated the identity.
 * The primary subject would be, for example, a user. The user may have authenticated
 * with a third party application (the second subject), which in turn authenticated the
 * legacy public API (the third subject), which has authenticated with the Cougar service.
 * Together this forms a chain of trust which is the identity. Every subject within the
 * chain is important. For example, the third party application may not be permitted
 * to access all the functionality of the web site, meaning there is a more limited range
 * of functionality for this user than they would have if they had used the same Cougar service
 * via the betfair.com web site.
 *
 */
public interface IdentityChain {

	/**
	 * Gets a List of all CougarSubjects, with the primary subject first,
	 * and all subsequent subjects in the chain of trust in order after
	 * @return an ordered list of CougarSubjects
	 */
	List<Identity> getIdentities();

	/**
	 * A list of the subjects of the specified type. The ordering will be maintained
	 * as per getIdentities(), but because only subjects of the specified type are
	 * returned the first subject in the list may not be the primary subject.
	 * This method allows the application or permissions resolver to process only
	 * those subjects that implement a known interface.
	 * @param <T> the type of Identity to return
	 * @param clazz
	 * @return all CougarSubjects that are instances of the specified class
	 */
	<T extends Identity> List<T> getIdentities(Class<T> clazz);

    void addIdentity(Identity identity);
}
