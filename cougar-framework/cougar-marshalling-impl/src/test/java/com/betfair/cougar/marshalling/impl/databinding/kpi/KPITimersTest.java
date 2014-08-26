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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.io.OutputStream;

import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.tornjak.kpi.KPIMonitor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit tests {@link KPITimingMarshaller}, {@link KPITimingFaultMarshaller} and
 * {@link KPITimingUnMarshaller}. These tests re-use a lot of functionality, no point in separate
 * classes.
 */
public class KPITimersTest {

    private static final String TEST_KPI_NAME = "test.kpi.name";

    private static final String TEST_ERROR_MSG = "test error message";

    private static final Object RESULT = new Object();
    private static final String ENCODING = "TESTENCODING";
    private OutputStream os;
    private CougarFault fault;
    private InputStream inputStream;

    private KPIMonitor monitor;

    private Marshaller baseMarshaller;
    private FaultMarshaller baseFaultMarshaller;
    private UnMarshaller baseUnmarshaller;


    @Before
    public void setUp() {
        fault = mock(CougarFault.class);
        inputStream = mock(InputStream.class);
        os = mock(OutputStream.class);

        monitor = mock(KPIMonitor.class);

        baseMarshaller = mock(Marshaller.class);
        baseFaultMarshaller = mock(FaultMarshaller.class);
        baseUnmarshaller = mock(UnMarshaller.class);
    }

    @Test
    public void testMarshallerSuccess() {
        doAnswer(doStuff(false)).when(baseMarshaller).marshall(os, RESULT, ENCODING, false);

        KPITimingMarshaller marshaller = new KPITimingMarshaller(monitor, TEST_KPI_NAME, baseMarshaller);
        marshaller.marshall(os, RESULT, ENCODING, false);

        expectKPIUpdate(TEST_KPI_NAME, true);
    }

    @Test
    public void testMarshallerFailure() {
        doAnswer(doStuff(true)).when(baseMarshaller).marshall(os, RESULT, ENCODING, false);

        KPITimingMarshaller marshaller
            = new KPITimingMarshaller(monitor, TEST_KPI_NAME, baseMarshaller);
        try {
            marshaller.marshall(os, RESULT, ENCODING, false);
            fail("Should have thrown an exception");
        }
        catch (RuntimeException e) {
            assertEquals(TEST_ERROR_MSG, e.getMessage());
        }
        expectKPIUpdate(TEST_KPI_NAME, false);
    }

    @Test
    public void testFaultMarshallerSuccess() {
        doAnswer(doStuff(false)).when(baseFaultMarshaller).marshallFault(os, fault, ENCODING);

        KPITimingFaultMarshaller marshaller
            = new KPITimingFaultMarshaller(monitor, TEST_KPI_NAME, baseFaultMarshaller);
        marshaller.marshallFault(os, fault, ENCODING);
        expectKPIUpdate(TEST_KPI_NAME, true);
    }

    @Test
    public void testFaultMarshallerFailure() {
        doAnswer(doStuff(true)).when(baseFaultMarshaller).marshallFault(os, fault, ENCODING);

        KPITimingFaultMarshaller marshaller
            = new KPITimingFaultMarshaller(monitor, TEST_KPI_NAME, baseFaultMarshaller);
        try {
            marshaller.marshallFault(os, fault, ENCODING);
            fail("Should have thrown an exception");
        }
        catch (RuntimeException e) {
            assertEquals(TEST_ERROR_MSG, e.getMessage());
        }
        expectKPIUpdate(TEST_KPI_NAME, false);
    }

    @Test
    public void testUnmarshallerSuccess() {
        doAnswer(doStuff(false)).when(baseUnmarshaller).unmarshall(inputStream,String.class,"test", true);

        KPITimingUnMarshaller marshaller
            = new KPITimingUnMarshaller(monitor, TEST_KPI_NAME, baseUnmarshaller);
        marshaller.unmarshall(inputStream,String.class,"test", true);
        expectKPIUpdate(TEST_KPI_NAME, true);
    }

    @Test
    public void testUnmarshallerFailure() {
        doAnswer(doStuff(true)).when(baseUnmarshaller).unmarshall(inputStream,String.class,"test", true);

        KPITimingUnMarshaller marshaller
            = new KPITimingUnMarshaller(monitor, TEST_KPI_NAME, baseUnmarshaller);
        try {
            marshaller.unmarshall(inputStream,String.class,"test", true);
            fail("Should have thrown an exception");
        }
        catch (RuntimeException e) {
            assertEquals(TEST_ERROR_MSG, e.getMessage());
        }
        expectKPIUpdate(TEST_KPI_NAME, false);
    }

    protected <T> Answer<T> doStuff(final boolean throwException) {
        return new Answer<T>() {

            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(20);
                if (throwException) {
                    throw new RuntimeException(TEST_ERROR_MSG);
                }
                return null;
            }
        };
    }

    private void expectKPIUpdate(final String msgSuffix, final boolean success) {
        verify(monitor, times(1)).addEvent(argThat(validKpiName(msgSuffix)), doubleThat(nonZero()), eq(success));
    }

    private Matcher<String> validKpiName(final String msgSuffix) {
        return new BaseMatcher<String>() {

            @Override
            public boolean matches(Object o) {
                return msgSuffix.equals(o);
            }

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("Update with KPI name ending with " + msgSuffix);
            }
        };
    }

    private Matcher<Double> nonZero() {
        return new BaseMatcher<Double>() {

            @Override
            public boolean matches(Object arg0) {
                assertTrue("Timed value must be non-zero", ((Double)arg0).doubleValue() > 0);
                return true;
            }

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("Non-zero duration");
            }
        };
    }
}
