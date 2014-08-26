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

package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.marshalling.api.databinding.*;

public class XMLDataBindingFactory implements DataBindingFactory{

	private final XMLMarshaller MARSHALLER;
    private final XMLUnMarshaller UNMARSHALLER;

    public XMLDataBindingFactory(SchemaValidationFailureParser schemaValidationFailureParser) {
        MARSHALLER = new XMLMarshaller();
        UNMARSHALLER = new XMLUnMarshaller(schemaValidationFailureParser);
    }

    @Override
	public Marshaller getMarshaller() {
		return MARSHALLER;
	}

    @Override
	public FaultMarshaller getFaultMarshaller() {
		return MARSHALLER;
	}

    @Override
    public FaultUnMarshaller getFaultUnMarshaller() {
        //At the time this was written, we had no need for XML fault unmarshalling
        //JSON supersedes this format in every way...
        return UNMARSHALLER;
    }

    @Override
	public UnMarshaller getUnMarshaller() {
		return UNMARSHALLER;
	}

}
