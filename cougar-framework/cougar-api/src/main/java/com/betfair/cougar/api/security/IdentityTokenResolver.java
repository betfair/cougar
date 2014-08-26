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
 * Base credential resolver interface. This interface is intended to be extended
 * per transport to provide the resolving, writing and rewriting capabilities
 * required per transport.
 *
 */
public interface IdentityTokenResolver<I, O, C> {

    /**
     * Resolve a set of identity tokens, which in turn will be used to
     * resolve identity, from the request. Examples of a credential are a username,
     * a password, or an SSO token.
     * @param input  the data from which the tokens will be read.
     * @param transportAuthTokens - the set of transport level tokens (certificates in an Http transport)
     * @return a set of credentials resolved from the request
     */
    public List<IdentityToken> resolve(I input, C transportAuthTokens);

    /**
     * Allows the credentials to be re-written to the output. This may be
     * necessary, for example, in the case of an SSO token, where a new token has
     * been issued, and this new token must now be supplied to the client.
     * @param credentials the credentials to rewrite
     * @param output the output to which the token wil be written
     */
    public void rewrite(List<IdentityToken> credentials, O output);

    /**
     * is rewriting the tokens to the output supported
     */
    public boolean isRewriteSupported();
}
