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
<#include "../common.ftl"><#t>
<#include "../interfaceParser.ftl"><#t>
<#assign name = doc.@name><#t>
<#assign dataTypeName = doc.@name>
<#assign mapKeyTypeArray=[]>
<#assign mapValueTypeArray=[]>
<#assign mapNameArray=[]>
<#assign mapSuffix="">
<#assign mapNumber=0>
// Generated from requestDataType.ftl
package ${package}.${majorVersion}.rescript;

import ${package}.${majorVersion}.to.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import java.util.*;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.api.annotations.*;
import ${package}.${majorVersion}.enumerations.*;
import com.betfair.cougar.api.*;
import com.betfair.cougar.util.ValidationUtils;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import com.betfair.cougar.util.BitmapBuilder;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptBody;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Wrapper object for the ${name} operation
 */
@SuppressWarnings("all")
@XmlRootElement(name="${className}")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
@XmlType(propOrder={})
@JsonIgnoreProperties(ignoreUnknown=true)
public class ${className} implements RescriptBody {

	private Map<String,Object> parameters = new HashMap<String,Object>();

	@Override
	public Object getValue(String name) {
		return parameters.get(name);
	}

    <#assign isConnected = (doc.@connected[0]!"false")?lower_case?trim=="true">
    <#if !isConnected>
        <#assign mapIndex=0>
        <#list doc.parameters.request.parameter as parameter>
            <#if parameter.extensions.style == "body">
                <#assign param=parseParam(dataTypeName, parameter, interface)><#t>
                <#assign paramName=param.paramName><#t>
                <#assign paramCapFirst=param.paramName?cap_first><#t>
                <#assign paramType=param.paramType><#t>
                <#assign isEnumType=param.isEnumType><#t>
                <#if parameter.validValues[0]??>
                    <#assign javaType = "${dataTypeName?cap_first}${paramCapFirst}Enum"><#t>
                <#else>
                    <#assign javaType = translateTypes(parameter.@type)><#t>
                </#if>

                <#if javaType?starts_with("List<")>
                    <#include "../collections.ftl">
                <#elseif javaType?starts_with("Set<")>
                    <#include "../collections.ftl">
                <#elseif javaType?starts_with("Map<")>
                    <#assign genericTypes = javaType?substring(javaType?index_of("<")+1, javaType?index_of(">"))?split(",")>
                    <#assign mapKeyType= genericTypes[0]>
                    <#assign mapValueType= genericTypes[1]>
                    <#assign mapName=paramName>
                    <#include "../mapAdapter.ftl">
                    <#include "../collections.ftl">
                    <#assign mapIndex = mapIndex + 1>
                <#else>
            private ${javaType} ${paramName};
                    <#if isEnumType>
            @XmlTransient
            @JsonIgnore
                    <#else>
            @XmlElement(name="${paramName}")
                    </#if>
                </#if>

            public ${javaType} get${paramCapFirst}() {
                return ${paramName};
            }

            public void set${paramCapFirst}(${javaType} ${paramName}) {
                <#if isEnumType>
                if (${paramName} == <@createTypeDecl paramType/>.UNRECOGNIZED_VALUE) {
                    throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
                }
                </#if>
                this.${paramName} = ${paramName};
                <#if isEnumType>
                this.raw${paramCapFirst}Value=${paramName} != null ? ${paramName}.name() : null;
                parameters.put("${paramName}", ${paramName}.name());
                <#else>
                parameters.put("${paramName}", ${paramName});
                </#if>
            }

                <#if isEnumType>
            private String raw${paramCapFirst}Value;

            @XmlElement(name="${paramName}")
            @JsonProperty(value = "${paramName}")
            public String getRaw${paramCapFirst}Value() {
                return raw${paramCapFirst}Value;
            }

            public void setRaw${paramCapFirst}Value(String ${paramName}) {
                <@createTypeDecl paramType/> enumValue = ${paramName} != null ? EnumUtils.readEnum(<@createTypeDecl paramType/>.class, ${paramName}) : null;
                this.${paramName} = enumValue;
                this.raw${paramCapFirst}Value = ${paramName};
                parameters.put("${paramName}", ${paramName});
            }
                </#if>
            </#if>
        </#list>
    </#if>

}

