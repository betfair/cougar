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
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

public class FaultDetailDeserialiser extends AbstractDeserializer {

	private Class<FaultDetail> cls;
    private Set<TranscribableParams> transcriptionParams;

    public FaultDetailDeserialiser(Class<FaultDetail> cls, Set<TranscribableParams> transcriptionParams) {
        this.cls = cls;
        this.transcriptionParams = transcriptionParams;
    }

    @Override
	public Object readObject(final AbstractHessianInput in, Object[] fields) throws IOException {

		try {

			TranscriptionInput ti = new TranscriptionInput() {

				@Override
				public <T> T readObject(Parameter param, boolean client) throws Exception {
					return (T) in.readObject();
				}
			};

			int ref = in.addRef(null);

            FaultDetail o = transcribe(ti);
            in.setRef(ref, o);

			return o;
		}
		catch (Exception e) {
			throw new IOException(e);
		}

	}

    private FaultDetail transcribe(TranscriptionInput in) throws Exception {

        String detailMessage = in.readObject(FaultDetail.detailMessageParam, true);
        String className = (String) in.readObject(FaultDetail.faultClassNameParam, true);
        Throwable exception = null;
        if (className != null) {
            ParameterType type = new ParameterType(Class.forName(ClassnameCompatibilityMapper.toMajorOnlyPackaging(className)), null);
            exception = (Throwable)in.readObject(new Parameter("exception", type, false), true);
        }
        return new FaultDetail(detailMessage, exception);
    }

}
