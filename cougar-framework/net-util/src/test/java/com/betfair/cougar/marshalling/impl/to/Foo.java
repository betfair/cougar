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

import java.util.Map;
import java.util.Set;


public class  Foo implements Result, Transcribable {
    private FooDelegate delegate;
    public Foo (FooDelegate delegate ) {
        this();
        this.delegate = delegate;
    }

    private String fooName;

    public final String getFooName()  {
        if (delegate != null) {
            return delegate.getFooName();
        }
        else {
            return fooName;
        }
    }

    public final void setFooName(String foo)  {
        if (delegate != null) {
            delegate.setFooName(foo);
        }
        else {
            this.fooName=foo;
        }
    }




    private Bar bar;

    public final Bar getBar()  {
        if (delegate != null) {
            return delegate.getBar();
        }
        else {
            return bar;
        }
    }

    public final void setBar(Bar bar)  {
        if (delegate != null) {
            delegate.setBar(bar);
        }
        else {
            this.bar=bar;
        }
    }

    private Map<Bar, Baz> barBazMap;

    public final Map<Bar,Baz> getBarBazMap() {
    	if (delegate != null) {
    		return delegate.getBarBazMap() ;
    	}
    	else {
    		return barBazMap;
    	}
    }

    public final void setBarBazMap(Map<Bar,Baz> barBazMap) {
    	if (delegate != null) {
    		delegate.setBarBazMap(barBazMap);
    	}
    	else {
    		this.barBazMap = barBazMap;
    	}
    }

    private byte[] primitiveArray;
    public final byte[] getPrimitiveArray() {
    	if (delegate != null) {
    		return delegate.getPrimitiveArray();
    	}
    	else {
    		return primitiveArray;
    	}
    }

    public final void setPrimitiveArray(byte[] bytes) {
    	if (delegate != null) {
    		delegate.setPrimitiveArray(bytes);
    	}
    	else {
    		primitiveArray = bytes;
    	}
    }


    public Foo () {}



	private static final Parameter __fooParam = new Parameter("fooName",new ParameterType(String.class, null ),true);

	private static final Parameter __barParam = new Parameter("bar",new ParameterType(Bar.class, null ),false);

	private static final Parameter __barBazMapParam = new Parameter("barBazMap",new ParameterType(Map.class, new ParameterType[] {new ParameterType(Bar.class,null), new ParameterType(Baz.class,null)} ),false);

	private static final Parameter __primitiveArray = new Parameter("primitiveArray", new ParameterType(byte[].class, new ParameterType[] {new ParameterType(byte.class,null)}),false);

    public static final Parameter[] PARAMETERS = new Parameter[] { __fooParam,  __barParam, __barBazMapParam, __primitiveArray};

    public Parameter[] getParameters() {
        return PARAMETERS;
    }


	public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
	    out.writeObject(getFooName(), __fooParam, client);
	    out.writeObject(getBar(), __barParam, client);
	    out.writeObject(getBarBazMap(), __barBazMapParam, client);
	    out.writeObject(getPrimitiveArray(), __primitiveArray, client);
	}

	public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
	    setFooName((String)in.readObject(__fooParam, client));
	    setBar((Bar)in.readObject(__barParam, client));
	    setBarBazMap((Map<Bar, Baz>) in.readObject(__barBazMapParam, client));
	    setPrimitiveArray((byte[]) in.readObject(__primitiveArray, client));
	}

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Foo)) {
            return false;
        }

        if (this == o) {
            return true;
        }
        Foo another = (Foo)o;

        return new EqualsBuilder()
            .append(fooName, another.fooName)
            .append(bar, another.bar)
            .append(barBazMap, another.barBazMap)
            .append(primitiveArray, another.primitiveArray)
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(fooName)
            .append(bar)
            .append(barBazMap)
            .append(primitiveArray)
            .toHashCode();
    }




}


