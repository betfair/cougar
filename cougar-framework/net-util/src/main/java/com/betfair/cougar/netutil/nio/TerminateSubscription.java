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

import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;

import java.util.Set;

/**
 */
public class TerminateSubscription extends AbstractHeapTranscribable {
    private long heapId;
    private String subscriptionId;
    private String closeReason;

    // used in transcription, change the ordering at your peril!
    // add new fields at the end!
    private Parameter[] parameters =  new Parameter[] {
        new Parameter("heapId", ParameterType.create(long.class), true),
        new Parameter("subscriptionId", ParameterType.create(String.class), true),
        new Parameter("closeReason", ParameterType.create(String.class), true)
    };

    public TerminateSubscription() {
    }

    public TerminateSubscription(long heapId, String subscriptionId, String closeReason) {
        this.heapId = heapId;
        this.subscriptionId = subscriptionId;
        this.closeReason = closeReason;
    }

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(heapId, parameters[0], client);
        out.writeObject(subscriptionId, parameters[1], client);
        out.writeObject(closeReason, parameters[2], client);
        // NOTE: add new fields at the end
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        heapId = (Long) in.readObject(parameters[0], client);
        subscriptionId = (String) in.readObject(parameters[1], client);
        closeReason = (String) in.readObject(parameters[2], client);
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

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TerminateSubscription that = (TerminateSubscription) o;

        if (heapId != that.heapId) return false;
        if (closeReason != null ? !closeReason.equals(that.closeReason) : that.closeReason != null) return false;
        if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (heapId ^ (heapId >>> 32));
        result = 31 * result + (subscriptionId != null ? subscriptionId.hashCode() : 0);
        result = 31 * result + (closeReason != null ? closeReason.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TerminateSubscription{" +
                "heapId=" + heapId +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", closeReason='" + closeReason + '\'' +
                '}';
    }
}
