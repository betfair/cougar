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

package com.betfair.cougar.core.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;

/**
 * Defines a Service without reference to a particular implementation
 *
 */
public abstract class ServiceDefinition {

	private final Map<OperationKey, OperationDefinition> operationDefinitionMap;

	public ServiceDefinition() {
		operationDefinitionMap = new HashMap<OperationKey, OperationDefinition>();
	}

	protected void init() {
		for (OperationDefinition def : getOperationDefinitions()) {
			operationDefinitionMap.put(def.getOperationKey(), def);
		}
	}

	public abstract String getServiceName();

	public abstract ServiceVersion getServiceVersion();

	public abstract OperationDefinition [] getOperationDefinitions();

    public OperationDefinition [] getOperationDefinitions(OperationKey.Type filteredByOperationKeyType) {
        List<OperationDefinition> filteredOpList = new ArrayList<OperationDefinition>();

        for (OperationDefinition opDef : getOperationDefinitions()) {
            if (opDef.getOperationKey().getType() == filteredByOperationKeyType) {
                filteredOpList.add(opDef);
            }
        }
        return filteredOpList.toArray(new OperationDefinition[0]);
    }

	public final OperationDefinition getOperationDefinition(OperationKey key) {
		return operationDefinitionMap.get(key);
	}


}