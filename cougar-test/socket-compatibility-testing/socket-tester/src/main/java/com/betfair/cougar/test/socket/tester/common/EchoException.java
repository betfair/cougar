/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.common;

/**
 *
 */

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * This exception is thrown when an operation fails
 */
@SuppressWarnings("all")
public class EchoException extends CougarApplicationException implements Transcribable {
    private static final String prefix = "EEX-";

    private static final Parameter __responseCodeParameter = new Parameter("responseCode",new ParameterType(ResponseCode.class, null ),false);
    private static final Parameter __stackSizeParameter = new Parameter("stackSize",new ParameterType(Integer.class, null ),false);
    private static final Parameter __stackClassNameParameter = new Parameter("stackClass",new ParameterType(String.class, null ),false);
    private static final Parameter __stackMethodNameParameter = new Parameter("stackMethod",new ParameterType(String.class, null ),false);
    private static final Parameter __stackFileNameParameter = new Parameter("stackFile",new ParameterType(String.class, null ),false);
    private static final Parameter __stackLineNumberParameter = new Parameter("stackLineNo",new ParameterType(Integer.class, null ),false);



    private static final Parameter __errorCodeParameter = new Parameter("errorCode",new ParameterType(EchoExceptionErrorCodeEnum.class, null ),false);
    private static final Parameter __infoParameter = new Parameter("info",new ParameterType(String.class, null ),true);
    /**
     * the unique code for this error
     */
    protected EchoExceptionErrorCodeEnum errorCode ;
    private String info;

    public final EchoExceptionErrorCodeEnum getErrorCode()  {
        return errorCode;
    }

    public final String getInfo() {
        return info;
    }

    private final void setErrorCode(EchoExceptionErrorCodeEnum errorCode)  {
        if (errorCode == EchoExceptionErrorCodeEnum.UNRECOGNIZED_VALUE) {
            throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
        }
        this.errorCode = errorCode;
        this.rawErrorCodeValue=errorCode != null ? errorCode.name() : null;
    }

    private final void setInfo(String info) {
        this.info = info;
    }

    private String rawErrorCodeValue;

    public final String getRawErrorCodeValue()  {
        return rawErrorCodeValue;
    }

    private final void setRawErrorCodeValue(String errorCode)  {
        EchoExceptionErrorCodeEnum enumValue = errorCode != null ? EnumUtils.readEnum(EchoExceptionErrorCodeEnum.class, errorCode) : null;
        this.errorCode=enumValue;
        this.rawErrorCodeValue=errorCode;
    }


    public EchoException(ResponseCode responseCode , EchoExceptionErrorCodeEnum errorCode, String info) {
        super(responseCode,  prefix + errorCode.getCode());
        setErrorCode(errorCode);
        setInfo(info);
    }

    private EchoException(ResponseCode responseCode , String errorCode, String info) {
        super(responseCode,  prefix + errorCode);
        setRawErrorCodeValue(errorCode);
        setInfo(info);
    }

    public EchoException(EchoExceptionErrorCodeEnum errorCode, String info){
        this(ResponseCode.BusinessException, errorCode, info);
    }

    /**
     * Constructor for reading the Exception from a TranscriptionInput source
     * @param in the TranscriptionInput to read the exception data from
     */
    public EchoException(TranscriptionInput in, Set<TranscribableParams> _transcriptionParams) throws Exception {
        this((ResponseCode)in.readObject(__responseCodeParameter, true)
                , readErrorCode(in, _transcriptionParams)
                , readInfo(in, _transcriptionParams)
        );
        transcribeStackTrace(in);
    }

    private static String readErrorCode(TranscriptionInput in, Set<TranscribableParams> _transcriptionParams) throws Exception {
        if (_transcriptionParams.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            return (String) in.readObject(__errorCodeParameter, true);
        }
        else {
            EchoExceptionErrorCodeEnum errorCode = (EchoExceptionErrorCodeEnum) in.readObject(__errorCodeParameter, true);
            return errorCode != null ? errorCode.name() : null;
        }
    }

    private static String readInfo(TranscriptionInput in, Set<TranscribableParams> _transcriptionParams) throws Exception {
        return (String) in.readObject(__infoParameter, true);
    }

    /**
     * Constructor with the cause of the exception (exception chaining)
     * @param  cause the cause
     * @see Throwable#getCause()
     */
    public EchoException(Throwable cause, ResponseCode responseCode , EchoExceptionErrorCodeEnum errorCode) {
        super(responseCode,  prefix + errorCode.getCode(), cause);
        setErrorCode(errorCode);
    }

    @Override
    public List<String[]> getApplicationFaultMessages() {
        List<String[]> appFaults = new ArrayList<String[]>();
        appFaults.add(new String[] {"errorCode", String.valueOf(errorCode)});
        return appFaults;
    }

    @Override
    public String getApplicationFaultNamespace() {
        return "http://www.betfair.com/servicetypes/v3/Health/";
    }

    @Override
    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> _transcriptionParams, boolean client) throws Exception {
        out.writeObject(getResponseCode(), __responseCodeParameter, client);
        if (_transcriptionParams.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            out.writeObject(getErrorCode() != null ? getErrorCode().name() : null, __errorCodeParameter, client);
        }
        else {
            out.writeObject(getErrorCode(), __errorCodeParameter, client);
        }
        out.writeObject(getInfo(), __infoParameter, client);
        transcribeStackTrace(out);
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
//Empty - transcription is done in the constructor
    }

    public static final ServiceVersion SERVICE_VERSION = new ServiceVersion("v3.0");

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
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

    public static final Parameter[] PARAMETERS = new Parameter[] { __responseCodeParameter , __errorCodeParameter , __infoParameter, __stackSizeParameter, __stackClassNameParameter, __stackMethodNameParameter, __stackFileNameParameter, __stackLineNumberParameter };

    public String getMessage() {
        return "responseCode="+getResponseCode()
                +", errorCode="+errorCode+" ("+prefix+errorCode.getCode()+")"
                ;
    }


}
