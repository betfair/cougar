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

package com.betfair.cougar.core.impl;

import com.betfair.cougar.core.api.CougarStartingGate;
import com.betfair.cougar.core.api.GateListener;
import com.betfair.cougar.core.api.ServiceRegistrar;
import com.betfair.cougar.core.api.ev.CompoundExecutableResolver;
import com.betfair.cougar.core.api.ev.EVServiceRegistration;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.transports.TransportRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;

/**
 * This bean locates all ServiceExporter instances and introduces each to Cougar and the collection of
 * transports
 */
public class CougarIntroductionService implements ApplicationContextAware, GateListener {
    private ApplicationContext applicationContext;

    private TransportRegistry transportRegistry;

    private ExecutionVenue executionVenue;
    private ServiceRegistrar serviceRegistrar;
    private CompoundExecutableResolver executableResolver;

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void onCougarStart() {
        performIntroductions();
    }

    public void performIntroductions() {
        //start by finding all the services exporter beans
        Collection<EVServiceRegistration> registeringServices = applicationContext.getBeansOfType(EVServiceRegistration.class).values();

        for (EVServiceRegistration service : registeringServices) {
            registerService(service);
        }
    }

    public void registerService(EVServiceRegistration service) {
        //Inform the EV
        service.introduceServiceToEV(executionVenue, serviceRegistrar, executableResolver);
        //Marry each service definition to the transports
        service.introduceServiceToTransports(transportRegistry.getTransports());
    }

    @Override
    public String getName() {
        return "CougarIntroductionService";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * By setting the starting gate property this IntroductionService will register
     * itself with the CougarStartingGate
     *
     * @param startingGate the starting gate for the application
     */
    public void setStartingGate(CougarStartingGate startingGate) {
        startingGate.registerStartingListener(this);
    }


    public void setExecutableResolver(CompoundExecutableResolver executableResolver) {
        this.executableResolver = executableResolver;
    }

    public void setExecutionVenue(ExecutionVenue executionVenue) {
        this.executionVenue = executionVenue;
    }

    public void setServiceRegistrar(ServiceRegistrar serviceRegistrar) {
        this.serviceRegistrar = serviceRegistrar;
    }

    public void setTransportRegistry(TransportRegistry transportRegistry) {
        this.transportRegistry = transportRegistry;
    }
}
