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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;

/**
 * Default implementation of IdentityChain
 *
 */
public class IdentityChainImpl implements IdentityChain {

	private final List<Identity> identities;

    public IdentityChainImpl() {
        this(new LinkedList<Identity>());
    }

    public IdentityChainImpl(List<Identity> identities) {
		this.identities = identities;
	}

    public void addIdentity(Identity identity) {
        identities.add(identity);
    }

	@Override
	public List<Identity> getIdentities() {
		List<Identity> result = new ArrayList<Identity>();
		result.addAll(identities);
		return result;
	}

	@Override
	public <T extends Identity> List<T> getIdentities(Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		for (Identity subject : identities) {
			if (clazz.isAssignableFrom(subject.getClass())) {
				result.add((T)subject);
			}
		}
		return result;
	}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<identities.size(); i++) {
            sb.append("Subject " + i + " " + identities.get(i));
            if (i<identities.size()-1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

}
