/*
 * Copyright 2013, Simon Matić Langford
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
<#include "external.ftl"><#t>
<#include "interfaceParser.ftl"><#t>
<#assign dataTypeName = doc.@name><#t>
<#assign mandatoryCheck = "mandCheck">
<#assign mandatoryChildCheck = "">
<#assign toString = "\"\"">
<#assign mapKeyTypeArray=[]>
<#assign mapValueTypeArray=[]>
<#assign mapNameArray=[]>
<#assign mapNumber=0>
<#assign mapSuffix="">
// Generated from dataType.ftl
package ${package}.${majorVersion}.to;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import java.util.*;
import java.io.Externalizable;
import java.io.IOException;
import com.betfair.cougar.api.Result;
import com.betfair.cougar.api.Validatable;
import com.betfair.cougar.core.api.transcription.*;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.core.api.builder.*;
import com.betfair.cougar.util.ValidationUtils;
import ${package}.${majorVersion}.enumerations.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
@SuppressWarnings("all")
public class  ${dataTypeName}Builder implements Builder<${dataTypeName}> {

    private final ${dataTypeName} value = new ${dataTypeName}();
    private boolean seal = true;

<#assign paramNameTypeHash={}>
<#assign paramHash={}>

<#assign dataType=parseDataType(doc, interface)>

<#recurse doc><#t>

    public ${dataTypeName}Builder () {}

    public ${dataTypeName} build() {
        if (seal) {
            value.seal();
        }
        return value;
    }

    public ${dataTypeName}Builder leaveModifiable() {
        seal = false;
        return this;
    }

<#if mapNameArray?size!=0>
<#list mapNameArray as map>
    <#assign mapKeyType=mapKeyTypeArray[map_index]>
    <#assign mapValueType=mapValueTypeArray[map_index]>
    <#assign mapName=map>
    <#assign mapIndex=map_index>
    <#include "mapAdapter.ftl">
</#list>
</#if>

}


<#t>
<#t>
<#macro parameter><#t>
    <#assign param=parseParam(dataTypeName, .node, interface)><#t>
    <#assign paramName=param.paramName><#t>
    <#assign paramCapFirst=param.paramName?cap_first><#t>
    <#assign paramType=param.paramType><#t>
    <#assign isEnumType=param.isEnumType><#t>
    <#assign isMandatory=param.isMandatory><#t>

    <#if .node.validValues[0]??>
        <#assign javaType = "${dataTypeName}${paramCapFirst}Enum"><#t>
    <#else>
        <#assign javaType = translateTypes(.node.@type)><#t>
    </#if>



    // TYPE DEBUGGING FOR ${paramName}/<@createTypeDecl paramType/>
    <@parseParamDebug dataTypeName, .node, interface/>
    <#if javaType=="boolean">
    <#elseif javaType=="byte">
    <#elseif javaType=="int">
    <#elseif javaType=="long">
    <#elseif javaType=="double">
    <#elseif javaType=="float">
    <#elseif isEnumType>
    <#else>
    /**
    <#if param.description?has_content>
     * ${param.description?trim}
    </#if>
     */
    public final ${dataTypeName}Builder set${paramCapFirst}(Builder<<@createTypeDecl paramType/>> param)  {
        this.value.set${paramCapFirst}(param.build());
        return this;
    }
    </#if>


    /**
    <#if param.description?has_content>
     * ${param.description?trim}
    </#if>
     */
    public final ${dataTypeName}Builder set${paramCapFirst}(<@createTypeDecl paramType/> param)  {
        this.value.set${paramCapFirst}(param);
        return this;
    }

</#macro><#t>

<#macro @element></#macro><#t>

