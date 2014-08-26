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

package com.betfair.cougar.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.logging.Level;

/**
 * Specialised runner that runs the tests a specified number of times each. Call setNumRuns from a method annotated with @BeforeClass
 * to define the number of runs to do, defaults to 1.
 */
public class MultiRunner extends BlockJUnit4ClassRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(MultiRunner.class);

    private static int numRuns = 1;

    public static void setNumRuns(int numRuns) {
        MultiRunner.numRuns = numRuns;
    }

    public MultiRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        EachTestNotifier eachNotifier= makeNotifier(method, notifier);
        if (method.getAnnotation(Ignore.class) != null) {
            eachNotifier.fireTestIgnored();
            return;
        }

        eachNotifier.fireTestStarted();
        try {
            for (int i=0; i<numRuns; i++) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("--Starting run "+(i+1)+" of "+method.getName()+"--");
                }
                methodBlock(method).evaluate();
            }
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }
    protected EachTestNotifier makeNotifier(FrameworkMethod method,
                                            RunNotifier notifier) {
        Description description= describeChild(method);
        return new EachTestNotifier(notifier, description);
    }
}
