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

package com.betfair.cougar.marshalling.impl;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class SimpleApplicationException extends CougarApplicationException implements Transcribable {

    private static final Parameter __responseCodeParameter = new Parameter("responseCode",new ParameterType(ResponseCode.class, null ),false);
	private static final Parameter __stackSizeParameter = new Parameter("stackSize",new ParameterType(Integer.class, null ),false);
	private static final Parameter __stackClassNameParameter = new Parameter("stackClass",new ParameterType(String.class, null ),false);
	private static final Parameter __stackMethodNameParameter = new Parameter("stackMethod",new ParameterType(String.class, null ),false);
	private static final Parameter __stackFileNameParameter = new Parameter("stackFile",new ParameterType(String.class, null ),false);
	private static final Parameter __stackLineNumberParameter = new Parameter("stackLineNo",new ParameterType(Integer.class, null ),false);

    private static final Parameter __messageParameter = new Parameter("message",new ParameterType(String.class, null ),false);

    private static final Parameter[] PARAMETERS = new Parameter[] {
        __responseCodeParameter, __messageParameter,  __stackSizeParameter, __stackClassNameParameter,
        __stackMethodNameParameter, __stackFileNameParameter,__stackLineNumberParameter };

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public SimpleApplicationException(ResponseCode responseCode , String message) {
    	super(responseCode,  message);
    }

    /**
     * Constructor for reading the Exception from a TranscriptionInput source
     * @param in the TranscriptionInput to read the exception data from
     */
    public SimpleApplicationException(TranscriptionInput in, Set<TranscribableParams> transcriptionParams) throws Exception {
    	this((ResponseCode)in.readObject(__responseCodeParameter, true), ((String)in.readObject(__messageParameter, true)));
        transcribeStackTrace(in);
    }

    @Override
	public List<String[]> getApplicationFaultMessages() {
		List<String[]> appFaults = new ArrayList<String[]>();
		 appFaults.add(new String[] {"responseCode", getResponseCode().toString()}); appFaults.add(new String[] {"message", String.valueOf(getMessage())});
		return appFaults;
	}

    @Override
	public String getApplicationFaultNamespace() {
		return "http://www.betfair.com/unittesting/";
	}

	public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
		out.writeObject(getResponseCode(), __responseCodeParameter, client);
	    out.writeObject(getMessage(), __messageParameter, client);
	    transcribeStackTrace(out);
	}

	public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
		//Empty - transcription is done in the constructor
	}

    @Override
    public ServiceVersion getServiceVersion() {
        return new ServiceVersion(1,0);
    }

	private void transcribeStackTrace(TranscriptionOutput out) throws Exception {
		StackTraceElement[] stackTrace = getStackTrace();
		if (stackTrace != null) {
			out.writeObject(stackTrace.length, __stackSizeParameter, false);
			for (StackTraceElement element : stackTrace) {
				out.writeObject(element.getClassName(), __stackClassNameParameter, false);
				out.writeObject(element.getMethodName(), __stackMethodNameParameter, false);
				out.writeObject(element.getFileName(), __stackFileNameParameter, false);
				out.writeObject(element.getLineNumber(), __stackLineNumberParameter, false);
			}
		} else out.writeObject(null, __stackSizeParameter, false);
	}

	private void transcribeStackTrace(TranscriptionInput in) throws Exception {
		Integer size = in.readObject(__stackSizeParameter, true);
		if (size != null) {
			StackTraceElement[] stackTrace = new StackTraceElement[size];
			for (int i = 0; i < stackTrace.length; i++) {
				stackTrace[i] = new StackTraceElement(
					(String)in.readObject( __stackClassNameParameter, true),
					(String)in.readObject( __stackMethodNameParameter, true),
					(String)in.readObject( __stackFileNameParameter, true),
					(Integer)in.readObject(__stackLineNumberParameter, true));
			}
			setStackTrace(stackTrace);
		}
	}
}
