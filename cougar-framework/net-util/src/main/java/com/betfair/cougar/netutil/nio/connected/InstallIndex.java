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
import com.betfair.platform.virtualheap.NodeType;
import com.betfair.platform.virtualheap.updates.Update;

import java.util.Set;

/**
 *
 */
public class InstallIndex extends AbstractUpdateAction {

    private int parentId;
    private int id;
    private int index;
    private NodeType type;
    private transient com.betfair.platform.virtualheap.updates.InstallIndex heapRepresentation;

    // used in transcription, change the ordering at your peril!
    // add new fields at the end!
    private static Parameter[] parameters =  new Parameter[] {
        new Parameter("parentId", ParameterType.create(int.class), true),
        new Parameter("id", ParameterType.create(int.class), true),
        new Parameter("index", ParameterType.create(int.class), true),
        new Parameter("type", ParameterType.create(String.class), true)
    };

    // only used for transcription
    public InstallIndex() {
    }

    public InstallIndex(int parentId, int id, int index, NodeType type) {
        this.parentId = parentId;
        this.id = id;
        this.index = index;
        this.type = type;
    }

    public InstallIndex(com.betfair.platform.virtualheap.updates.InstallIndex u) {
        this(u.getParentId(), u.getId(), u.getIndex(), u.getType());
        heapRepresentation = u;
    }

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(parentId, parameters[0], client);
        out.writeObject(id, parameters[1], client);
        out.writeObject(index, parameters[2], client);
        out.writeObject(type.name(), parameters[3], client);
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        parentId = (Integer) in.readObject(parameters[0], client);
        id = (Integer) in.readObject(parameters[1], client);
        index = (Integer) in.readObject(parameters[2], client);
        type = NodeType.valueOf((String) in.readObject(parameters[3], client));
        // NOTE: add new fields at the end
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Update getHeapRepresentation() {
        if (heapRepresentation == null) {
            heapRepresentation = new com.betfair.platform.virtualheap.updates.InstallIndex(parentId, id, index, type);
        }
        return heapRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstallIndex that = (InstallIndex) o;

        if (id != that.id) return false;
        if (index != that.index) return false;
        if (parentId != that.parentId) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parentId;
        result = 31 * result + id;
        result = 31 * result + index;
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstallIndex{" +
                "parentId=" + parentId +
                ", id=" + id +
                ", index=" + index +
                ", type=" + type +
                '}';
    }
}
