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

import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Serializer;

/**
 * A serialiser for serialising transcribable classes that are internal to cougar (specifically {@link FaultDetail})
 * </p>
 * This class doesn't conform to the contraints required by the {@link TranscribableSerialiser}.  As a consequence more control over the serialisation is
 * handed to these two classes.  The downside is that any changes to the transcription of these classes will break clients
 */
public class FaultDetailSerialiser implements Serializer{

    private Set<TranscribableParams> transcriptionParams;

    public FaultDetailSerialiser(Set<TranscribableParams> transcriptionParams) {
        this.transcriptionParams = transcriptionParams;
    }

    @Override
	public void writeObject(Object obj, final AbstractHessianOutput out) throws IOException {

		if (out.addRef(obj)) {
			return;
		}


        FaultDetail transcribableException = (FaultDetail) obj;
		TranscriptionOutput to = new TranscriptionOutput() {
			@Override
			public void writeObject(Object obj, Parameter param, boolean client) throws Exception {
				out.writeObject(obj);

		}};

		int ref = out.writeObjectBegin(obj.getClass().getName());

		try {
			if (ref >= 0) {
				transcribe(to, transcribableException);
			}
			else {
				out.writeInt(0);
				out.writeObjectBegin(obj.getClass().getName());
                transcribe(to, transcribableException);
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}


	}

    private void transcribe(TranscriptionOutput out, FaultDetail detail) throws Exception {
        out.writeObject(detail.getDetailMessage(), FaultDetail.detailMessageParam, false);
        if (detail.getCause() != null && detail.getCause() instanceof Transcribable) {
            Transcribable tCause = (Transcribable) detail.getCause();
            out.writeObject(ClassnameCompatibilityMapper.toMajorMinorPackaging(tCause.getClass(), tCause.getServiceVersion()), FaultDetail.faultClassNameParam, false);
            ParameterType type = new ParameterType(detail.getCause().getClass(), null);
            out.writeObject(detail.getCause(), new Parameter("exception", type, false), false);
        } else {
            out.writeObject(null, FaultDetail.faultClassNameParam, false);
        }
    }

}
