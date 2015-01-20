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

package com.betfair.cougar.core.impl.tracing;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.tracing.Tracer;

import java.util.ArrayList;
import java.util.List;

/**
 * Compound tracer, enables running with 0 or more tracers.
 */
public class CompoundTracer extends AbstractTracer {
    private List<Tracer> tracers = new ArrayList<>();

    @Override
    public void start(RequestUUID uuid, OperationKey operation) {
        for (Tracer t : tracers) {
            t.start(uuid, operation);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, arg1);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, arg1, arg2);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, arg1, arg2, arg3);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        for (Tracer t : tracers) {
            t.trace(uuid, msg, args);
        }
    }

    @Override
    public void end(RequestUUID uuid) {
        for (Tracer t : tracers) {
            t.end(uuid);
        }
    }

    @Override
    public void startCall(RequestUUID uuid, RequestUUID subUuid, OperationKey key) {
        for (Tracer t : tracers) {
            t.startCall(uuid, subUuid, key);
        }
    }

    @Override
    public void endCall(RequestUUID uuid, RequestUUID subUuid, OperationKey key) {
        for (Tracer t : tracers) {
            t.endCall(uuid, subUuid, key);
        }
    }

    public void addTracer(Tracer impl) {
        tracers.add(impl);
    }
}
