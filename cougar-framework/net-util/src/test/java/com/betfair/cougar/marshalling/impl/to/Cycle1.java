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


public class  Cycle1 implements Result, Transcribable {
    private FooDelegate delegate;
    public Cycle1 ( ) {
    }


    private Cycle2 cycle2;
    public final Cycle2 getCycle2()  {
    	return cycle2;
    }

    public final void setCycle2(Cycle2 cycle2)  {
    	this.cycle2=cycle2;
    }





	private static final Parameter __cycle2Param = new Parameter("cycle2",new ParameterType(Cycle2.class, null ),true);

    public static final Parameter[] PARAMETERS = new Parameter[] { __cycle2Param};

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

	public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
	    out.writeObject(getCycle2(), __cycle2Param, client);
	}

	public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
	    setCycle2((Cycle2)in.readObject(__cycle2Param, client));
	}

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Cycle1)) {
            return false;
        }

        if (this == o) {
            return true;
        }
        Cycle1 another = (Cycle1)o;

        return new EqualsBuilder()
            .append(cycle2, another.cycle2)
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(cycle2)
            .toHashCode();
    }
}


