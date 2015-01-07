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

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.security.IdentityResolverFactory;
import com.betfair.cougar.core.impl.CougarInternalOperations;
import com.betfair.cougar.util.configuration.PropertyConfigurer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import static com.betfair.cougar.core.impl.ev.BaseExecutionVenue.DefinedExecutable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for ServiceRegisterableExecutionVenue
 */
public class ServiceRegisterableExecutionVenueTest {

    private OperationKey op1Key = new OperationKey(new ServiceVersion("v1.0"), "Service1", "Operation1");
    private OperationKey op2Key = new OperationKey(new ServiceVersion("v1.0"), "Service1", "Operation2");
    private OperationKey fooOp1Key = new OperationKey(op1Key, "foo");
    private OperationKey fooOp2Key = new OperationKey(op2Key, "foo");
    private OperationKey barOp1Key = new OperationKey(op1Key, "bar");
    private OperationKey barOp2Key = new OperationKey(op2Key, "bar");
    private OperationKey internalOp1Key = new OperationKey(op1Key, CougarInternalOperations.COUGAR_IN_PROCESS_NAMESPACE);
    private OperationKey internalOp2Key = new OperationKey(op2Key, CougarInternalOperations.COUGAR_IN_PROCESS_NAMESPACE);

    private OperationDefinition op1Def = new SimpleOperationDefinition(op1Key, null, null);
    private OperationDefinition op2Def = new SimpleOperationDefinition(op2Key, null, null);
    private ServiceDefinition serviceDef = new ServiceDefinition() {
        @Override
        public OperationDefinition[] getOperationDefinitions() {
            return new OperationDefinition[] {op1Def, op2Def};
        }
        @Override
        public String getServiceName() {
            return "Service1";
        }
        @Override
        public ServiceVersion getServiceVersion() {
            return new ServiceVersion("v1.0");
        }
    };
    private ServiceRegisterableExecutionVenue ev;
    private IdentityResolverFactory fact;
    private ServiceLogManager manager;
    private ServiceLogManagerFactory managerFactory;
    private ExecutableResolver resolver;
    private ApplicationContext appContext;
    private Service service;
    private IdentityResolverFactory identityResolverFactory;

    @Before
    public void before() {
        managerFactory = mock(ServiceLogManagerFactory.class);
        resolver = mock(ExecutableResolver.class);
        ev = new ServiceRegisterableExecutionVenue();
        ev.setServiceLogManagerFactory(managerFactory);
        appContext = mock(ApplicationContext.class);
        service = mock(Service.class);
        manager = mock(ServiceLogManager.class);

        when(managerFactory.create(anyString(), anyString(), any(ServiceVersion.class))).thenReturn(manager);

        identityResolverFactory = new IdentityResolverFactory();
        ev.setIdentityResolverFactory(identityResolverFactory);
    }

    @Test
    public void testRegisterService() {

        ev.registerService(serviceDef, service, resolver);

        //Verify that the OperationDefinition has been registered with the EV
        assertEquals(op1Def, ev.getOperationDefinition(op1Key));
        assertEquals(op2Def, ev.getOperationDefinition(op2Key));
        // verify that the internal in process namespace has also been registered
        assertEquals(op1Def, ev.getOperationDefinition(internalOp1Key));
        assertEquals(op2Def, ev.getOperationDefinition(internalOp2Key));
        assertNull(ev.getOperationDefinition(fooOp1Key));
        assertNull(ev.getOperationDefinition(fooOp2Key));
        assertNull(ev.getOperationDefinition(barOp1Key));
        assertNull(ev.getOperationDefinition(barOp2Key));
    }

    @Test
    public void testRegisterServiceWithNamespace() {

        ev.registerService("foo", serviceDef, service, resolver);

        //Verify that the OperationDefinition has been registered with the EV under the namespace
        assertNull(ev.getOperationDefinition(op1Key));
        assertNull(ev.getOperationDefinition(op2Key));
        // internal in-process namespace should only be registered for a service with the default namespace
        assertNull(ev.getOperationDefinition(internalOp1Key));
        assertNull(ev.getOperationDefinition(internalOp2Key));
        assertEquals(op1Def, ev.getOperationDefinition(fooOp1Key));
        assertEquals(op2Def, ev.getOperationDefinition(fooOp2Key));
        assertNull(ev.getOperationDefinition(barOp1Key));
        assertNull(ev.getOperationDefinition(barOp2Key));
    }

    @Test
    public void testRegisterServiceTwoDifferentNamespaces() {

        ev.registerService("foo", serviceDef, service, resolver);
        ev.registerService("bar", serviceDef, service, resolver);

        assertNull(ev.getOperationDefinition(op1Key));
        assertNull(ev.getOperationDefinition(op2Key));
        // internal in-process namespace should only be registered for a service with the default namespace
        assertNull(ev.getOperationDefinition(internalOp1Key));
        assertNull(ev.getOperationDefinition(internalOp2Key));
        assertEquals(op1Def, ev.getOperationDefinition(fooOp1Key));
        assertEquals(op2Def, ev.getOperationDefinition(fooOp2Key));
        assertEquals(op1Def, ev.getOperationDefinition(barOp1Key));
        assertEquals(op2Def, ev.getOperationDefinition(barOp2Key));
    }

    @Test
    public void testRegisterServiceNamespaceAndNot() {

        ev.registerService("foo", serviceDef, service, resolver);
        ev.registerService(serviceDef, service, resolver);

        assertEquals(op1Def, ev.getOperationDefinition(op1Key));
        assertEquals(op2Def, ev.getOperationDefinition(op2Key));
        // verify that the internal in process namespace has also been registered
        assertEquals(op1Def, ev.getOperationDefinition(internalOp1Key));
        assertEquals(op2Def, ev.getOperationDefinition(internalOp2Key));
        assertEquals(op1Def, ev.getOperationDefinition(fooOp1Key));
        assertEquals(op2Def, ev.getOperationDefinition(fooOp2Key));
        assertNull(ev.getOperationDefinition(barOp1Key));
        assertNull(ev.getOperationDefinition(barOp2Key));
    }
    @Test
    public void testRegisterServiceTwiceWithSameNamespace() {

        ev.registerService("foo", serviceDef, service, resolver);

        try {
            ev.registerService("foo", serviceDef, service, resolver);
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    @Test
    public void testOnApplicationEvent() {
        ev.registerService(serviceDef, service, resolver);

        //raise the event
        ev.onApplicationEvent(new ContextRefreshedEvent(appContext));

        verify(service).init(any(ContainerContext.class));
    }

    @Test
    public void timeoutSet() {
        PropertyConfigurer.getAllLoadedProperties().put("timeout.foo:Service1/v1.0/Operation1","1000");
        PropertyConfigurer.getAllLoadedProperties().put("timeout.Service1/v1.0/Operation2","100");
        ev.registerService("foo", serviceDef, service, resolver);
        ev.registerService(serviceDef, service, resolver);
        DefinedExecutable fooOp1De = ev.getDefinedExecutable(fooOp1Key);
        assertEquals(1000, fooOp1De.getMaxExecutionTime());
        DefinedExecutable op2De = ev.getDefinedExecutable(op2Key);
        assertEquals(100, op2De.getMaxExecutionTime());
    }
}
