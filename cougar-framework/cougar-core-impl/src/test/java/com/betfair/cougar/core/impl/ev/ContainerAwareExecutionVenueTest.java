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

import com.betfair.cougar.core.api.GateListener;
import com.betfair.cougar.core.api.security.IdentityResolverFactory;
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.JMXControl;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusAggregator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.mockito.Mockito.*;

public class ContainerAwareExecutionVenueTest {

	private ContainerAwareExecutionVenue ev;
	private ApplicationContext appContext;
	private Exportable exportable1;
	private Exportable exportable2;
	private GateListener gateListener1;
	private GateListener gateListener2;
    private IdentityResolverFactory identityResolverFactory;
    private MonitorRegistry monitorRegistry;
    private StatusAggregator statusAggregator;

    @Before
	public void before() {
		ev = new ContainerAwareExecutionVenue();
		appContext = mock(ApplicationContext.class);

        identityResolverFactory = new IdentityResolverFactory();
        ev.setIdentityResolverFactory(identityResolverFactory);

        monitorRegistry = mock(MonitorRegistry.class);
        ev.setMonitorRegistry(monitorRegistry);
        statusAggregator = mock(StatusAggregator.class);
        when(statusAggregator.getStatus()).thenReturn(Status.OK);
        when(monitorRegistry.getStatusAggregator()).thenReturn(statusAggregator);
	}

	@Test
	public void testOnApplicationEvent() {
		//Set up dependencies
		JMXControl jmxControl = new JMXControl(null);
		when(appContext.getBean(JMXControl.BEAN_JMX_CONTROL)).thenReturn(jmxControl);
		setupExportables();
		setupGateListeners();

		//raise the event
		ev.onApplicationEvent(new ContextRefreshedEvent(appContext));

		//Verify that all exportables were exported
		verify(exportable1).export(jmxControl);
		verify(exportable2).export(jmxControl);

		//Verify that all registered gate listeners have been notified
		verify(gateListener1).onCougarStart();
		verify(gateListener2).onCougarStart();
	}

	private void setupGateListeners() {
		gateListener1 = mock(GateListener.class);
		gateListener2 = mock(GateListener.class);
		when(gateListener1.getPriority()).thenReturn(1);
		when(gateListener2.getPriority()).thenReturn(10);
		ev.registerStartingListener(gateListener1);
		ev.registerStartingListener(gateListener2);
	}

	private void setupExportables() {
		exportable1 = mock(Exportable.class);
		exportable2 = mock(Exportable.class);
		ev.registerExportable(exportable1);
		ev.registerExportable(exportable2);
	}

}
