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

package com.betfair.cougar.core.api.security;

import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;

/**
 *  This class provides a container to manage the singleton identity resolver
 *  This must be implemented by the application.  Note that there can be only
 *  one Identity resolver per application, and once its set is immutable
 *  @see com.betfair.cougar.api.security.IdentityResolver
 */
public class IdentityResolverFactory {
    private IdentityResolver identityResolver;

    public IdentityResolver getIdentityResolver() {
        return identityResolver;
    }

    public void setIdentityResolver(IdentityResolver identityResolver) {
        if (this.identityResolver != null && identityResolver != null) {
            throw new CougarFrameworkException("IdentityResolver is immutable!");
        }
        this.identityResolver = identityResolver;
    }
}
