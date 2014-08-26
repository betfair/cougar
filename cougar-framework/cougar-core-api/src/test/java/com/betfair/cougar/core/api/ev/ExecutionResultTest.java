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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import org.slf4j.LoggerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for @see ExecutionResult
 */
public class ExecutionResultTest {
    private static class TestCAE extends CougarApplicationException {
        public TestCAE() {
            super(ResponseCode.InternalError, "Bad");
        }

        @Override
        public List<String[]> getApplicationFaultMessages() {
            return null;
        }

        @Override
        public String getApplicationFaultNamespace() {
            return null;
        }
    }

    @Test
    public void testExceptionSelection()   {
        CougarApplicationException cae = new TestCAE();
        ExecutionResult r = new ExecutionResult(cae);
        assertEquals(ExecutionResult.ResultType.Fault, r.getResultType());
        assertNotNull(r.getFault());
        assertNull(r.getResult());
        assertNull(r.getSubscription());

        r = new ExecutionResult(new CougarFrameworkException("Too many beers"));
        assertEquals(ExecutionResult.ResultType.Fault, r.getResultType());
        assertNotNull(r.getFault());
        assertNull(r.getResult());
        assertNull(r.getSubscription());
    }

    @Test
    public void testSubscriptionResult() {
        Subscription sub = new Subscription() {
            @Override
            public void addListener(SubscriptionListener listener) {
            }

            @Override
            public void removeListener(SubscriptionListener listener) {
            }

            @Override
            public void close() {
                close(CloseReason.REQUESTED_BY_SUBSCRIBER);
            }

            @Override
            public void close(CloseReason reason) {
            }

            @Override
            public CloseReason getCloseReason() {
                return null;
            }
        };

        ExecutionResult r = new ExecutionResult(sub);
        assertEquals(r.getResultType(), ExecutionResult.ResultType.Subscription);
        assertNotNull(r.getSubscription());
        assertNull(r.getResult());
        assertNull(r.getFault());
    }

    @Test
    public void testVoidSuccessResult() {
        ExecutionResult r = new ExecutionResult();
        assertEquals(r.getResultType(), ExecutionResult.ResultType.Success);
        assertNull(r.getSubscription());
        assertNull(r.getResult());
        assertNull(r.getFault());
    }

    @Test
    public void testPopulatedSuccessResult() {
        ExecutionResult r = new ExecutionResult("success!");
        assertEquals(r.getResultType(), ExecutionResult.ResultType.Success);
        assertNull(r.getSubscription());
        assertNotNull(r.getResult());
        assertNull(r.getFault());
    }

}
