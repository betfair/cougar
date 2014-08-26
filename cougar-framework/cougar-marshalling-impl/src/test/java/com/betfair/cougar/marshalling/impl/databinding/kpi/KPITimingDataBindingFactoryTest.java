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

import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import com.betfair.tornjak.kpi.KPIMonitor;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test {@link KPITimingDataBindingFactory}.
 */
public class KPITimingDataBindingFactoryTest {

    private KPIMonitor monitor;
    private DataBindingFactory baseFactory;

    private Marshaller mockMarshaller;
    private FaultMarshaller mockFaultMarshaller;
    private UnMarshaller mockUnMarshaller;
    private FaultUnMarshaller mockFaultUnMarshaller;

    private KPITimingDataBindingFactory factory;

    @Before
    public void setUp() {

        monitor = mock(KPIMonitor.class);
        baseFactory = new MockFactory();

        mockMarshaller = mock(Marshaller.class);
        mockFaultMarshaller = mock(FaultMarshaller.class);
        mockUnMarshaller = mock(UnMarshaller.class);
        mockFaultUnMarshaller = mock(FaultUnMarshaller.class);

        factory = new KPITimingDataBindingFactory(monitor, baseFactory, "foo");
    }

    @Test
    public void testGetMarshaller() {

        Marshaller marshaller = factory.getMarshaller();

        // remaining
        marshaller.marshall(null, null, null, false);

        assertSame(marshaller, factory.getMarshaller());    // same instance always
        expectMarshall();
    }

    @Test
    public void testGetFaultMarshaller() {

        FaultMarshaller marshaller = factory.getFaultMarshaller();

        // remaining
        marshaller.marshallFault(null, null, null);

        assertSame(marshaller, factory.getFaultMarshaller());    // same instance always
        expectFaultMarshall();
    }

    @Test
    public void testGetUnmarshaller() {

        UnMarshaller marshaller = factory.getUnMarshaller(); // any old class

        // remaining
        marshaller.unmarshall(null,(Class)null,null, false);

        assertSame(marshaller, factory.getUnMarshaller());    // same instance always
        expectUnMarshall();
    }

    @Test
    public void testGetFaultUnMarshaller() {

        FaultUnMarshaller faultUnMarshaller = factory.getFaultUnMarshaller();

        faultUnMarshaller.unMarshallFault(null, null);

        assertSame(faultUnMarshaller, factory.getFaultUnMarshaller());
        expectFaultUnMarshall();

    }

    private void expectMarshall() {
        verify(mockMarshaller).marshall(null,null, null, false);
        verify(monitor).addEvent(eq("Cougar.ws.foo.marshall"), anyLong(), eq(true));
    }

    private void expectFaultMarshall() {
        verify(mockFaultMarshaller).marshallFault(null, null, null);
        verify(monitor).addEvent(eq("Cougar.ws.foo.marshallFault"), anyLong(), eq(true));
    }

    private void expectUnMarshall() {
        verify(mockUnMarshaller).unmarshall(any(InputStream.class), any(Class.class), anyString(), anyBoolean());
        verify(monitor).addEvent(eq("Cougar.ws.foo.unmarshall"), anyLong(), eq(true));
    }

    private void expectFaultUnMarshall() {
        verify(mockFaultUnMarshaller).unMarshallFault(any(InputStream.class), anyString());
        verify(monitor).addEvent(eq("Cougar.ws.foo.unmarshallFault"), anyLong(), eq(true));
    }



    private class MockFactory implements DataBindingFactory {

        @Override
        public FaultMarshaller getFaultMarshaller() {
            return mockFaultMarshaller;
        }

        @Override
        public FaultUnMarshaller getFaultUnMarshaller() {
            return mockFaultUnMarshaller;
        }

        @Override
        public Marshaller getMarshaller() {
            return mockMarshaller;
        }

        @Override
        public UnMarshaller getUnMarshaller() {
            return mockUnMarshaller;
        }

    }
}
