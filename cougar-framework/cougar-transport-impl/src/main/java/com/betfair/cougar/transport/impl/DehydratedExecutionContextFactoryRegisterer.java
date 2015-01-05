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

package com.betfair.cougar.transport.impl;

import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;

/**
 * Utility to aid registering of DehydratedExecutionContextFactorys.
 */
public class DehydratedExecutionContextFactoryRegisterer {
    private DehydratedExecutionContextResolution resolution;
    private DehydratedExecutionContextResolverFactory factory;
    private boolean enabled;

    public DehydratedExecutionContextFactoryRegisterer(DehydratedExecutionContextResolution resolution, DehydratedExecutionContextResolverFactory factory) {
        this(resolution, factory, true);
    }

    public DehydratedExecutionContextFactoryRegisterer(DehydratedExecutionContextResolution resolution, DehydratedExecutionContextResolverFactory factory, boolean enabled) {
        this.resolution = resolution;
        this.factory = factory;
        this.enabled = enabled;
    }

    public void init() {
        if (enabled) {
            resolution.registerFactory(factory);
        }
    }
}
