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

package com.betfair.cougar.core.impl.kpi;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.tornjak.kpi.KPIMonitor;
import com.betfair.tornjak.kpi.KPITimer;

/**
 * ExecutionObserver wrapper which triggers a KPI event when the result has been produced.
 */
public class PerformanceMonitoringExecutionObserver implements ExecutionObserver {
    private final ExecutionObserver delegate;
    private final KPIMonitor monitor;
    private final KPITimer timer;
    private final boolean catchFailures;
    private final String name;
    private final String operation;


    public PerformanceMonitoringExecutionObserver(ExecutionObserver delegate, KPIMonitor monitor, KPITimer timer,
                                                  boolean catchFailures, String name, String operation) {
        this.delegate = delegate;
        this.monitor = monitor;
        this.timer = timer;
        this.catchFailures = catchFailures;
        this.name = name;
        this.operation = operation;
    }

    @Override
    public void onResult(ExecutionResult executionResult) {
        double duration = timer.stop();
        if (catchFailures) {
            monitor.addEvent(name, operation, duration, !executionResult.isFault());
        } else {
            monitor.addEvent(name, operation, duration);
        }
        delegate.onResult(executionResult);
    }
}
