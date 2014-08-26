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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.transports.EventTransport;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for @AbstractServiceRegistration and ServiceRegistration
 */
public class ServiceRegistrationTest {

    private ServiceRegistration cut = new ServiceRegistration();

    private Set<BindingDescriptor> bindingDescriptorSet = new HashSet<BindingDescriptor>();

    @Mock
    private ExecutableResolver executableResolver;

    @Mock
    private Service service;

    @Mock
    private ServiceDefinition serviceDefinition;

    @Mock
    private EventTransport eventTransport;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private ContainerAwareExecutionVenue ev;

    @Mock
    private CompoundExecutableResolver compoundExecutableResolver;

    @Before
    public void setup() {
         MockitoAnnotations.initMocks(this);

        Set<EventTransport> eventTransports = new HashSet<EventTransport>();
        eventTransports.add(eventTransport);

        bindingDescriptorSet.add(Mockito.mock(BindingDescriptor.class));
        cut.setBindingDescriptors(bindingDescriptorSet);
        cut.setResolver(executableResolver);
        cut.setService(service);
        cut.setServiceDefinition(serviceDefinition);
        cut.setEventTransports(eventTransports);
    }

    @Test
    public void testIntroductionToEV() {
        OperationDefinition[] opDefs = new OperationDefinition[] {
            new SimpleOperationDefinition(
                    new OperationKey(new ServiceVersion("v1.0"), "testService", "event", OperationKey.Type.Event), null, null)
        };
        when(serviceDefinition.getOperationDefinitions(any(OperationKey.Type.class))).thenReturn(opDefs);


        cut.introduceServiceToEV(ev, ev, compoundExecutableResolver);
        verify((ServiceRegistrar)ev).registerService(eq((String) null), eq(serviceDefinition), eq(service), eq(compoundExecutableResolver));

        ArgumentCaptor<OperationKey> keyCaptor = ArgumentCaptor.forClass(OperationKey.class);
        verify(ev).execute(any(ExecutionContext.class), keyCaptor.capture(), any(Object[].class), any(ExecutionObserver.class), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
        assertTrue(keyCaptor.getValue().getType() == OperationKey.Type.Event);
    }

    @Test
    public void testIntroductionToEVWithNamespace() {
        OperationDefinition[] opDefs = new OperationDefinition[] {
            new SimpleOperationDefinition(
                    new OperationKey(new ServiceVersion("v1.0"), "testService", "event", OperationKey.Type.Event), null, null)
        };
        when(serviceDefinition.getOperationDefinitions(any(OperationKey.Type.class))).thenReturn(opDefs);

        cut.setNamespace("foo");
        cut.introduceServiceToEV(ev, ev, compoundExecutableResolver);
        verify((ServiceRegistrar)ev).registerService(eq("foo"), eq(serviceDefinition), eq(service),eq(compoundExecutableResolver));

        ArgumentCaptor<OperationKey> keyCaptor = ArgumentCaptor.forClass(OperationKey.class);
        verify(ev).execute(any(ExecutionContext.class), keyCaptor.capture(), any(Object[].class), any(ExecutionObserver.class), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
        assertTrue(keyCaptor.getValue().getType() == OperationKey.Type.Event);
        assertEquals(null, keyCaptor.getValue().getNamespace()); // Events are non namespace aware.
    }

    @Test
    public void testIntroductionToTransports() {
        BindingDescriptorRegistrationListener mockTransport = mock(BindingDescriptorRegistrationListener.class);

        List<BindingDescriptorRegistrationListener> transports = new ArrayList<BindingDescriptorRegistrationListener>();
        transports.add(mockTransport);

        cut.introduceServiceToTransports(transports.iterator());
        verify(mockTransport).notify(any(BindingDescriptor.class));
    }

    @Test
    public void testIntroductionToDefinedEventTransports() {
        EventTransport mockEventTransport1 = mock(EventTransport.class);
        EventTransport mockEventTransport2 = mock(EventTransport.class);

        List<BindingDescriptorRegistrationListener> transports = new ArrayList<BindingDescriptorRegistrationListener>();
        transports.add(mockEventTransport1);
        transports.add(mockEventTransport2);

        Set<EventTransport> eventTransports = new HashSet<EventTransport>();
        eventTransports.add(mockEventTransport1);
        cut.setEventTransports(eventTransports);

        cut.introduceServiceToTransports(transports.iterator());
        verify(mockEventTransport1).notify(any(BindingDescriptor.class));
        verifyZeroInteractions(mockEventTransport2);
    }
}
