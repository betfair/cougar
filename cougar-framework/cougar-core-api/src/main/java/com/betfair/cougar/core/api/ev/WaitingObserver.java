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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * This class provides a synchronous observer that'll wait for a result.
 * Useful for providing a synchronous adapter around an async service.
 */
public class WaitingObserver implements ExecutionObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitingObserver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile ExecutionResult executionResult;

    @Override
    public void onResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
        latch.countDown();
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    /**
     *
     * @param timeoutInMillis - specifies time to wait in milli seconds for this operation to complete.  If the
     * value is <= 0, it will wait indefinitely.
     * @return returns true if a result was received, false if it timed out
     * @throws InterruptedException if the waiting call was interrupted
     */
    public boolean await(long timeoutInMillis) throws InterruptedException {
        if (timeoutInMillis > 0) {
            return latch.await(timeoutInMillis, TimeUnit.MILLISECONDS);
        } else {
            latch.await();
            return true;
        }
    }

    public boolean await(TimeConstraints timeConstraints) throws InterruptedException {
        Long waitTime = null;
        if (timeConstraints.getTimeRemaining() != null) {
            waitTime = timeConstraints.getTimeRemaining();
        }
        // if waitTime is null then that means no constraint
        if (waitTime == null) {
            latch.await();
            return true;
        }
        else {
            return latch.await(waitTime, TimeUnit.MILLISECONDS);
        }
    }
}