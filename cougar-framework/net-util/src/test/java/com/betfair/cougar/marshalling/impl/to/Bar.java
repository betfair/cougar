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

import java.util.List;
import java.util.Set;


public class  Bar implements Result, Transcribable {
    private BarDelegate delegate;
    public Bar (BarDelegate delegate ) {
        this();
        this.delegate = delegate;
    }


    private Double barDouble;

    public final Double getBarDouble()  {
        if (delegate != null) {
            return delegate.getBarDouble();
        }
        else {
            return barDouble;
        }
    }

    public final void setBarDouble(Double barDouble)  {
        if (delegate != null) {
            delegate.setBarDouble(barDouble);
        }
        else {
            this.barDouble=barDouble;
        }
    }


    private Baz[] bazArray = null;
    public final Baz[] getBazArray() {
    	if (delegate != null) {
    		return delegate.getBazArray();
    	}
    	else {
    		return bazArray;
    	}
    }

    public final void setBazArray(Baz[] bazArray) {
    	if (delegate != null) {
    		delegate.setBazArray(bazArray);
    	}
    	else {
    		this.bazArray = bazArray;
    	}
    }

    private List<Baz> bazList = null;

    public final List<Baz> getBazList()  {
        if (delegate != null) {
            return delegate.getBazList();
        }
        else {
            return bazList;
        }
    }

    public final void setBazList(List<Baz> bazs)  {
        if (delegate != null) {
            delegate.setBazList(bazs);
        }
        else {
            this.bazList=bazs;
        }
    }

    private Set<Baz> bazSet = null;

    public final Set<Baz> getBazSet()  {
        if (delegate != null) {
            return delegate.getBazSet();
        }
        else {
            return bazSet;
        }
    }

    public final void setBazSet(Set<Baz> bazSet)  {
        if (delegate != null) {
            delegate.setBazSet(bazSet);
        }
        else {
            this.bazSet=bazSet;
        }
    }



    public Bar () {}



	private static final Parameter __barDoubleParam = new Parameter("barDouble",new ParameterType(Double.class, null ),true);
	private static final Parameter __bazListParam = new Parameter("bazList",new ParameterType(List.class, new ParameterType[] { new ParameterType(Baz.class,null) }),true);
	private static final Parameter __bazSetParam = new Parameter("bazSet",new ParameterType(Set.class, new ParameterType[] { new ParameterType(Baz.class,null) }),true);
	private static final Parameter __bazArrayParam = new Parameter("bazArray",new ParameterType(Baz[].class, new ParameterType[] { new ParameterType(Baz.class,null) }),true);


    public static final Parameter[] PARAMETERS = new Parameter[] { __barDoubleParam , __bazListParam, __bazSetParam, __bazArrayParam};

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

	public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
	    out.writeObject(getBarDouble(), __barDoubleParam, client);
	    out.writeObject(getBazList(), __bazListParam, client);
	    out.writeObject(getBazSet(), __bazSetParam, client);
	    out.writeObject(getBazArray(),__bazArrayParam, client);

	}

	public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
	    setBarDouble((Double)in.readObject(__barDoubleParam, client));
	    setBazList((List<Baz>) in.readObject(__bazListParam, client));
	    setBazSet((Set<Baz>) in.readObject(__bazSetParam, client));
	    setBazArray((Baz[]) in.readObject(__bazArrayParam, client));
	}

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Bar)) {
            return false;
        }

        if (this == o) {
            return true;
        }
        Bar another = (Bar)o;

        return new EqualsBuilder()
            .append(barDouble, another.barDouble)
            .append(bazList, another.bazList)
            .append(bazSet, another.bazSet)
            .append(bazArray, another.bazArray)
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(barDouble)
            .append(bazList)
            .append(bazSet)
            .append(bazArray)
            .toHashCode();
    }
}


