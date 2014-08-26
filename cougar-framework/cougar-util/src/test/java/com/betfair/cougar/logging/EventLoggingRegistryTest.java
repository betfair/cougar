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

package com.betfair.cougar.logging;

import java.io.IOException;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.logging.handlers.AbstractLogHandler;
import org.mockito.Mockito;


public class EventLoggingRegistryTest extends CougarUtilTestCase {
	private final EventLoggingRegistry registry = new EventLoggingRegistry();

	public void testRegisterLogger() {
		addHandler("foo", false);
		EventLogDefinition eld = registry.getInvokableLogger("foo");
		assertEquals(eld.getLogName(), "foo");
		assertEquals(eld.isAbstract(), false);

	}

	public void testRegisterAbstractLogger() {
		addHandler("foo", true);
		assertNull(registry.getInvokableLogger("foo"));
	}

	public void testRegisterTwoAbstractLoggers() {
		addHandler("foo", true);
		try {
			addHandler("bar", true);
			fail();
		} catch (IllegalStateException e) {
			// OK
		}
	}

	public void testCreateConcreteLogger() throws Exception {
		addHandler("foo", true);
        registry.registerConcreteLogger(null,"bar");
        EventLogDefinition eld = registry.getInvokableLogger("foobar");
        assertEquals(eld.getLogName(), "foobar");
        assertEquals(eld.isAbstract(), false);

		registry.registerConcreteLogger("ns","bar");
		eld = registry.getInvokableLogger("nsfoobar");
		assertEquals(eld.getLogName(), "nsfoobar");
		assertEquals(eld.isAbstract(), false);

		registry.registerConcreteLogger("ns","foo");
		eld = registry.getInvokableLogger("nsfoofoo");
		assertEquals(eld.getLogName(), "nsfoofoo");
		assertEquals(eld.isAbstract(), false);
	}

	public void testCreateConcreteLoggerNoAbstract() throws Exception {
		try {
			registry.registerConcreteLogger("ns","bar");
			fail();
		} catch (IllegalStateException e) {
			// OK
		}
	}

    public void testCreateConcreteLoggerDuplicateServices() throws Exception {
        final String myNamespace   = "myNamespace";
        final String myServiceName = "myServiceName";

        EventLoggingRegistry eventLoggingRegistry = new EventLoggingRegistry();

        EventLogDefinition abstractGlobalLogger = new EventLogDefinition() {
            private int callCount=0;
            public EventLogDefinition getConcreteLogger(String serviceName, String namespace) {
                if (++callCount > 1) {
                    fail("This should only have been called once!");
                }
                return super.getConcreteLogger(serviceName, namespace);
            }
        };

        abstractGlobalLogger.setAbstract(true);
        abstractGlobalLogger.setLogName("bob");
        abstractGlobalLogger.setHandler(Mockito.mock(AbstractLogHandler.class));
        abstractGlobalLogger.setRegistry(eventLoggingRegistry);
        eventLoggingRegistry.register(abstractGlobalLogger);

        EventLogDefinition eventLog = new EventLogDefinition();
        eventLog.setLogName(eventLog.deriveConcreteLogName(myNamespace, myServiceName));
        eventLog.setAbstract(false);
        eventLog.setRegistry(eventLoggingRegistry);

        eventLoggingRegistry.registerConcreteLogger(myNamespace, myServiceName);
        eventLoggingRegistry.registerConcreteLogger(myNamespace, myServiceName);
    }

	private void addHandler(String name, boolean isAbstract) {
		EventLogDefinition eld = new EventLogDefinition();
		eld.setRegistry(registry);
		eld.setAbstract(isAbstract);
		eld.setHandler(new AbstractLogHandler(isAbstract) {


			@Override
			protected AbstractLogHandler cloneHandlerToName(String logName, String serviceName, String namespace)
					throws IOException {
				return this;
			}
    	});
		eld.setLogName(name);
		eld.register();

	}
}
