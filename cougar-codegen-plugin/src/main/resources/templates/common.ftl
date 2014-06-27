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
<#function translateTypes type><#t>
<#return translateTypes2(type,"",false,"CO",false, false)><#t>
</#function><#t>

<#function translateTypes2 type interface treatAsConnected connectedObjectPostfix insideComplexType anonymousExtensionWithinComplexType><#t>
<#assign retType=""><#t>
<#if treatAsConnected><#t>
	<#assign collectionTypeSuffix="Projection" ><#t>
<#else> <#t>
    <#assign collectionTypeSuffix="" ><#t>
</#if><#t>
<#if type == "list(byte)"><#t>
	<#assign retType=optionallyWrap("byte[]", treatAsConnected) ><#t>
<#elseif type?starts_with("list(") && type?ends_with(")")><#t>
    <#assign subType = type?substring(5, type?length-1)?trim><#t>
  <#if treatAsConnected>
    <#assign retType="ListProjection<"+translateTypes2(subType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+">" ><#t>
  <#else> <#t>
    <#assign retType="List<"+translateTypes2(subType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+">" ><#t>
  </#if><#t>
<#elseif type?starts_with("set(") && type?ends_with(")")><#t>
  <#assign subType = type?substring(4, type?length-1)?trim><#t>
  <#if treatAsConnected>
    <#assign retType="SetProjection<"+translateTypes2(subType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+">" ><#t>
  <#else> <#t>
    <#assign retType="Set<"+translateTypes2(subType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+">" ><#t>
  </#if><#t>
<#elseif type?starts_with("map(") && type?ends_with(")")><#t>
   <#assign comma = type?index_of(",")>
     <#assign keyType = type?substring(4, comma)?trim><#t>
     <#assign valueType = type?substring(comma+1, type?length-1)?trim><#t>
  <#if treatAsConnected>
     <#assign retType="MapProjection<"+translateTypes2(valueType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+">" ><#t>
  <#else> <#t>
     <#assign retType="Map<"+translateTypes2(keyType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+","+translateTypes2(valueType, interface, treatAsConnected, connectedObjectPostfix, true, anonymousExtensionWithinComplexType)+">" ><#t>
  </#if><#t>
<#elseif type == "i64" ><#t>
	<#assign retType=optionallyWrap("Long", treatAsConnected) ><#t>
<#elseif type == "i32" > <#t>
	<#assign retType=optionallyWrap("Integer", treatAsConnected) ><#t>
<#elseif type == "byte" > <#t>
	<#assign retType=optionallyWrap("Byte", treatAsConnected) ><#t>
<#elseif type == "string" > <#t>
	<#assign retType=optionallyWrap("String", treatAsConnected) ><#t>
<#elseif type == "float" > <#t>
	<#assign retType=optionallyWrap("Float", treatAsConnected) ><#t>
<#elseif type == "double" > <#t>
	<#assign retType=optionallyWrap("Double", treatAsConnected) ><#t>
<#elseif type == "bool" > <#t>
	<#assign retType=optionallyWrap("Boolean", treatAsConnected) ><#t>
<#elseif type == "dateTime" > <#t>
	<#assign retType=optionallyWrap("Date", treatAsConnected) ><#t>
<#elseif type == "void" > <#t>
	<#assign retType=optionallyWrap("void", treatAsConnected) ><#t>
<#else> <#t>
  <#if treatAsConnected>
    <#if isSimpleType(interface, type)>
      <#assign retType=translateTypes2(getSimpleTypeName(interface, type), interface, treatAsConnected, connectedObjectPostfix, false, anonymousExtensionWithinComplexType) ><#t>
    <#else>
      <#if (insideComplexType && anonymousExtensionWithinComplexType)>
        <#assign retType="? extends "+type+connectedObjectPostfix ><#t>
      <#else>
        <#assign retType=type+connectedObjectPostfix ><#t>
      </#if>
    </#if>
  <#else> <#t>
    <#assign retType=type ><#t>
  </#if><#t>
</#if><#t>
    <#return retType><#t>
</#function><#t>

<#function optionallyWrap type wrap><#t>
  <#if wrap>
    <#assign retType="ScalarProjection<"+type+">" ><#t>
  <#else> <#t>
    <#assign retType=type ><#t>
  </#if><#t>
<#return retType><#t>
</#function><#t>

<#function isSimpleType interface paramType><#t>
    <#assign ret = false><#t>
    <#list interface.simpleType as st><#t>
        <#if st.@name==paramType><#t>
            <#assign ret = true><#t>
        </#if><#t>
    </#list>
    <#return ret><#t>
</#function>


<#function getSimpleTypeName interface paramType><#t>
    <#assign ret = paramType><#t>
    <#list interface.simpleType as st><#t>
        <#if st.@name==paramType><#t>
            <#assign ret = st.@type><#t>
        </#if><#t>
    </#list>
    <#return ret><#t>
</#function>

<#function toSimpleType interface paramType><#t>
    <#assign ret = {}><#t>
    <#list interface.simpleType as st><#t>
        <#if st.@name==paramType><#t>
            <#assign ret = st><#t>
        </#if><#t>
    </#list>
    <#return ret><#t>
</#function>

<#function restParam type name><#t>
<#assign retType="@"+type?cap_first+"Param(\""+name+"\")"><#t>
<#return retType><#t>
</#function><#t>

