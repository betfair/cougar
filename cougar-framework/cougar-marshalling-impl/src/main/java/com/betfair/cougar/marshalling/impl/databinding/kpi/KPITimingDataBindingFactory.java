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

import com.betfair.cougar.marshalling.api.databinding.*;
import com.betfair.tornjak.kpi.KPIMonitor;

/**
 * Factory which returns wrapped (un)marshallers which store KPI statistics.
 * <p>
 * NOTE: this factory always returns the same Marshaller and FaultMarshaller instances, and will
 * only call the underlying binding factory ONCE for a marshaller and faultMarshaller.
 */
public class KPITimingDataBindingFactory implements DataBindingFactory {

    private final String kpiMarshallerName;
    private final String kpiFaultMarshallerName;
    private final String kpiUnMarshallerName;
    private final String kpiFaultUnMarshallerName;


    private final KPIMonitor monitor;

    private final DataBindingFactory factory;

    private final Marshaller marshaller;
    private final FaultMarshaller faultMarshaller;
    private final UnMarshaller unMarshaller;
    private final FaultUnMarshaller faultUnMarshaller;

    /**
     * Constructor
     *
     * @param monitor
     * @param factory (note that we only ever get ONE marshaller, and ONE faultMarshaller from this
     *      factory
     * @param formatType format being (un)marshalled, eg. xml/json, forms part of KPI name
     */
    public KPITimingDataBindingFactory(KPIMonitor monitor, DataBindingFactory factory,
                    String formatType) {

        this.monitor = monitor;

        this.factory = factory;

        kpiMarshallerName         = "Cougar.ws." + formatType + ".marshall";
        kpiFaultMarshallerName    = "Cougar.ws." + formatType + ".marshallFault";
        kpiUnMarshallerName       = "Cougar.ws." + formatType + ".unmarshall";
        kpiFaultUnMarshallerName  = "Cougar.ws." + formatType + ".unmarshallFault";

        marshaller = new KPITimingMarshaller(monitor, kpiMarshallerName, factory.getMarshaller());
        faultMarshaller = new KPITimingFaultMarshaller(monitor, kpiFaultMarshallerName, factory.getFaultMarshaller());
        unMarshaller = new KPITimingUnMarshaller(monitor, kpiUnMarshallerName, factory.getUnMarshaller());
        faultUnMarshaller = new KPITimingFaultUnMarshaller(monitor, kpiFaultUnMarshallerName, factory.getFaultUnMarshaller());
    }

    @Override
    public Marshaller getMarshaller() {
        return marshaller;
    }

    @Override
    public FaultMarshaller getFaultMarshaller() {
        return faultMarshaller;
    }

    @Override
    public UnMarshaller getUnMarshaller() {
        return unMarshaller;
    }

    @Override
    public FaultUnMarshaller getFaultUnMarshaller() {
        return faultUnMarshaller;
    }
}