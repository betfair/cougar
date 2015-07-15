/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.client;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class TestRunner {

    private List<Future<TestResult>> resultFutures = new LinkedList<>();
    private ForkJoinPool pool;

    public TestRunner(int parallelism) {
        pool = new ForkJoinPool(parallelism);
    }

    public void runTest(final ClientTest test) {
        ForkJoinTask<TestResult> task = new ForkJoinTask<TestResult>() {

            private final TestResult ret = new TestResult(test.getName(), test.getServerVariant());

            @Override
            public TestResult getRawResult() {
                return ret;
            }

            @Override
            protected void setRawResult(TestResult value) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected boolean exec() {
                try {
                    test.test(ret);
                }
                catch (Exception e) {
                    ret.setError(e);
                }
                return true;
            }
        };
        pool.execute(task);
        resultFutures.add(task);
    }

    public List<Future<TestResult>> await(int ms) throws InterruptedException {
        pool.shutdown();
        pool.awaitTermination(ms, TimeUnit.MILLISECONDS);
        return resultFutures;
    }


}
