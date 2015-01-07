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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;

import java.util.Arrays;

public class SimpleOperationDefinition implements OperationDefinition {
    private final OperationKey operationKey;
    private final Parameter[] parameters;
    private final ParameterType returnType;

    public SimpleOperationDefinition(final OperationKey operationKey, final Parameter[] parameters, final ParameterType returnType) {//NOSONAR
        if (parameters != null) {
            final Parameter[] p = parameters.clone();
            this.parameters = p;
        } else{
            this.parameters=null;
        }
        this.operationKey = operationKey;
        this.returnType = returnType;
    }

    public OperationKey getOperationKey() {
        return operationKey;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public ParameterType getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "SimpleOperationDefinition{" +
                "operationKey=" + operationKey +
                ", parameters=" + Arrays.toString(parameters) +
                ", returnType=" + returnType +
                '}';
    }
}
