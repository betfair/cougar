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

import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.util.jmx.JMXControl;
import com.betfair.cougar.logging.EventLoggingRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultServiceLogManagerFactoryTest {

	private final ServiceVersion serviceVersion = new ServiceVersion("v1.0");
	private final String serviceName = "Service1";
	private final String loggerName = "testLogger";
	private final OperationKey op1Key = new OperationKey(serviceVersion, serviceName, "Operation1");
	private final OperationKey op2Key = new OperationKey(serviceVersion, serviceName, "Operation2");

	private DefaultServiceLogManagerFactory factory;
	private EventLoggingRegistry loggingRegistry;

	@Before
	public void init() {
        loggingRegistry = mock(EventLoggingRegistry.class);
		factory = new DefaultServiceLogManagerFactory(loggingRegistry);
		when(loggingRegistry.registerConcreteLogger(null, serviceName)).thenReturn(loggerName);
        when(loggingRegistry.registerConcreteLogger("foo", serviceName)).thenReturn("foo"+loggerName);
        when(loggingRegistry.registerConcreteLogger("bar", serviceName)).thenReturn("bar" + loggerName);
	}


	@Test
	public void create() {
		//Test a service and operation that aren't registered
		ServiceLogManager em = factory.create(op1Key.getNamespace(), op1Key.getServiceName(), op1Key.getVersion());
		assertEquals(loggerName, em.getLoggerName());
	}

    @Test
    public void createUnderDifferentNamespaces() {
        String[] namespaces = {null, "foo", "bar"};


        for (String ns: namespaces) {
            ServiceLogManager em;
            String nsLoggerName = (ns == null ? "" : ns) + loggerName;
            OperationKey ns1Key = new OperationKey(op1Key, ns);
            OperationKey ns2Key = new OperationKey(op2Key, ns);

            em = factory.create(ns1Key.getNamespace(), ns1Key.getServiceName(), ns1Key.getVersion());
            assertNotNull(em);
            assertEquals(nsLoggerName, em.getLoggerName());

            em = factory.create(ns2Key.getNamespace(), ns2Key.getServiceName(), ns2Key.getVersion());
            assertNotNull(em);
            assertEquals(nsLoggerName, em.getLoggerName());

            em = factory.create(ns, serviceName, serviceVersion);
            assertNotNull(em);
            assertEquals(nsLoggerName, em.getLoggerName());
        }
    }
}
