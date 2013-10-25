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
<#include "external.ftl"><#t>
<#include "interfaceParser.ftl"><#t>
<#assign service = doc.@name><#t>
<#assign mandatoryCheck = "mandCheck">
<#assign mandatoryChildCheck = "">
<#assign toString = "\"\"">
<#assign mapKeyTypeArray=[]>
<#assign mapValueTypeArray=[]>
<#assign mapNameArray=[]>
<#assign mapNumber=0>
<#assign mapSuffix="">

<#function getFactoryName param paramType interface withinComplexType><#t>
<#assign retType=""><#t>
    <#if paramType == "list(byte)"><#t>
        <#assign retType = "byteArrayProjector" ><#t>
    <#elseif paramType?starts_with("list(") && paramType?ends_with(")")><#t>
        <#assign retType=param+"Projector" ><#t>
    <#elseif paramType?starts_with("set(") && paramType?ends_with(")")><#t>
        <#assign retType=param+"Projector" ><#t>
    <#elseif paramType?starts_with("map(") && paramType?ends_with(")")><#t>
        <#assign retType=param+"Projector" ><#t>
    <#elseif paramType == "i64" ><#t>
        <#assign retType = "longProjector" ><#t>
    <#elseif paramType == "i32" > <#t>
        <#assign retType = "intProjector" ><#t>
    <#elseif paramType == "byte" > <#t>
        <#assign retType = "byteProjector" ><#t>
    <#elseif paramType == "string" > <#t>
        <#assign retType = "stringProjector" ><#t>
    <#elseif paramType == "float" > <#t>
        <#assign retType = "floatProjector" ><#t>
    <#elseif paramType == "double" > <#t>
        <#assign retType = "doubleProjector" ><#t>
    <#elseif paramType == "bool" > <#t>
        <#assign retType = "booleanProjector" ><#t>
    <#elseif paramType == "dateTime" > <#t>
        <#assign retType = "dateProjector" ><#t>
    <#else> <#t>
      <#if withinComplexType>
        <#assign retType=getFactoryType(paramType, interface) ><#t>
      <#else>
        <#assign retType=param+"Projector" ><#t>
      </#if>
    </#if><#t>
<#return retType><#t>
</#function><#t>

<#function getFactoryType paramType interface><#t>
<#assign retType=""><#t>
    <#if paramType == "list(byte)"><#t>
        <#assign retType = "" ><#t>
    <#elseif (paramType?starts_with("list(") || paramType?starts_with("set(")) && paramType?ends_with(")")><#t>
        <#if paramType?starts_with("list(")>
            <#assign subType = paramType?substring(5, paramType?length-1)?trim><#t>
        <#else >
            <#assign subType = paramType?substring(4, paramType?length-1)?trim><#t>
        </#if>
        <#assign translatedSubType=subType><#t>
      <#if isSimpleType(interface, subType)><#t>
        <#assign translatedSubType=getSimpleTypeName(interface, subType)><#t>
      </#if>
        <#assign retType="listProjector("+getFactoryName("", translatedSubType, interface, true)+")" ><#t>
    <#elseif paramType?starts_with("map(") && paramType?ends_with(")")><#t>
        <#assign comma = paramType?index_of(",")>
        <#assign keyType = paramType?substring(4, comma)?trim><#t>
        <#assign valueType = paramType?substring(comma+1, paramType?length-1)?trim><#t>
        <#assign translatedValue=valueType><#t>
      <#if isSimpleType(interface, valueType)><#t>
        <#assign translatedValue=getSimpleTypeName(interface, valueType)><#t>
      </#if>
        <#assign retType="mapProjector("+getFactoryName("", translatedValue, interface, true)+")" ><#t>
    <#elseif paramType == "i64" ><#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "i32" > <#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "byte" > <#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "string" > <#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "float" > <#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "double" > <#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "bool" > <#t>
        <#assign retType = "" ><#t>
    <#elseif paramType == "dateTime" > <#t>
        <#assign retType = "" ><#t>
    <#else> <#t>
        <#assign retType="objectProjector("+paramType+"CO.class)" ><#t>
    </#if><#t>
<#return retType><#t>
</#function><#t>
// Generated from connectedDataType.ftl
package ${package}.${majorVersion}.co;

import java.util.*;

import com.betfair.platform.virtualheap.*;
import com.betfair.platform.virtualheap.projection.*;
import static com.betfair.platform.virtualheap.projection.ProjectorFactory.*;

/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
public interface ${service}CO {
<#assign paramNameTypeHash={}>
<#assign paramHash={}>

<#assign dataType=parseDataType(doc, interface)>

<#recurse doc><#t>

}
<#t>
<#t>
<#macro parameter><#t>
    <#assign param=.node.@name?cap_first><#t>
    <#assign paramType=.node.@type><#t>

    <#if isSimpleType(interface, paramType)>
        <#assign paramType = getSimpleTypeName(interface, paramType)><#t>
    </#if><#t>

    <#if paramType == "list(byte)"><#t>
        <#assign javaType = "byte[]" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif (paramType?starts_with("list(") || paramType?starts_with("set(")) && paramType?ends_with(")")><#t>
        <#if paramType?starts_with("list(")>
            <#assign subType = paramType?substring(5, paramType?length-1)?trim><#t>
        <#else >
            <#assign subType = paramType?substring(4, paramType?length-1)?trim><#t>
        </#if>
        <#assign javaType=translateTypes2(subType, interface, true, "CO", true, true) ><#t>
        <#if javaType?starts_with("ScalarProjection<") >
            <#assign javaType=javaType?substring(17,javaType?length-1) ><#t>
            <#assign listType="HListScalar" ><#t>
        <#else>
            <#assign listType="HListComplex" ><#t>
        </#if>
        <#assign javaType=listType+"<"+javaType+">" ><#t>
        <#assign returnTypeIsObjectOrCollection = true ><#t>
    <#elseif paramType?starts_with("map(") && paramType?ends_with(")")><#t>
        <#assign comma = paramType?index_of(",")>
        <#assign keyType = paramType?substring(4, comma)?trim><#t>
        <#assign valueType = paramType?substring(comma+1, paramType?length-1)?trim><#t>
        <#assign javaType=translateTypes2(valueType, interface, true, "CO", true, true)><#t>
        <#if javaType?starts_with("ScalarProjection<") >
            <#assign javaType=javaType?substring(17,javaType?length-1) ><#t>
            <#assign mapType="HMapScalar" ><#t>
        <#else>
            <#assign mapType="HMapComplex" ><#t>
        </#if>
        <#assign javaType=mapType+"<"+javaType+">" ><#t>
        <#assign returnTypeIsObjectOrCollection = true ><#t>
    <#elseif paramType == "i64" ><#t>
        <#assign javaType="Long" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "i32" > <#t>
        <#assign javaType="Integer" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "byte" > <#t>
        <#assign javaType="Byte" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "string" > <#t>
        <#assign javaType="String" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "float" > <#t>
        <#assign javaType="Float" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "double" > <#t>
        <#assign javaType="Double" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "bool" > <#t>
        <#assign javaType="Boolean" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#elseif paramType == "dateTime" > <#t>
        <#assign javaType="Date" ><#t>
        <#assign returnTypeIsObjectOrCollection = false ><#t>
    <#else> <#t>
        <#assign javaType=paramType+"CO" ><#t>
        <#assign returnTypeIsObjectOrCollection = true ><#t>
    </#if><#t>

    <#if !returnTypeIsObjectOrCollection>
    void set${param}(${javaType} p);
    </#if>
    ${javaType} get${param}();
    
</#macro><#t>

<#macro @element></#macro><#t>

