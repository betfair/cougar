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

package com.betfair.cougar.util.monitors;

import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.OnDemandMonitor;
import com.betfair.tornjak.monitor.Status;

/**
 * A monitor which decouples components which are able to present a simple binary health state from
 * the monitoring framework. Easier testing, configuration and method-name-clash-avoidance ensues.
 */
public class SimpleHealthMonitor extends OnDemandMonitor {

    private final String monitorName;
    private final HealthAware component;
    private final Status failState;


    public SimpleHealthMonitor(String monitorName, HealthAware component, MonitorRegistry registry, Status failState) {
        if (failState == Status.OK) {
            throw new IllegalArgumentException("Fail state may not be OK");
        }
        this.monitorName = monitorName;
        this.component = component;
        this.failState = failState;
        registry.addMonitor(this); // this idiom is evil but Spring-friendly
    }

    @Override
    public String getName() {
        return monitorName;
    }

    @Override
    public Status checkStatus() {
        if (component.isHealthy()) {
            return Status.OK;
        }
        return failState;
    }
}
