/*
 * Copyright #{YEAR}, The Sporting Exchange Limited
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
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
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
        OperationKey op = new OperationKey(new ServiceVersion(1,0),"Wibble","wobble");
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

        verify(venue, times(1)).execute(argThat(isSubContextOf(ctx)),eq(new OperationKey(op, null)),same(args),same(obs),same(constraints));
    }

    private Matcher<ExecutionContext> isSubContextOf(final ExecutionContext ctx) {
        return new BaseMatcher<ExecutionContext>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof  ExecutionContext)) {
                    return false;
                }

                ExecutionContext subCandidate = (ExecutionContext) o;

                if (subCandidate.traceLoggingEnabled() != ctx.traceLoggingEnabled()) {
                    return false;
                }

                if (!subCandidate.getLocation().equals(ctx.getLocation())) {
                    return false;
                }

                if (!subCandidate.isTransportSecure()) {
                    return false;
                }

                if (!subCandidate.getIdentity().equals(ctx.getIdentity())) {
                    return false;
                }

                if (subCandidate.getTransportSecurityStrengthFactor() != Integer.MAX_VALUE) {
                    return false;
                }

                if (!subCandidate.getReceivedTime().equals(subCandidate.getRequestTime())) {
                    return false;
                }

                if (subCandidate.getReceivedTime().compareTo(ctx.getReceivedTime()) <= 0) {
                    return false;
                }

                RequestUUID subUuid = subCandidate.getRequestUUID();
                RequestUUID parentUuid = ctx.getRequestUUID();

                if (parentUuid.getRootUUIDComponent()==null && !subUuid.getRootUUIDComponent().equals(parentUuid.getLocalUUIDComponent())) {
                    return false;
                }
                if (!subUuid.getParentUUIDComponent().equals(parentUuid.getLocalUUIDComponent())) {
                    return false;
                }
                if (subUuid.getLocalUUIDComponent().equals(parentUuid.getLocalUUIDComponent())) {
                    return false;
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }
}
