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

package com.betfair.cougar.core.impl.ev;

import java.util.List;

import com.betfair.cougar.core.api.ev.CompoundExecutableResolver;
import com.betfair.cougar.core.api.ev.ExecutableResolver;

/**
 * Helper class for registering multiple ExecutionResolvers with Spring configuration.
 *
 */
public class CompoundExecutableResolverHelper {

    /**
     * Registers a single ExecutableResolver with a CompoundExecutionResolver in the default namespace
     * @param compoundResolver
     * @param simpleResolver
     */
    public CompoundExecutableResolverHelper(CompoundExecutableResolver compoundResolver, ExecutableResolver simpleResolver) {
        compoundResolver.registerExecutableResolver(null, simpleResolver);
    }

	/**
	 * Registers a single ExecutableResolver with a CompoundExecutionResolver
	 * @param compoundResolver
	 * @param simpleResolver
	 */
	public CompoundExecutableResolverHelper(CompoundExecutableResolver compoundResolver, String namespace, ExecutableResolver simpleResolver) {
		compoundResolver.registerExecutableResolver(namespace, simpleResolver);
	}
}
