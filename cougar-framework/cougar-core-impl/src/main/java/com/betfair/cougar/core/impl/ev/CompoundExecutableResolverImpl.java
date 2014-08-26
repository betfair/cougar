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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.betfair.cougar.core.api.ev.CompoundExecutableResolver;
import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ExecutableResolver;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationKey;

public class CompoundExecutableResolverImpl implements CompoundExecutableResolver {

	private Map<String,List<ExecutableResolver>> executableResolvers = new HashMap<String,List<ExecutableResolver>>();

    @Override
	public void registerExecutableResolver(ExecutableResolver executableResolver) {
		registerExecutableResolver(null, executableResolver);
	}

    @Override
    public void registerExecutableResolver(String namespace, ExecutableResolver executableResolver) {
        List<ExecutableResolver> resolver = executableResolvers.get(namespace);
        if (resolver == null) {
            resolver = new ArrayList<ExecutableResolver>();
            executableResolvers.put(namespace, resolver);
        }
        resolver.add(executableResolver);
    }

	@Override
	public Executable resolveExecutable(OperationKey operationKey, ExecutionVenue ev) {
		for (ExecutableResolver resolver : executableResolvers.get(operationKey.getNamespace())) {
			Executable executable = resolver.resolveExecutable(operationKey.getLocalKey(), ev);
			if (executable != null) {
				return executable;
			}
		}
		return null;
	}

}
