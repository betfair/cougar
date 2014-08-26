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

import javax.xml.bind.annotation.XmlTransient;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.betfair.cougar.api.Result;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;

import java.util.Set;


public class  Baz implements Result, Transcribable {
    private BazDelegate delegate;
    public Baz (BazDelegate delegate ) {
        this();
        this.delegate = delegate;
    }




    /**
     * The selection id
     */

    private Long bazId;

    public final Long getBazId()  {
        if (delegate != null) {
            return delegate.getBazId();
        }
        else {
            return bazId;
        }
    }

    public final void setBazId(Long bazId)  {
        if (delegate != null) {
            delegate.setBazId(bazId);
        }
        else {
            this.bazId=bazId;
        }
    }





    public String toString() {
    	return "{"+""+"bazId="+getBazId()+"}";
    }
    public Baz () {}



	private static final Parameter __bazIdParam = new Parameter("bazId",new ParameterType(Long.class, null ),true);

    @XmlTransient
    @JsonIgnore
    public static final Parameter[] PARAMETERS = new Parameter[] { __bazIdParam };

    @XmlTransient
    @JsonIgnore
    public Parameter[] getParameters() {
        return PARAMETERS;
    }

	public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
	    out.writeObject(getBazId(), __bazIdParam, client);
	}

	public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
	    setBazId((Long)in.readObject(__bazIdParam, client));
	}

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Baz)) {
            return false;
        }

        if (this == o) {
            return true;
        }
        Baz another = (Baz)o;

        return new EqualsBuilder()
            .append(bazId, another.bazId)
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(bazId)
            .toHashCode();
    }
}


