/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.testng;

import com.betfair.testing.utils.cougar.manager.CougarManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class TestListener implements ITestListener, Runnable {

    public TestListener() {
        System.out.println("Listener installed");
    }

    private Thread thread;
    private AtomicReference<CountDownLatch> activityLatch = new AtomicReference<>(new CountDownLatch(1));

    @Override
    public void run() {
        while (true) {
            // wait on something for 9 minutes,
            try {
                if (!activityLatch.get().await(9, TimeUnit.MINUTES)) {
                    System.err.println("Thread dump:");
                    System.err.println("============");
                    for (Map.Entry<Thread,StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                        System.err.println("Thread: "+entry.getKey().getId()+" - "+entry.getKey().getName());
                        for (StackTraceElement s : entry.getValue()) {
                            System.err.println(s);
                        }
                        System.err.println("\n");
                    }

                    // server dump
//                    CougarManager.getInstance().getCougarHelpers().dumpThreads();
                }
            }
            catch (InterruptedException ie) {
                // ignore, go round the loop again
            }
        }
    }

    private void activity() {
        CountDownLatch next = new CountDownLatch(1);
        activityLatch.getAndSet(next).countDown();
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
//        System.out.println();
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        System.out.println(".");
        activity();
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        System.out.println(".");
        activity();
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        System.out.println(".");
        activity();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
    }

    @Override
    public void onStart(ITestContext iTestContext) {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        thread.stop();
    }
}
