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
<#compress>
<#include "../common.ftl"><#t>
<#include "../external.ftl"><#t>
<#include "../interfaceParser.ftl"><#t>
<#assign name = doc.@name><#t>
<#assign dataTypeName = doc.@name>
<#assign mapKeyTypeArray=[]>
<#assign mapValueTypeArray=[]>
<#assign mapNameArray=[]>
<#assign mapNumber=0>
<#assign mapSuffix="Response">
// Generated from responseDataType.ftl
package ${package}.${majorVersion}.rescript;

import ${package}.${majorVersion}.*;
import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import java.util.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import com.betfair.cougar.api.*;
import com.betfair.cougar.core.api.collectionwrappers.*;
import com.betfair.cougar.util.BitmapBuilder;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * Wrapper object for the ${name} operation response
 * needed to make the response compliant with the schema
 */
@SuppressWarnings("all")
@XmlRootElement(name="${className}")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown=true)
public class  ${className} implements RescriptResponse {


<#recurse doc><#t>

<#if mapNameArray?size!=0>
    <#list mapNameArray as map>
        <#assign mapKeyType=mapKeyTypeArray[map_index]>
        <#assign mapValueType=mapValueTypeArray[map_index]>
        <#assign mapName=map>
        <#assign mapIndex=map_index+"Response">
        <#include "../mapAdapter.ftl">
    </#list>
</#if>

}<#t>
<#t>
<#t>
<#macro parameters><#t>
    <#recurse .node><#t>
</#macro><#t>
<#t>
<#macro simpleResponse><#t>

    <#assign param=parseParam2(dataTypeName, .node, "wrappedValue", interface)><#t>
    <#assign paramName="wrappedValue"><#t>
    <#assign paramCapFirst=paramName?cap_first><#t>
    <#assign paramType=param.paramType><#t>
    <#assign isEnumType=param.isEnumType><#t>
    <#assign isMandatory=param.isMandatory><#t>
    <#if .node.validValues[0]??>
        <#assign javaType = "${dataTypeName?cap_first}${paramCapFirst}Enum"><#t>
    <#else>
        <#assign javaType = translateTypes(.node.@type)><#t>
    </#if>
    <#if javaType!="void">

    /**
    <#if .node.description?has_content>
     * ${.node.description?trim}
    </#if>
     */

        <#assign doNotWrap="true">
        <#if javaType?starts_with("List<")>
            <#include "../collections.ftl">
        <#elseif javaType?starts_with("Set<")>
            <#include "../collections.ftl">
        <#elseif javaType?starts_with("Map<")>
            <#include "../collections.ftl">
            <#assign mapType="true">
        <#else>
    private ${javaType} ${paramName};

            <#if javaType=="byte[]">
    @XmlElement(name="byte-array")
            <#else>
    @XmlElement(name="${javaType}")
            </#if>
            <#if javaType=="Date">
    @XmlJavaTypeAdapter(value=com.betfair.cougar.util.dates.XMLDateAdapter.class)
        </#if>

        </#if>
	public ${javaType} get${paramCapFirst}()  {
        return ${paramName};
    }

    public void set${paramCapFirst}(${javaType} ${paramName})  {
        <#if isEnumType>
        if (${paramName} == <@createTypeDecl paramType/>.UNRECOGNIZED_VALUE) {
            throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
        }
        </#if>
        this.${paramName}=${paramName};
    }

	@Override
	@SuppressWarnings("all")
    public void setResult(Object result)  {
        this.${paramName}=(${javaType})result;
        <#if mapType??>
            // Whenever the result is set, ensure that the pair-based value is created for XML marshalling
            mapAsEntries0${mapSuffix} = new ${dataTypeName}MapAdapter0${mapSuffix}().marshal(wrappedValue).entries;
        </#if>
    }

	@Override
    public Object getResult()  {
        return ${paramName};
    }

    public boolean isVoid() {
        return false;
    }

    <#else>
    public void setResult(Object result) {
    }

    public Object getResult() {
        return null;
    }

    public boolean isVoid() {
        return true;
    }
    </#if>


</#macro><#t>
<#t>
<#macro @element></#macro><#t>
</#compress>
