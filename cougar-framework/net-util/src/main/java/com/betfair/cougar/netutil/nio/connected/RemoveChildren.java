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
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;

import java.util.Set;

/**
 *
 */
public class RemoveChildren extends AbstractUpdateAction {

    private int id;
    private transient Set<Integer> deallocatedIds;
    private transient com.betfair.platform.virtualheap.updates.RemoveChildren heapRepresentation;

    // used in transcription, change the ordering at your peril!
    // add new fields at the end!
    private static Parameter[] parameters =  new Parameter[] {
        new Parameter("id", ParameterType.create(int.class), true)
    };

    // only used for transcription
    public RemoveChildren() {
    }

    public RemoveChildren(int id, Set<Integer> deallocatedIds) {
        this.id = id;
        this.deallocatedIds = deallocatedIds;
    }

    public RemoveChildren(com.betfair.platform.virtualheap.updates.RemoveChildren u) {
        this(u.getId(), u.getDeallocatedIds());
        this.heapRepresentation = u;
    }

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(id, parameters[0], client);
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        id = (Integer) in.readObject(parameters[0], client);
        // NOTE: add new fields at the end
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public com.betfair.platform.virtualheap.updates.Update getHeapRepresentation() {
        if (heapRepresentation == null) {
            heapRepresentation = new com.betfair.platform.virtualheap.updates.RemoveChildren(id, deallocatedIds);
        }
        return heapRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoveChildren that = (RemoveChildren) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "RemoveChildren{" +
                "id=" + id +
                '}';
    }
}
