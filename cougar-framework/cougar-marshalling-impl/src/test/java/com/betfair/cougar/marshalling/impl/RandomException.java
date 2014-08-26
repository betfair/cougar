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

package com.betfair.cougar.marshalling.impl;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Set;

/**
 * An exception for testing exception transcribing
 */
public class RandomException extends Throwable implements Transcribable {

    private final Parameter MESSAGE_PARAM = new Parameter("cause", new ParameterType(String.class, null), false);

    public Parameter[] getParameters() {
        return new Parameter[] { MESSAGE_PARAM };
    }

    private String message;

    public RandomException(TranscriptionInput in, Set<TranscribableParams> transcriptionParams) throws Exception {
        this.message = in.readObject(MESSAGE_PARAM, true);
    }

    public RandomException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(message, MESSAGE_PARAM, client);
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        this.message = in.readObject(MESSAGE_PARAM, client);
    }

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

    public boolean equals(Object o) {
        boolean equal = false;

        if (o instanceof RandomException) {
            return new EqualsBuilder().append(getMessage(), ((RandomException)o).getMessage()).isEquals();
        }
        return equal;

    }

}

