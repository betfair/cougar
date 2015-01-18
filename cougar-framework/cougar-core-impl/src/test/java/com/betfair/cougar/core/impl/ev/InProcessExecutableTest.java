/*
 * Copyright 2014, Simon Matić Langford
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
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.impl.security.IdentityChainImpl;
import com.betfair.cougar.core.impl.tracing.TracingEndObserver;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.Same;

import java.util.Date;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 */
public class InProcessExecutableTest {

    @BeforeClass
    public static void setupUuid()
    {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

    @Test
    public void subCall()
    {
        Tracer tracer = mock(Tracer.class);
        InProcessExecutable victim = new InProcessExecutable(tracer);

        ExecutionContext ctx = mock(ExecutionContext.class);
        OperationKey expectedOp = new OperationKey(new ServiceVersion(1,0),"Wibble","wobble");
        OperationKey op = new OperationKey(expectedOp, "_IN_PROCESS");
        Object[] args = new Object[1];
        ExecutionObserver obs = mock(ExecutionObserver.class);
        ExecutionVenue venue = mock(ExecutionVenue.class);
        TimeConstraints constraints = mock(TimeConstraints.class);

        RequestUUID parentUuid = new RequestUUIDImpl();
        when(ctx.getRequestUUID()).thenReturn(parentUuid);
        when(ctx.isTransportSecure()).thenReturn(false);
        IdentityChain mockIdentityChain = new IdentityChainImpl();
        when(ctx.getIdentity()).thenReturn(mockIdentityChain);
        GeoLocationDetails mockLocation = mock(GeoLocationDetails.class);
        when(ctx.getLocation()).thenReturn(mockLocation);
        when(ctx.getReceivedTime()).thenReturn(new Date(0L));
        when(ctx.getRequestTime()).thenReturn(new Date(0L));
        when(ctx.getTransportSecurityStrengthFactor()).thenReturn(0);


        victim.execute(ctx, op, args, obs, venue, constraints);

        ArgumentCaptor<ExecutionContext> arg1 = ArgumentCaptor.forClass(ExecutionContext.class);
        ArgumentCaptor<OperationKey> arg2 = ArgumentCaptor.forClass(OperationKey.class);
        ArgumentCaptor<Object[]> arg3 = ArgumentCaptor.forClass(Object[].class);
        ArgumentCaptor<ExecutionObserver> arg4 = ArgumentCaptor.forClass(ExecutionObserver.class);
        ArgumentCaptor<TimeConstraints> arg5 = ArgumentCaptor.forClass(TimeConstraints.class);

        // moved from this as it was failing and v hard to work out which bit was failing
//        verify(venue, times(1)).execute(argThat(isSubContextOf(ctx)),eq(new OperationKey(op, null)),same(args),
//                argThat(isTracingEndObserver(obs, parentUuid, op, tracer)),same(constraints));

        verify(venue, times(1)).execute(arg1.capture(), arg2.capture(), arg3.capture(), arg4.capture(), arg5.capture());

        assertThat(arg1.getValue(), isSubContextOf(ctx));
        assertThat(arg2.getValue(), new Equals(expectedOp));
        assertThat(arg3.getValue(), new Same(args));
        assertThat(arg4.getValue(), isTracingEndObserver(obs, parentUuid, expectedOp, tracer));
        assertThat(arg5.getValue(), new Same(constraints));
    }

    private static boolean isSubUuidOf(RequestUUID subUuid, RequestUUID parentUuid, StringBuilder buffer) {

        if (parentUuid.getRootUUIDComponent()==null && !subUuid.getRootUUIDComponent().equals(parentUuid.getLocalUUIDComponent())) {
            buffer.append("Sub uuid isn't rooted at parent");
            return false;
        }
        if (!subUuid.getParentUUIDComponent().equals(parentUuid.getLocalUUIDComponent())) {
            buffer.append("Sub uuid doesn't have correct parent");
            return false;
        }
        if (subUuid.getLocalUUIDComponent().equals(parentUuid.getLocalUUIDComponent())) {
            buffer.append("Sub uuid local component is same as parent");
            return false;
        }

        return true;
    }

    private Matcher<ExecutionObserver> isTracingEndObserver(final ExecutionObserver child, final RequestUUID parentUuid, final OperationKey key, final Tracer tracer) {
        return new BaseMatcher<ExecutionObserver>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof TracingEndObserver)) {
                    return false;
                }

                TracingEndObserver tracingObserver = (TracingEndObserver) o;
                if (tracingObserver.getObs() != child) {
                    return false;
                }
                if (tracingObserver.getParentUuid() != parentUuid) {
                    return false;
                }
                if (!tracingObserver.getKey().equals(key)) {
                    return false;
                }
                if (tracingObserver.getTracer() != tracer) {
                    return false;
                }
                if (!isSubUuidOf(tracingObserver.getCallUuid(), parentUuid, new StringBuilder())) {
                    return false;
                }



                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("isTracingEndObserver()");
            }
        };
    }

    private Matcher<ExecutionContext> isSubContextOf(final ExecutionContext ctx) {
        return new BaseMatcher<ExecutionContext>() {
            StringBuilder buffer = new StringBuilder();
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof  ExecutionContext)) {
                    buffer.append("Not an ExecutionContext!");
                    return false;
                }

                ExecutionContext subCandidate = (ExecutionContext) o;

                if (subCandidate.traceLoggingEnabled() != ctx.traceLoggingEnabled()) {
                    buffer.append("Trace logging not " + ctx.traceLoggingEnabled());
                    return false;
                }

                if (!subCandidate.getLocation().equals(ctx.getLocation())) {
                    buffer.append("Location not " + ctx.getLocation());
                    return false;
                }

                if (!subCandidate.isTransportSecure()) {
                    buffer.append("Transport not secure");
                    return false;
                }

                if (!subCandidate.getIdentity().equals(ctx.getIdentity())) {
                    buffer.append("Identity not "+ctx.getIdentity());
                    return false;
                }

                if (subCandidate.getTransportSecurityStrengthFactor() != Integer.MAX_VALUE) {
                    buffer.append("Transport strength not " + Integer.MAX_VALUE);
                    return false;
                }

                if (!subCandidate.getReceivedTime().equals(subCandidate.getRequestTime())) {
                    buffer.append("Received time not request time");
                    return false;
                }

                if (subCandidate.getReceivedTime().compareTo(ctx.getReceivedTime()) <= 0) {
                    buffer.append("Received time before or same as " + ctx.getReceivedTime());
                    return false;
                }

                RequestUUID subUuid = subCandidate.getRequestUUID();
                RequestUUID parentUuid = ctx.getRequestUUID();

                if (!isSubUuidOf(subUuid, parentUuid, buffer)) {
                    return false;
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("isSubContextOf("+ctx+"): "+buffer.toString());
            }
        };
    }
}
