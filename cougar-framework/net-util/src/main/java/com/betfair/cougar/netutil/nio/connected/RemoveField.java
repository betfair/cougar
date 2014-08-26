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
public class RemoveField extends AbstractUpdateAction {

    private int parentId;
    private int id;
    private String name;
    private transient Set<Integer> deallocatedIds;
    private transient com.betfair.platform.virtualheap.updates.RemoveField heapRepresentation;

    // used in transcription, change the ordering at your peril!
    // add new fields at the end!
    private static Parameter[] parameters =  new Parameter[] {
        new Parameter("parentId", ParameterType.create(int.class), true),
        new Parameter("id", ParameterType.create(int.class), true),
        new Parameter("name", ParameterType.create(String.class), true)
    };

    // only used for transcription
    public RemoveField() {
    }

    public RemoveField(int parentId, int id, String name, Set<Integer> deallocatedIds) {
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.deallocatedIds = deallocatedIds;
    }

    public RemoveField(com.betfair.platform.virtualheap.updates.RemoveField u) {
        this(u.getParentId(), u.getId(), u.getName(), u.getDeallocatedIds());
        heapRepresentation = u;
    }

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(parentId, parameters[0], client);
        out.writeObject(id, parameters[1], client);
        out.writeObject(name, parameters[2], client);
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        parentId = (Integer) in.readObject(parameters[0], client);
        id = (Integer) in.readObject(parameters[1], client);
        name = (String) in.readObject(parameters[2], client);
        // NOTE: add new fields at the end
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public com.betfair.platform.virtualheap.updates.Update getHeapRepresentation() {
        if (heapRepresentation == null) {
            heapRepresentation = new com.betfair.platform.virtualheap.updates.RemoveField(parentId, id, name, deallocatedIds);
        }
        return heapRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoveField)) return false;

        RemoveField that = (RemoveField) o;

        if (id != that.id) return false;
        if (parentId != that.parentId) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parentId;
        result = 31 * result + id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RemoveField{" +
                "parentId=" + parentId +
                ", id=" + id +
                ", name=" + name +
                ", deallocatedIds=" + deallocatedIds +
                '}';
    }
}
