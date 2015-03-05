/*
 * Copyright 2013, The Sporting Exchange Limited
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
<#include "common.ftl"><#t>
<#include "interfaceParser.ftl"><#t>
<#assign service = doc.@name><#t>
<#assign constructorArgs = "">
<#assign constructorArgsNoTypes = "">
<#assign constructorParams = "">
<#assign constructorArgsEnumsAsStrings = "">
<#assign constructorArgsNoTypesEnumsAsStrings = "">
<#assign constructorParamsEnumsAsStrings = "">
<#assign appFaultMessages = "">
// Generated from exceptionBase.ftl
package ${package}.${majorVersion}.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.betfair.cougar.api.ResponseCode;
import ${package}.${majorVersion}.enumerations.*;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;
import com.betfair.cougar.core.api.transcription.EnumUtils;

<#assign exception=parseException(doc, interface)>

/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
@SuppressWarnings("all")
public class  ${service} extends CougarApplicationException implements Transcribable {
    private static final String prefix = "${doc.@prefix}-";

    private static final Parameter __responseCodeParameter = new Parameter("responseCode",new ParameterType(ResponseCode.class, null ),false);
    private static final Parameter __stackSizeParameter = new Parameter("stackSize",new ParameterType(Integer.class, null ),false);
    private static final Parameter __stackClassNameParameter = new Parameter("stackClass",new ParameterType(String.class, null ),false);
    private static final Parameter __stackMethodNameParameter = new Parameter("stackMethod",new ParameterType(String.class, null ),false);
    private static final Parameter __stackFileNameParameter = new Parameter("stackFile",new ParameterType(String.class, null ),false);
    private static final Parameter __stackLineNumberParameter = new Parameter("stackLineNo",new ParameterType(Integer.class, null ),false);

    <#recurse doc><#t>

    public ${service}(ResponseCode responseCode ${constructorArgs}) {
    	super(responseCode,  prefix + ${firstEnumName}.getCode());
        ${constructorParams}
    }

    private ${service}(ResponseCode responseCode ${constructorArgsEnumsAsStrings}) {
    	super(responseCode,  prefix + ${firstEnumName});
        ${constructorParamsEnumsAsStrings}
    }

    public ${service}(${constructorArgs?substring(2)}){
        this(ResponseCode.BusinessException ${constructorArgsNoTypes});
    }

    /**
     * Constructor for reading the Exception from a TranscriptionInput source
     * @param in the TranscriptionInput to read the exception data from
     */
    public ${service}(TranscriptionInput in, Set<TranscribableParams> _transcriptionParams) throws Exception {
        this((ResponseCode)in.readObject(__responseCodeParameter, true)
    <#list exception.params as param>
        <#if param.isEnumType>
            <#assign paramCapFirst=param.paramName?cap_first><#t>
        , read${paramCapFirst}(in, _transcriptionParams)
        <#else>
        , ((<@createTypeDecl param.paramType/>)in.readObject(__${param.paramName}Parameter, true))
        </#if>
    </#list>);
    transcribeStackTrace(in);
    }

    <#list exception.params as param>
        <#if param.isEnumType>
            <#assign paramCapFirst=param.paramName?cap_first><#t>
    private static String read${paramCapFirst}(TranscriptionInput in, Set<TranscribableParams> _transcriptionParams) throws Exception {
        if (_transcriptionParams.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            return (String) in.readObject(__${param.paramName}Parameter, true);
        }
        else {
            <@createTypeDecl param.paramType/> ${param.paramName} = (<@createTypeDecl param.paramType/>) in.readObject(__${param.paramName}Parameter, true);
            return ${param.paramName} != null ? ${param.paramName}.name() : null;
        }
    }
        </#if>
    </#list>

    /**
     * Constructor with the cause of the exception (exception chaining)
     * @param  cause the cause
     * @see Throwable#getCause()
     */
    public ${service}(Throwable cause, ResponseCode responseCode ${constructorArgs}) {
        super(responseCode,  prefix + ${firstEnumName}.getCode(), cause);
        ${constructorParams}
    }

    @Override
	public List<String[]> getApplicationFaultMessages() {
		List<String[]> appFaults = new ArrayList<String[]>();
		${appFaultMessages}
		return appFaults;
	}

    @Override
	public String getApplicationFaultNamespace() {
		return "${namespace}";
	}

    @Override
    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> _transcriptionParams, boolean client) throws Exception {
		out.writeObject(getResponseCode(), __responseCodeParameter, client);
	    <#list exception.params as param>
            <#if param.isEnumType>
        if (_transcriptionParams.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            out.writeObject(get${param.paramName?cap_first}() != null ? get${param.paramName?cap_first}().name() : null, __${param.paramName}Parameter, client);
        }
        else {
            out.writeObject(get${param.paramName?cap_first}(), __${param.paramName}Parameter, client);
        }
            <#else>
        out.writeObject(get${param.paramName?cap_first}(), __${param.paramName}Parameter, client);
            </#if>
	    </#list>
	    transcribeStackTrace(out);
	}

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
//Empty - transcription is done in the constructor
    }

    <#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
    public static final ServiceVersion SERVICE_VERSION = new ServiceVersion("${dotMajorMinorVersion}");

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

    public static final Parameter[] PARAMETERS = new Parameter[] { __responseCodeParameter <@compress single_line=true>
    <#list exception.params as param>
        , __${param.paramName}Parameter
    </#list>
    , __stackSizeParameter, __stackClassNameParameter, __stackMethodNameParameter,  __stackFileNameParameter,  __stackLineNumberParameter };
    </@compress>

