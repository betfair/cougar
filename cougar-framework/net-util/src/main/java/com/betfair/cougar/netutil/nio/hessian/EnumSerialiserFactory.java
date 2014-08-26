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

package com.betfair.cougar.netutil.nio.hessian;

import com.betfair.cougar.core.api.transcription.TranscribableEnum;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;

import java.util.Set;

/**
 */
public class EnumSerialiserFactory extends AbstractSerializerFactory {

    private Set<TranscribableParams> transcriptionParams;

    public EnumSerialiserFactory(Set<TranscribableParams> transcriptionParams) {
        this.transcriptionParams = transcriptionParams;
    }

    @Override
	public Deserializer getDeserializer(Class cls) throws HessianProtocolException {

		Deserializer deserializer = null;
        if (cls != null && TranscribableEnum.class.isAssignableFrom(cls)) {
            deserializer = new TranscribableEnumDeserializer(cls, transcriptionParams);
        }


		return deserializer;
	}

	@Override
	public Serializer getSerializer(Class cls) throws HessianProtocolException {

        if (cls != null && TranscribableEnum.class.isAssignableFrom(cls)) {
		    return new TranscribableEnumSerializer(cls, transcriptionParams);
        }
        return null;
	}

}
