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

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.security.*;

import java.util.LinkedList;
import java.util.List;

/**
 * An identity resolver implementation suitable for use with platform services.
 */
public class ClientIdentityResolver implements IdentityResolver {

    @Override
    public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException {
    }

    @Override
    public List<IdentityToken> tokenise(IdentityChain chain) {
        List<IdentityToken> identityTokens = new LinkedList<IdentityToken>();

        for(Identity identity : chain.getIdentities()) {
            Credential credential = identity.getCredential();
            if(credential != null) {
                IdentityToken token = new IdentityToken(credential.getName(), credential.getValue().toString());
                identityTokens.add(token);
            }
        }

        return identityTokens;
    }
}
