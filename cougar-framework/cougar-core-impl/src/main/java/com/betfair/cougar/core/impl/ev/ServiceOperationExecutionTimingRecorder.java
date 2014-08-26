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

import com.betfair.tornjak.kpi.KPIMonitor;

/**
 *
 */
public class ServiceOperationExecutionTimingRecorder extends SimpleExecutionTimingRecorder {

    private final String operationName;

    public ServiceOperationExecutionTimingRecorder(KPIMonitor stats, String statsName, String operationName) {
        super(stats, statsName);
        this.operationName = operationName;
    }

    @Override
    public void recordCall(double timeTakenMs) {
        super.recordCall(timeTakenMs);
        stats.addEvent(statsName, operationName, timeTakenMs, true);
    }

    @Override
    public void recordFailure(double timeTakenMs) {
        super.recordFailure(timeTakenMs);
        stats.addEvent(statsName, operationName, timeTakenMs, false);
    }
}
