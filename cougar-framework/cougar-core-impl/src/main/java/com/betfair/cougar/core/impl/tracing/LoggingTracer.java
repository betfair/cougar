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

package com.betfair.cougar.core.impl.tracing;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.logging.CougarLoggingUtils;

/**
 * Simple tracer implementation which writes trace messages to a trace log.
 */
public class LoggingTracer extends AbstractTracer {

    @Override
    public void start(RequestUUID uuid, OperationKey operation) {
        // no-op
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, arg1);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, arg1, arg2);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, arg1, arg2, arg3);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        CougarLoggingUtils.getTraceLogger().info(uuid.toString()+": "+ msg, args);
    }

    @Override
    public void end(RequestUUID uuid) {
        // no-op
    }
}
