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
<#assign dataTypeName = doc.@name><#t>
<#assign mandatoryCheck = "false">
<#assign mandatoryChildCheck = "">
<#assign mapKeyTypeArray=[]>
<#assign mapValueTypeArray=[]>
<#assign mapNameArray=[]>
<#assign mapNumber=0>
// Generated from dataTypeDelegate.ftl
package ${package}.${majorVersion}.to;

import java.util.*;
import ${package}.${majorVersion}.enumerations.*;

/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
@SuppressWarnings("all")
public interface  ${dataTypeName}Delegate  {

<#recurse doc><#t>

	
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

    /**
    <#if .node.description?has_content>
     * ${.node.description?trim}
    </#if>
     */
    public <@createTypeDecl paramType/> get${paramCapFirst}()  ;

    /**
    <#if .node.description?has_content>
     * ${.node.description?trim}
    </#if>
     */
    public void set${paramCapFirst}(<@createTypeDecl paramType/> ${paramName});

    <#if isEnumType>
    public String getRaw${paramCapFirst}Value()  ;

    public void setRaw${paramCapFirst}Value(String ${paramName});
    </#if>
    
</#macro><#t>

<#macro @element></#macro><#t>

