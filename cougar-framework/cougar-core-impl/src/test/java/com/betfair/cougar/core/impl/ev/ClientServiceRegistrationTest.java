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
import com.betfair.cougar.core.api.BindingDescriptor;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceRegistrar;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.transports.EventTransport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for ClientServiceRegistration
 */
public class ClientServiceRegistrationTest {
    private ClientServiceRegistration cut = new ClientServiceRegistration();

    @Mock
    private ExecutableResolver executableResolver;

    @Mock
    private Service service;

    @Mock
    private ServiceDefinition serviceDefinition;

    @Mock
    private ContainerAwareExecutionVenue ev;

    @Mock
    private CompoundExecutableResolver compoundExecutableResolver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        cut.setResolver(executableResolver);
        cut.setServiceDefinition(serviceDefinition);
    }

    @Test
    public void testIntroduceNoNamespace() {
        cut.introduceServiceToEV(ev, ev, compoundExecutableResolver);
        verify(compoundExecutableResolver).registerExecutableResolver(eq((String)null), eq(executableResolver));
        verify((ServiceRegistrar)ev).registerService(eq((String)null), eq(serviceDefinition), any(Service.class), eq(compoundExecutableResolver));

        verify(ev, never()).execute(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class), any(ExecutionObserver.class), any(TimeConstraints.class));
    }

    @Test
    public void testIntroduceWithNamespace() {
        cut.setNamespace("foo");
        cut.introduceServiceToEV(ev, ev, compoundExecutableResolver);
        verify(compoundExecutableResolver).registerExecutableResolver(eq("foo"), eq(executableResolver));
        verify((ServiceRegistrar)ev).registerService(eq("foo"), eq(serviceDefinition), any(Service.class), eq(compoundExecutableResolver));

        verify(ev, never()).execute(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class), any(ExecutionObserver.class), any(TimeConstraints.class));
    }

    @Test
    public void testSettingServiceBindingDoesNothing() {
        //Any operation called on a whingeingSet will throw an exception
        //It should not be interacted with in any way for this test
        cut.setBindingDescriptors(new WhingeingSet<BindingDescriptor>());
        cut.introduceServiceToEV(ev, ev, compoundExecutableResolver);
    }

    private static class WhingeingSet<T> implements Set<T> {

        @Override
        public int size() {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean isEmpty() {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean contains(Object o) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public Iterator iterator() {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public Object[] toArray() {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean add(Object o) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean remove(Object o) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean addAll(Collection c) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new IllegalAccessError("Shouldn't be here");
        }

        @Override
        public void clear() {
            throw new IllegalAccessError("Shouldn't be here");
        }
    }
}
