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

package com.betfair.cougar.util;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.concurrent.*;

/**
 * A thread pool executor that will report its status and queue sizes
 */
@ManagedResource
public class JMXReportingThreadPoolExecutor extends ThreadPoolExecutor {

    public JMXReportingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public JMXReportingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory tf) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, tf);
    }

    @Override
    @ManagedAttribute
    public long getTaskCount() {
        return super.getTaskCount();
    }

    @Override
    @ManagedAttribute
    public int getActiveCount() {
        return super.getActiveCount();
    }

    @Override
    @ManagedAttribute
    public int getPoolSize() {
        return super.getPoolSize();
    }

    @Override
    @ManagedAttribute
    public int getLargestPoolSize() {
        return super.getLargestPoolSize();
    }

    @Override
    @ManagedAttribute
    public int getMaximumPoolSize() {
        return super.getMaximumPoolSize();
    }

    @Override
    @ManagedAttribute
    public int getCorePoolSize() {
        return super.getCorePoolSize();
    }

    @Override
    @ManagedAttribute
    public boolean isTerminating() {
        return super.isTerminating();
    }

    @Override
    @ManagedAttribute
    public boolean isShutdown() {
        return super.isShutdown();
    }

    @ManagedAttribute
    public int getQueueSize() {
        return getQueue().size();
    }
}
