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

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;

import java.util.Set;

/**
*
*/
public class IdentityTO implements Transcribable {
    private String principal;
    private String credential;

    private static final Parameter __principalParam = new Parameter("principal",new ParameterType(String.class, null ),true);
    private static final Parameter __credentialParam = new Parameter("credential",new ParameterType(String.class, null ),true);

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public static final Parameter[] PARAMETERS = new Parameter[] { __principalParam, __credentialParam };

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(getPrincipal(), __principalParam, client);
        out.writeObject(getCredential(), __credentialParam, client);
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        setPrincipal((String) in.readObject(__principalParam, client));
        setCredential((String) in.readObject(__credentialParam, client));
    }

    public static final ServiceVersion SERVICE_VERSION = Common.SERVICE_VERSION;

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }

    @Override
    public String toString() {
        return "IdentityTO{" +
                "principal='" + principal + '\'' +
                ", credential='" + credential + '\'' +
                '}';
    }
}
