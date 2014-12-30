/*
 * Copyright 2014, Simon Matić Langford
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


import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class WaitingObserverTest {
    @Test
    public void awaitMillis_noTimeout() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        notifyAfter(observer, 100);
        assertTrue(observer.await(0));
    }
    @Test
    public void awaitMillis_noTimeout_alreadyHappened() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        notifyAfter(observer, 0);
        assertTrue(observer.await(0));
    }
    @Test
    public void awaitMillis_negativeTimeout() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        notifyAfter(observer, 100);
        assertTrue(observer.await(-1));
    }
    @Test
    public void awaitMillis_withTimeout_Ok() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        notifyAfter(observer, 100);
        assertTrue(observer.await(1000));
    }
    @Test
    public void awaitMillis_withTimeout_AlreadyHappened() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        notifyAfter(observer, 0);
        assertTrue(observer.await(1000));
    }
    @Test
    public void awaitMillis_withTimeout_Fails() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        notifyAfter(observer, 1000);
        assertFalse(observer.await(100));
    }
    @Test
    public void awaitTimeConstraints_noTimeout() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        TimeConstraints tc = mock(TimeConstraints.class);
        when(tc.getTimeRemaining()).thenReturn(null);
        notifyAfter(observer, 100);
        assertTrue(observer.await(tc));
    }
    @Test
    public void awaitTimeConstraints_withTimeout_Ok() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        TimeConstraints tc = mock(TimeConstraints.class);
        when(tc.getTimeRemaining()).thenReturn(1000L);
        notifyAfter(observer, 100);
        assertTrue(observer.await(tc));
    }
    @Test
    public void awaitTimeConstraints_withTimeout_AlreadyHappened() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        TimeConstraints tc = mock(TimeConstraints.class);
        when(tc.getTimeRemaining()).thenReturn(1000L);
        notifyAfter(observer, 0);
        assertTrue(observer.await(tc));
    }
    @Test
    public void awaitTimeConstraints_withTimeout_Fails() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        TimeConstraints tc = mock(TimeConstraints.class);
        when(tc.getTimeRemaining()).thenReturn(100L);
        notifyAfter(observer, 1000);
        assertFalse(observer.await(tc));
    }
    @Test
    public void awaitTimeConstraints_expiredBeforeCallButHappenedBeforeAwait() throws InterruptedException {
        WaitingObserver observer = new WaitingObserver();
        TimeConstraints tc = mock(TimeConstraints.class);
        when(tc.getTimeRemaining()).thenReturn(-5L);
        notifyAfter(observer, 0);
        assertTrue(observer.await(tc));
    }

    private void notifyAfter(final WaitingObserver observer, final long millis) {
        if (millis == 0) {
            observer.onResult(new ExecutionResult(null));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millis);
                }
                catch (InterruptedException ie) {
                    // meh
                }
                observer.onResult(new ExecutionResult(null));
            }
        }).start();
    }
}
