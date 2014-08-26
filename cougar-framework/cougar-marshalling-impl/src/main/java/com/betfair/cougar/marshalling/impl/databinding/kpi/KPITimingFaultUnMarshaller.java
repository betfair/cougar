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

import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;
import com.betfair.tornjak.kpi.KPIMonitor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <Class comment here>
 */
public class KPITimingFaultUnMarshaller implements FaultUnMarshaller {
    private final KPIMonitor monitor;
    private final String  kpiName;
    private final FaultUnMarshaller faultUnMarshaller;



    public KPITimingFaultUnMarshaller(KPIMonitor monitor, String kpiName, FaultUnMarshaller faultUnMarshaller) {
        this.monitor = monitor;
        this.kpiName = kpiName;
        this.faultUnMarshaller = faultUnMarshaller;
    }

    @Override
    public CougarFault unMarshallFault(InputStream inputStream, String encoding) {
        boolean success = false;
        long start = System.currentTimeMillis();
        CougarFault fault = null;
        try {
            fault = faultUnMarshaller.unMarshallFault(inputStream, encoding);
            success = true;
        }
        finally {
            monitor.addEvent(kpiName, System.currentTimeMillis() - start, success);
        }
        return fault;
    }



}
