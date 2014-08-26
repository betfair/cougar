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

package com.betfair.cougar.marshalling.impl.to;

import com.betfair.cougar.api.Result;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;


public class  Cycle2 implements Result, Transcribable {
    private FooDelegate delegate;
    public Cycle2 ( ) {
    }


    private Cycle1 cycle1;
    public final Cycle1 getCycle1()  {
    	return cycle1;
    }

    public final void setCycle1(Cycle1 cycle1)  {
    	this.cycle1=cycle1;
    }





	private static final Parameter __cycle1Param = new Parameter("cycle1",new ParameterType(Cycle1.class, null ),true);

    public static final Parameter[] PARAMETERS = new Parameter[] { __cycle1Param};

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

	public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
	    out.writeObject(getCycle1(), __cycle1Param, client);
	}

	public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
	    setCycle1((Cycle1)in.readObject(__cycle1Param, client));
	}

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Cycle2)) {
            return false;
        }

        if (this == o) {
            return true;
        }
        Cycle2 another = (Cycle2)o;

        return new EqualsBuilder()
            .append(cycle1, another.cycle1)
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(cycle1)
            .toHashCode();
    }
}


