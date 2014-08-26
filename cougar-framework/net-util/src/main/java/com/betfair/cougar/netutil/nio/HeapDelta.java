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

package com.betfair.cougar.netutil.nio;

import com.betfair.cougar.core.api.transcription.*;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.cougar.netutil.nio.connected.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class HeapDelta extends AbstractHeapTranscribable {

    private long heapId;
    private long updateId;
    private List<Update> updates = new ArrayList<Update>();

    // used in transcription, change the ordering at your peril!
    // add new fields at the end!
    private Parameter[] parameters =  new Parameter[] {
        new Parameter("heapId", ParameterType.create(long.class), true),
        new Parameter("updateId", ParameterType.create(long.class), true),
        new Parameter("updates", ParameterType.create(ArrayList.class, Update.class), true)
    };

    public HeapDelta() {
    }

    public HeapDelta(long heapId, long updateId, List<Update> updates) {
        this.heapId = heapId;
        this.updateId = updateId;
        this.updates = updates;
    }

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(heapId, parameters[0], client);
        out.writeObject(updateId, parameters[1], client);
        out.writeObject(updates, parameters[2], client);
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        heapId = (Long) in.readObject(parameters[0], client);
        updateId = (Long) in.readObject(parameters[1], client);
        updates = (List<Update>) in.readObject(parameters[2], client);
        // NOTE: add new fields at the end
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    public long getHeapId() {
        return heapId;
    }

    public void setHeapId(long heapId) {
        this.heapId = heapId;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }

    public void applyTo(HeapListener listener) {
        for (Update u : updates) {
            u.apply(listener);
        }
    }

    public void add(Update u) {
        updates.add(u);
    }

    public void setUpdateId(long updateId) {
        this.updateId = updateId;
    }

    public long getUpdateId() {
        return updateId;
    }

    public boolean containsFirstUpdate() {
        // initial update must be the first..
        if (!updates.isEmpty()) {
            if (updates.get(0) instanceof InitialUpdate) {
                return true;
            }
        }
        return false;
    }

    public boolean containsHeapTermination() {
        if (!updates.isEmpty()) {
            Update last = updates.get(updates.size()-1);
            return  (last.getActions().contains(TerminateHeap.INSTANCE));
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeapDelta heapDelta = (HeapDelta) o;

        if (heapId != heapDelta.heapId) return false;
        if (updateId != heapDelta.updateId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (heapId ^ (heapId >>> 32));
        result = 31 * result + (int) (updateId ^ (updateId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "HeapDelta{" +
                "heapId=" + heapId +
                ", updateId=" + updateId +
                ", updates=" + updates +
                '}';
    }
}
