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

package com.betfair.cougar.netutil.nio.connected;

import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import com.betfair.platform.virtualheap.updates.Update;

import java.util.Set;

public class TerminateHeap extends AbstractUpdateAction {

    public static TerminateHeap INSTANCE = new TerminateHeap();
    private static Parameter[] parameters = new Parameter[0];

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        // NOTE: add new fields at the end
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Update getHeapRepresentation() {
        return new com.betfair.platform.virtualheap.updates.TerminateHeap();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass());
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "TerminateHeap{}";
    }
}