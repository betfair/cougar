/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.common;

import com.betfair.cougar.api.Result;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;

import java.util.Set;

/**
*
*/
public class EchoResponse implements Transcribable, Result {
    private String message;
    private ExecutionContextTO executionContext;

    private static final Parameter __messageParam = new Parameter("message",new ParameterType(String.class, null ),true);
    private static final Parameter __executionContextParam = new Parameter("executionContext",new ParameterType(ExecutionContextTO.class, null ),true);

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExecutionContextTO getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContextTO executionContext) {
        this.executionContext = executionContext;
    }

    public static final Parameter[] PARAMETERS = new Parameter[] { __messageParam, __executionContextParam };

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(getMessage(), __messageParam, client);
        out.writeObject(getExecutionContext(), __executionContextParam, client);
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        setMessage((String) in.readObject(__messageParam, client));
        setExecutionContext((ExecutionContextTO) in.readObject(__executionContextParam, client));
    }

    public static final ServiceVersion SERVICE_VERSION = Common.SERVICE_VERSION;

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }
}
