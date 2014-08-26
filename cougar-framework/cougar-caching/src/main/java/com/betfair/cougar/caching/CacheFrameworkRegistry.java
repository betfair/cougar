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

package com.betfair.cougar.caching;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class CacheFrameworkRegistry {

    private Map<String, CacheFrameworkIntegration> frameworks = new ConcurrentHashMap<String, CacheFrameworkIntegration>();

    public void registerFramework(String name, CacheFrameworkIntegration integration) {
        frameworks.put(name, integration);
    }

    public void registerFramework(CacheFrameworkIntegration integration) {
        frameworks.put(integration.getName(), integration);
    }

    public void unregisterFramework(String name) {
        frameworks.remove(name);
    }

    public Collection<CacheFrameworkIntegration> getFrameworks() {
        return frameworks.values();
    }
}
