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

package com.betfair.cougar.transport.jms.monitoring;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import com.betfair.cougar.transport.api.protocol.events.AbstractEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PingEvent extends AbstractEvent {

    private long emissionTime;

    public long getEmissionTime() {
        return emissionTime;
    }

    public void setEmissionTime(long emissionTime) {
        this.emissionTime = emissionTime;
    }

    private static final Parameter __emissionTimeParam = new Parameter("emissionTime",new ParameterType(Long.class, null ),true);

    @XmlTransient
    @JsonIgnore
    public static final Parameter[] PARAMETERS = new Parameter[] { __emissionTimeParam };

    @XmlTransient
    @JsonIgnore
    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(getEmissionTime(), __emissionTimeParam, client);
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        setEmissionTime((Long) in.readObject(__emissionTimeParam, client));
    }

    @XmlTransient
    @JsonIgnore
    public ServiceVersion getServiceVersion() {
        throw new UnsupportedOperationException();
    }
}
