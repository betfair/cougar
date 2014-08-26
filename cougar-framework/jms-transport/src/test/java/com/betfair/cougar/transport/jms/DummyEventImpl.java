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

package com.betfair.cougar.transport.jms;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;
import com.betfair.cougar.transport.api.protocol.events.AbstractEvent;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * Noddy Event implementation for unit tests
 */
public class DummyEventImpl extends AbstractEvent implements Transcribable {
    public static final DummyEventImpl BOBS_ADDRESS = new DummyEventImpl("bob", "100 chancellors");

    public Object getValue(String name) {
        return null;
    }

    public DummyEventImpl(String name, String address) {
        this.name = name;
        this.address = address;
    }

    private String name;
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private static final Parameter _nameParam = new Parameter("name",new ParameterType(String.class, new ParameterType [] { new ParameterType(String.class, null ) } ),true);
    private static final Parameter _addressParam = new Parameter("address",new ParameterType(String.class, new ParameterType [] { new ParameterType(String.class, null ) } ),true);

    @XmlTransient
    public static final Parameter[] PARAMETERS = new Parameter[] { _nameParam, _addressParam };

    @Override
    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(getName(), _nameParam, client);
        out.writeObject(getAddress(), _addressParam, client);
    }

    @Override
    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
       setName((String)in.readObject(_nameParam, client));
        setAddress((String)in.readObject(_addressParam, client));
    }

    @Override
    public ServiceVersion getServiceVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter[] getParameters() {
        return PARAMETERS;
    }
}