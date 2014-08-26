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

import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.tornjak.kpi.KPIMonitor;

/**
 * A wrapper which stores KPI statistics for a {@link FaultMarshaller}.
 */
public class KPITimingMarshaller implements Marshaller {

    private final KPIMonitor monitor;
    private final String  kpiName;
    private final Marshaller marshaller;

    public KPITimingMarshaller(KPIMonitor monitor, String kpiName, Marshaller marshaller) {
        this.monitor = monitor;
        this.kpiName = kpiName;
        this.marshaller = marshaller;
    }

	@Override
	public String getFormat() {
		return marshaller.getFormat();
	}

    @Override
    public void marshall(OutputStream outputStream, Object result, String encoding, boolean client) {
        boolean success = false;
        long start = System.currentTimeMillis();
        try {
            marshaller.marshall(outputStream, result, encoding, client);
            success = true;
        }
        finally {
            monitor.addEvent(kpiName, System.currentTimeMillis() - start, success);
        }
    }
}