<#t>
<#macro parameter><#t>
	<#local parsedParam = parseParam(service, .node, service)>
    <#assign param=.node.@name><#t>
    <#assign paramCapFirst=.node.@name?cap_first><#t>
    <#assign paramType=.node.@type><#t>
    <#assign isEnumType=.node.validValues[0]??><#t>
    <#if isEnumType>
    	<#assign javaType = "${service}${paramCapFirst}Enum"><#t>
    <#else>
    	<#assign javaType = translateTypes(paramType)><#t>
    </#if>
    <#assign constructorArgs = "${constructorArgs}, ${javaType} ${param}">
    <#assign constructorArgsNoTypes = "${constructorArgsNoTypes}, ${param}">
    <#assign constructorParams = "${constructorParams} set${paramCapFirst}(${param});">
    <#if isEnumType>
        <#assign constructorArgsEnumsAsStrings = "${constructorArgsEnumsAsStrings}, String ${param}">
        <#assign constructorArgsNoTypesEnumsAsStrings = "${constructorArgsNoTypesEnumsAsStrings}, ${param}">
        <#assign constructorParamsEnumsAsStrings = "${constructorParamsEnumsAsStrings} setRaw${paramCapFirst}Value(${param});">
    <#else>
        <#assign constructorArgsEnumsAsStrings = "${constructorArgsEnumsAsStrings}, ${javaType} ${param}">
        <#assign constructorArgsNoTypesEnumsAsStrings = "${constructorArgsNoTypesEnumsAsStrings}, ${param}">
        <#assign constructorParamsEnumsAsStrings = "${constructorParamsEnumsAsStrings} set${paramCapFirst}(${param});">
    </#if>
    <#assign appFaultMessages = "${appFaultMessages} appFaults.add(new String[] {\"${param}\", String.valueOf(${param})});">
	<#if !firstEnumName??>
    	<#assign firstEnumName = "${param}">
    </#if><#t>
    private static final Parameter __${parsedParam.paramName}Parameter = <@createParameterDecl parsedParam/>;
 /**
<#if .node.description?has_content>
  * ${.node.description?trim}
</#if>
  */
    protected ${javaType} ${param} ;

    public final ${javaType} get${paramCapFirst}()  {
        return ${param};
    }

    private final void set${paramCapFirst}(${javaType} ${param})  {
    <#if isEnumType>
        if (${param} == ${javaType}.UNRECOGNIZED_VALUE) {
            throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
        }
    </#if>
        this.${param} = ${param};
    <#if isEnumType>
        this.raw${paramCapFirst}Value=${param} != null ? ${param}.name() : null;
    </#if>
    }

    <#if isEnumType>
    private String raw${paramCapFirst}Value;

    public final String getRaw${paramCapFirst}Value()  {
        return raw${paramCapFirst}Value;
    }

    private final void setRaw${paramCapFirst}Value(String ${param})  {
        ${javaType} enumValue = ${param} != null ? EnumUtils.readEnum(${javaType}.class, ${param}) : null;
        this.${param}=enumValue;
        this.raw${paramCapFirst}Value=${param};
    }
    </#if>
</#macro><#t>

<#macro @element></#macro><#t>

