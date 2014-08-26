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
import java.util.Set;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Serializer;

/**
 * serialiser that deals with most transcribable objects.  in order for this to work the class must have a no parameter constructor, declare all the fields
 * it will write via the getParameters method, and write <b>only</b> those fields in the order declared
 * </p>
 * data type classes generated from idd conform to the above restrictions
 */
public class TranscribableSerialiser implements Serializer {

    private Set<TranscribableParams> transcriptionParams;
    private boolean client;

    public TranscribableSerialiser(Set<TranscribableParams> transcriptionParams, boolean client) {
        this.transcriptionParams = transcriptionParams;
        this.client = client;
    }

    @Override
	public void writeObject(Object obj, final AbstractHessianOutput out) throws IOException {
		try {
			if (out.addRef(obj)) {
				return;
			}

            Transcribable transcribable = (Transcribable) obj;
            ServiceVersion serviceVersion = transcribable.getServiceVersion();
			Class clazz = obj.getClass();

            String classNameToWrite = clazz.getName();
            // if we're talking to an older version we need to migrate vMajor to vMajor_Minor
            if (!transcriptionParams.contains(TranscribableParams.MajorOnlyPackageNaming)) {
                // look for vMajor
                classNameToWrite = ClassnameCompatibilityMapper.toMajorMinorPackaging(clazz, serviceVersion);
            }
			int ref = out.writeObjectBegin(classNameToWrite);

            // true if the object has already been defined
			if (ref >= 0) {
				transcribe(out,transcribable, client);
			}
			else  {
				Parameter[] parameters =  transcribable.getParameters();
				out.writeInt(parameters.length);
				for (int i=0; i < parameters.length; i++) {
					out.writeString(parameters[i].getName());
				}
				out.writeObjectBegin(classNameToWrite);
				transcribe(out,transcribable, client);
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}


	}

	private void transcribe(final AbstractHessianOutput out, Transcribable transcribable, boolean client) throws IOException {
		try {
			transcribable.transcribe(new TranscriptionOutput() {
				@Override
				public void writeObject(Object obj, Parameter param, boolean client) throws Exception {
					out.writeObject(obj);
				}}, transcriptionParams, client);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}


}
