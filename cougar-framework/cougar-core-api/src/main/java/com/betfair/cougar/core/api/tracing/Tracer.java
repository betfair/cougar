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

package com.betfair.cougar.core.api.tracing;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.OperationKey;

/**
 * SPI for providing tracing implementations.
 */
public interface Tracer {

    void start(RequestUUID uuid, OperationKey operationKey);

    void trace(RequestUUID uuid, String msg);
    void trace(RequestUUID uuid, String msg, Object arg1);
    void trace(RequestUUID uuid, String msg, Object arg1, Object arg2);
    void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3);
    void trace(RequestUUID uuid, String msg, Object... args);

    void trace(ExecutionContext ctx, String msg);
    void trace(ExecutionContext ctx, String msg, Object arg1);
    void trace(ExecutionContext ctx, String msg, Object arg1, Object arg2);
    void trace(ExecutionContext ctx, String msg, Object arg1, Object arg2, Object arg3);
    void trace(ExecutionContext ctx, String msg, Object... args);

    /**
     * Called when a request (or call within a batch request) is completed. Called at most one time (certain error conditions may result in this never being called, although in
     * reality this would represent a bug in Cougar.
     */
    void end(RequestUUID uuid);

    void startCall(RequestUUID uuid, RequestUUID subUuid, OperationKey operationKey);

    void endCall(RequestUUID uuid, RequestUUID subUuid, OperationKey operationKey);
}
