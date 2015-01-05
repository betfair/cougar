/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.api;

import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;

import java.util.List;

/**
 *
 */
public interface DehydratedExecutionContext extends ExecutionContext {

    List<IdentityToken> getIdentityTokens();

    /**
     * Sets the identity chain once it has been resolved. This method may only be called once in the lifetime of an execution context.
     * Subsequent invocations will throw a {@link IllegalStateException}.
     * @param chain The resolved IdentityChain
     */
    void setIdentityChain(IdentityChain chain);
}
