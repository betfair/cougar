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

import java.util.*;

import com.betfair.cougar.core.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.ExportableRegistration;
import com.betfair.cougar.util.jmx.JMXControl;
import com.betfair.tornjak.monitor.Status;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Implementation of ExecutionVenue which is aware of the Spring container, and of the JMXControl.
 * It will ensure that once the Spring application is loaded all registered Exportable
 * beans will be registered with the JMXControl.
 * This implementation is also a CougarStartingGate, and notifies any registered listeners of when
 * Spring is loaded and the application is ready to start.
 *
 */
@ManagedResource
public class ContainerAwareExecutionVenue extends ServiceRegisterableExecutionVenue implements CougarStartingGate, ExportableRegistration {

	private final static Logger LOGGER = LoggerFactory.getLogger(ContainerAwareExecutionVenue.class);

    private List<GateListener> startingListeners = new ArrayList<GateListener>();
    private List<Exportable> exportables = new ArrayList<Exportable>();

    public ContainerAwareExecutionVenue() {
        super();
    }

    @Override
	public void registerExportable(Exportable exportable) {
		exportables.add(exportable);
	}


	/**
	 * In the case of ContextRefreshedEvent (that is, once all Spring configuration is loaded), the following actions
	 * will be taken:
	 * <list>
	 * <li>the superclass's onAppEvent will initialise all services</li>
	 * <li>The JMXControl will be retrieved from the application context and all registered Exportable implementations will be exported</li>
	 * <li>All registered StartingGateListeners will be notified that the starting gate is open</li>
	 * <li>A status check will be made on every registered service, with the status logged</li>
	 * </list>
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
            super.onApplicationEvent(event);
            try {
                final ApplicationContext ctx = ((ContextRefreshedEvent) event).getApplicationContext();
				// Check that the JMXControl exists before registering all exportables
				// see JMXControl javadocs for why we have to do it this way
				JMXControl jmxControl = JMXControl.getFromContext(ctx);
				if (jmxControl == null) {
					throw new PanicInTheCougar("jmxControl bean not found in ApplicationContext");
				}
				if (exportables != null) {
					for (Exportable exportable : exportables) {
						exportable.export(jmxControl);
					}
				}

				// Open the starting gate
				notifyStartingGateListeners();

				Collection<ServiceAware> serviceAwareImpls = getServiceAwareImplementations(ctx);
				if (serviceAwareImpls != null && serviceAwareImpls.size() > 0) {
					Set<Service> services = new HashSet<Service>();
					Collection<Map<ServiceDefinition, Service>> serviceDefinitions = getServiceImplementationMap().values();
					for (Map<ServiceDefinition, Service>  serviceDefinition : serviceDefinitions) {
						services.addAll(serviceDefinition.values());
					}
					for (ServiceAware serviceAware : serviceAwareImpls) {
						serviceAware.setServices(services);
					}
				}

                Status status = monitorRegistry
                        .getStatusAggregator()
                        .getStatus();
                if (status != Status.OK) {
                    LOGGER.warn("Cougar returned status {} at startup", status);
                }
                else {
                    LOGGER.info("Cougar returned status {} at startup", status);
                }
			} catch (Exception e) {
				throw new PanicInTheCougar("Failed to initialise server", e);
			}
            logSuccessfulCougarStartup();
		}
	}

    private Collection<ServiceAware> getServiceAwareImplementations(ApplicationContext ctx) {
		Map<String, ServiceAware> serviceAwareBeans = ctx.getBeansOfType(ServiceAware.class);
		return serviceAwareBeans.values();
	}


	private void logSuccessfulCougarStartup() {
        // Logging facade removes the ability for custom log levels, so just INFO
        LOGGER.info("**** COUGAR HAS STARTED *****");
    }

	@Override
	public boolean registerStartingListener(GateListener listener) {
		LOGGER.info("Registering gate listener {} with priority {}", listener.getName(), listener.getPriority());
		return startingListeners.add(listener);
	}


	private void notifyStartingGateListeners() {

		int numListeners = startingListeners.size();
		int cnt = 0;
		Collections.sort(startingListeners, LISTENER_COMPARATOR);
		for (GateListener listener: startingListeners) {
			LOGGER.info("({} of {}) Calling gate listener {}", ++cnt, numListeners, listener.getName());
			listener.onCougarStart();
		}
	}

	private static Comparator<GateListener> LISTENER_COMPARATOR =
		new Comparator<GateListener>() {
		@Override
		public int compare(GateListener g1, GateListener g2) {
			if (g1.getPriority() == g2.getPriority()) {
				return g1.getName().compareTo(g2.getName());
			} else if (g1.getPriority() < g2.getPriority()) {
				return 1;
			} else {
				return -1;
			}
		}
	};
}
