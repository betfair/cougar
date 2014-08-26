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

import org.springframework.beans.factory.InitializingBean;

/**
 *
 */
public class CacheFrameworkRegisterer implements InitializingBean {
    private CacheFrameworkRegistry registry;
    private CacheFrameworkIntegration framework;

    public CacheFrameworkRegisterer(CacheFrameworkRegistry registry, CacheFrameworkIntegration framework) {
        this.registry = registry;
        this.framework = framework;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registry.registerFramework(framework);
    }
}
