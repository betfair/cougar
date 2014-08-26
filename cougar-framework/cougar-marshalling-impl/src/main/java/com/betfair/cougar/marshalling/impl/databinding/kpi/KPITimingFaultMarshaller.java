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

package com.betfair.cougar.marshalling.impl.databinding.kpi;

import java.io.OutputStream;

import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.tornjak.kpi.KPIMonitor;

/**
 * A wrapper which stores KPI statistics for a {@link FaultMarshaller}.
 */
public class KPITimingFaultMarshaller implements FaultMarshaller {

    private final KPIMonitor monitor;
    private final String  kpiName;
    private final FaultMarshaller marshaller;

    public KPITimingFaultMarshaller(KPIMonitor monitor, String kpiName, FaultMarshaller marshaller) {
        this.monitor = monitor;
        this.kpiName = kpiName;
        this.marshaller = marshaller;
    }

    @Override
    public void marshallFault(OutputStream outputStream, CougarFault fault, String encoding) {
        boolean success = false;
        long start = System.currentTimeMillis();
        try {
            marshaller.marshallFault(outputStream, fault, encoding);
            success = true;
        }
        finally {
            monitor.addEvent(kpiName, System.currentTimeMillis() - start, success);
        }
    }
}
