<#--
 Copyright 2013, The Sporting Exchange Limited

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<#include "../common.ftl"><#t>
<#include "../interfaceParser.ftl"><#t>
// Generated from event.ftl
package ${package}.${majorVersion}.events;

import ${package}.${majorVersion}.enumerations.*;
import ${package}.${majorVersion}.to.*;

import com.betfair.cougar.transport.api.protocol.events.AbstractEvent;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;

import java.util.*;
import javax.xml.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


<#assign event=parseEvent(doc, interface)><#t>

/**
 * ${event.description}
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ${className} extends AbstractEvent implements Transcribable {

	<#list event.params as parameter><#t>
        <#if parameter.paramStyle == "body"><#t>
            <#assign paramType = parameter.paramType.javaType><#t>
            <#assign paramName = parameter.paramName><#t>
            <#assign isEnumType = parameter.isEnumType><#t>

	private ${paramType} ${paramName};

            <#if isEnumType>
    @XmlTransient
    @JsonIgnore
            </#if>
	public ${paramType} get${paramName?cap_first}() {
		return ${paramName};
	}

    /**
     * Sets the ${paramName} parameter
     *
     * @param ${paramName} - ${parameter.description}
     */
	public void set${paramName?cap_first}(${paramType} ${paramName}) {
            <#if isEnumType>
        if (${paramName} == ${paramType}.UNRECOGNIZED_VALUE) {
            throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
        }
            </#if>
		this.${paramName} = ${paramName};
            <#if isEnumType>
        this.raw${paramName?cap_first}Value=${paramName} != null ? ${paramName}.name() : null;
            </#if>
	}

            <#if isEnumType>
    private String raw${paramName?cap_first}Value;

    @XmlElement(name="${paramName}")
    @JsonProperty(value = "${paramName}")
    public final String getRaw${paramName?cap_first}Value()  {
        return raw${paramName?cap_first}Value;
    }

    public final void setRaw${paramName?cap_first}Value(String ${paramName})  {
        ${paramType} enumValue = ${paramName} != null ? EnumUtils.readEnum(${paramType}.class, ${paramName}) : null;
        this.${paramName}=enumValue;
        this.raw${paramName?cap_first}Value=${paramName};
    }
            </#if>
        </#if>
	</#list>

    <#assign params="">
    <#list event.params as param>
    private static final Parameter __${param.paramName}Param = <@createParameterDecl param/>;
        <#assign params = params + "__" + param.paramName + "Param">
        <#if param_has_next>
            <#assign params = params + ", ">
        </#if>
    </#list>

    @XmlTransient
    @JsonIgnore
    public static final Parameter[] PARAMETERS = new Parameter[] { ${params} };

    @XmlTransient
    @JsonIgnore
    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        <#list event.params as param>
            <#if param.isEnumType>
        if (params.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            out.writeObject(get${param.paramName?cap_first}() != null ? get${param.paramName?cap_first}().name() : null, __${param.paramName}Param, client);
        }
        else {
            out.writeObject(get${param.paramName?cap_first}(), __${param.paramName}Param, client);
        }
            <#else>
        out.writeObject(get${param.paramName?cap_first}(), __${param.paramName}Param, client);
            </#if>
        </#list>
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        <#list event.params as param>
            <#if param.isEnumType>
        if (params.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            setRaw${param.paramName?cap_first}Value((String)in.readObject(__${param.paramName}Param, client));
        }
        else {
            set${param.paramName?cap_first}((<@createTypeDecl param.paramType/>)in.readObject(__${param.paramName}Param, client));
        }
            <#else>
        set${param.paramName?cap_first}((<@createTypeDecl param.paramType/>)in.readObject(__${param.paramName}Param, client));
            </#if>
        </#list>
    }

    <#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
    @XmlTransient
    @JsonIgnore
    public static final ServiceVersion SERVICE_VERSION = new ServiceVersion("${dotMajorMinorVersion}");

    @XmlTransient
    @JsonIgnore
    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }


}

