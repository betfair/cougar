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

import com.betfair.cougar.api.DehydratedExecutionContext;

import java.util.List;

/**
 * The IdentityResolver resolves a set of credentials into an IdentityChain.
 *
 * @see IdentityChain
 *
 */
public interface IdentityResolver {

    /**
     * Given a set of credentials, resolves those credentials into
     * an IdentityChain. The identity chain to add the result(s) to is passed in.
     * @param chain the identity chain to add resolved identities to
     * @param ctx the execution context resolved so far including identity tokens resolved by the {@link IdentityTokenResolver} (IdentityChain on this context will be null).
     * @throws InvalidCredentialsException
     */
    public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException;

    /**
     * Given an identity chain, resolve back into a set of writable tokens
     * @param chain an identity chain
     * @return a list of tokens that may be written, which may be null.
     * @throws InvalidCredentialsException
     */
    public List<IdentityToken> tokenise(IdentityChain chain);
}
