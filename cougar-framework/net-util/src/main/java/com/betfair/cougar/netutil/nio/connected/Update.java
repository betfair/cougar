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
import com.betfair.cougar.netutil.nio.AbstractHeapTranscribable;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.updates.UpdateBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Update extends AbstractHeapTranscribable {

    private List<UpdateAction> actions;

    // used in transcription, change the ordering at your peril!
    // add new fields at the end!
    private static Parameter[] parameters =  new Parameter[] {
        new Parameter("actions", ParameterType.create(ArrayList.class, UpdateAction.class), true)
    };

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(actions, parameters[0], client);
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        actions = (List<UpdateAction>) in.readObject(parameters[0], client);
        // NOTE: add new fields at the end
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    public void apply(HeapListener listener) {
        List<com.betfair.platform.virtualheap.updates.Update> updates = new ArrayList<com.betfair.platform.virtualheap.updates.Update>();
        for (UpdateAction ua : actions) {
            updates.add(ua.getHeapRepresentation());
        }
        listener.applyUpdate(new UpdateBlock(updates));
    }

    public void setActions(List<UpdateAction> actions) {
        this.actions = actions;
    }

    public List<UpdateAction> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return "Update{" +
                "actions=" + actions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Update update = (Update) o;

        if (!actions.equals(update.actions)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return actions.hashCode();
    }
}