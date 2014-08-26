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

package com.betfair.cougar.core.impl.kpi;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.tornjak.kpi.KPIMonitor;
import com.betfair.tornjak.kpi.KPITimer;
import com.betfair.tornjak.kpi.aop.KPIAsyncTimedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Cougar-specific aspect which handles {@link KPIAsyncTimedEvent}-annotated methods. The methods must also have an
 * argument of type {@link ExecutionObserver} in the last position. Method signatures on the generated
 * async service interfaces match this pattern.
 */
@Aspect
public class KPIAsyncTimer {

    private KPIMonitor monitor;

    @Pointcut("@annotation(timer) && args(.., observer)")
    private void cougarAsyncMethod(KPIAsyncTimedEvent timer, ExecutionObserver observer) {

    }

    @Around("cougarAsyncMethod(event, observer)")
    public Object measureAsyncMethod(final ProceedingJoinPoint pjp, KPIAsyncTimedEvent event, ExecutionObserver observer) throws Throwable {
        final String eventValue = event.value();
        final String eventOperation = event.operation();
        final String name = eventValue.isEmpty() ? pjp.getTarget().getClass().getSimpleName() : eventValue;
        final String operation = eventOperation.isEmpty() ? pjp.getSignature().getName() : eventOperation;

        final KPITimer timer = new KPITimer();
        timer.start();
        final PerformanceMonitoringExecutionObserver perfObserver =
                new PerformanceMonitoringExecutionObserver(observer, monitor, timer,
                                                           event.catchFailures(), name, operation);

        return pjp.proceed(replaceObserver(perfObserver, pjp.getArgs()));
    }

    private Object[] replaceObserver(PerformanceMonitoringExecutionObserver perfObserver, Object[] args) {
       //As per the class javadocs, the ExecutionObserver must be the last element in the an the arg list
        if (args != null &&
            args.length > 0 &&
            args[args.length-1] instanceof ExecutionObserver) {
            args[args.length-1] = perfObserver;
        }
        return args;
    }

    public void setMonitor(KPIMonitor monitor) {
        this.monitor = monitor;
    }


}
