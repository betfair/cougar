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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

/**
 * Deserialiser for classes serialised by {@link TranscribableSerialiser}
 */
public class TranscribableDeserialiser extends AbstractDeserializer {

	private Class<? extends Transcribable> cls;

    private Set<TranscribableParams> transcriptionParams;
    private boolean client;

    public TranscribableDeserialiser(Class<? extends Transcribable> cls, Set<TranscribableParams> transcriptionParams, boolean client) {
        this.cls = cls;
        this.transcriptionParams = transcriptionParams;
        this.client = client;
    }

    @Override
	public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {

		try {

			Transcribable o = cls.newInstance();
			in.addRef(o);

			Map<String,Class> paramClassMap = new HashMap<String, Class>();
			Parameter[] parameters = o.getParameters();
			for (Parameter param : parameters) {
				paramClassMap.put(param.getName(), param.getParameterType().getImplementationClass());
			}


			final Map<String, Object> fieldValues = new HashMap<String, Object>();
			for (int i = 0; i < fields.length; i++) {
				Class paramClass = paramClassMap.get(fields[i]);
				fieldValues.put((String)fields[i], in.readObject(paramClass));//important to use the class so that hessian creates arrays of correct type
			}

			TranscriptionInput ti = new TranscriptionInput() {
				@Override
				public <T> T readObject(Parameter param, boolean client) throws Exception {
					return (T) fieldValues.get(param.getName());
				}
			};

			o.transcribe(ti, transcriptionParams, client);

			return o;

		} catch (Exception e) {
			throw new IOException(e);
		}

	}
}
