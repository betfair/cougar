<#ftl strip_text=true>
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
<#include "common.ftl"><#t>

<#function parseType type>
	<#local javaType="">
	<#local componentTypes=[]>
	<#if type == "list(byte)">
		<#local javaType="byte[]" >
	<#elseif type?starts_with("list(") && type?ends_with(")")>
	    <#local subType = type?substring(5, type?length-1)?trim>
	    <#local javaType="List">
	    <#local componentTypes = componentTypes + [parseType(subType)]>
	<#elseif type?starts_with("set(") && type?ends_with(")")><#t>
	    <#local subType = type?substring(4, type?length-1)?trim><#t>
	    <#local javaType="Set">
	    <#local componentTypes = componentTypes + [parseType(subType)]>
	<#elseif type?starts_with("map(") && type?ends_with(")")><#t>
		<#local comma = type?index_of(",")>
		<#local javaType="Map">
		<#local keyType = type?substring(4, comma)?trim><#t>
		<#local valueType = type?substring(comma+1, type?length-1)?trim><#t>
		<#local componentTypes = componentTypes + [parseType(keyType),parseType(valueType)]>
	<#elseif type == "i64" >
	    <#local javaType="Long" >
	<#elseif type == "i32" >
	    <#local javaType="Integer" >
	<#elseif type == "byte" >
	    <#local javaType="Byte" >
	<#elseif type == "string" >
	    <#local javaType="String" >
	<#elseif type == "float" >
	    <#local javaType="Float" >
	<#elseif type == "double" >
	    <#local javaType="Double" >
	<#elseif type == "bool" >
	    <#local javaType="Boolean" >
	<#elseif type == "dateTime" >
	    <#local javaType="Date" >
	<#elseif type == "void" >
	    <#local javaType="void" >
	<#else>
	    <#local javaType=type >
	</#if>
	<#local paramType = {"javaType":javaType,"componentTypes":componentTypes}>
	<#return paramType>
</#function>

<#macro createReturnTypeDeclForExecutionResult type>
<@compress single_line=true>
<#if type.javaType=='void'>
    ${type.javaType?cap_first}
<#else>
    ${type.javaType}
    <#if type.componentTypes?size!=0>
        ${"<"}
        <#list type.componentTypes as componentType>
            <@createTypeDecl componentType/>
            <#if componentType_has_next>,</#if>
        </#list>
        ${">"}
    </#if>
</#if>
</@compress>
</#macro>

<#macro createTypeDecl type>
	<@compress single_line=true>
	${type.javaType}
	<#if type.componentTypes?size!=0>
		${"<"}
		<#list type.componentTypes as componentType>
			<@createTypeDecl componentType/>
			<#if componentType_has_next>,</#if>
		</#list>
		${">"}
	</#if>
	</@compress>
</#macro>

<#macro createParameterTypeDecl type>
	<@compress single_line=true>
	new ParameterType(${type.javaType}.class,
	<#if type.componentTypes?size!=0>
		new ParameterType [] {
		<#list type.componentTypes as componentType>
			<@createParameterTypeDecl componentType/>
			<#if componentType_has_next>,</#if>
		</#list>
		}
	<#else>
		null
	</#if>
	)
	</@compress>
</#macro>

<#macro createParameterDecl param>
	<@compress single_line=true>
    new Parameter("${param.paramName}",<@createParameterTypeDecl param.paramType/>,${param.isMandatory?string})
	</@compress>
</#macro>

<#macro parseParamDebug parentName param interface>
    <#local mandatory="false">
    <#if param.@mandatory?? && (param.@mandatory?size > 0)>
        <#local mandatory=param.@mandatory?lower_case>
    </#if>

    <#local isSimpleType = isSimpleType(interface, param.@type)>
    <#local isSimpleTypeEnum = isSimpleType && toSimpleType(interface, param.@type).validValues[0]??>
    <#local isLocalEnumType = param.validValues[0]??>
    <#local isEnumType = isLocalEnumType || isSimpleTypeEnum>
    // mandatory = ${mandatory}
    // isSimpleType = ${isSimpleType?string}
    // isSimpleTypeEnum = ${isSimpleTypeEnum?string}
    // isLocalEnumType = ${isLocalEnumType?string}
    // isEnumType = ${isEnumType?string}
</#macro>


<#function parseParam parentName param interface>
    <#return parseParam2(parentName, param, param.@name, interface)>
</#function>
<#function parseParam2 parentName param paramName interface>

    <#local mandatory="false">
    <#if param.@mandatory?? && (param.@mandatory?size > 0)>
        <#local mandatory=param.@mandatory?lower_case>
    </#if>

    <#local isSimpleType = isSimpleType(interface, param.@type)>
    <#local isSimpleTypeEnum = isSimpleType && toSimpleType(interface, param.@type).validValues[0]??>
    <#local isLocalEnumType = param.validValues[0]??>
    <#local isEnumType = isLocalEnumType || isSimpleTypeEnum>

    <#if isLocalEnumType>
    	<#local enumValues = []>
    	<#list param.validValues.value as validValue>
    		<#local enumValues = enumValues + [{"name":validValue.@name,"id":validValue.@id!""}]>
    	</#list>
    	<#local paramType = {"javaType":"${parentName?cap_first}${paramName?cap_first}Enum","componentTypes":[],"enumValues":enumValues}>
    <#else>
    	<#local paramType = parseType(param.@type)>
    </#if>
	<#return {"paramName":paramName,"paramType":paramType,"isMandatory":mandatory=="true","description":param.description,"paramStyle":param.extensions.style,"isEnumType":isEnumType,"isSimpleType":isSimpleType,"rawReturnType":param.@type}>
</#function>

<#function parseOperation operation interface>
    <#local params=[]>
    <#local exceptions=[]>
    <#local operationName=operation.@name>
    <#local description=operation.description>
    <#local responseParam = parseParam2(operationName, operation.parameters.simpleResponse, "WrappedValue", interface)>
    <#local returnType=responseParam.paramType>
    <#local connected = (operation.@connected[0]!"false")?lower_case?trim=="true">

    <#list operation.parameters.request.parameter as param>
        <#local params = params + [parseParam(operationName, param, interface)]>
    </#list>

    <#list operation.parameters.exceptions.exception as exception>
        <#local exceptions = exceptions + [exception.@type]>
    </#list>

    <#return {"operationName":operationName,"description":description,"returnType":returnType,"params":params,"exceptions":exceptions,"connected":connected, "rawReturnType":responseParam.rawReturnType, "responseParam":responseParam}>
</#function>

<#function parseOperations interface>

	<#local operations=[]>

	<#list interface.operation as operation>
		<#local operations = operations + [parseOperation(operation, interface)]>
	</#list>

	<#return operations>

</#function>

<#function parseEvents interface>
	<#local events=[]>
	<#list interface.event as event>
		<#local events = events + [parseEvent(event, interface)]>
	</#list>
	<#return events>
</#function>


<#function parseInterface interface>
	<#local operations = parseOperations(interface)>
    <#local events = parseEvents(interface)>
	<#local dataTypes = []>
	<#list interface.dataType as dataType>
		<#local dataTypes = dataTypes + [parseDataType(dataType, interface)]>
	</#list>
	<#local exceptions = []>
    <#local exceptionMap = {}>
	<#list interface.exceptionType as exceptionType>
        <#local e = parseException(exceptionType, interface)>
		<#local exceptions = exceptions + [e]>
        <#local exceptionMap = exceptionMap + { e.exceptionName : e}>
	</#list>
	<#return {"serviceName":interface.@name,"operations":operations,"events":events,"dataTypes":dataTypes,"exceptions":exceptions,"exceptionMap":exceptionMap}>
</#function>

<#function parseDataType dataTypeNode interface>
	<#local dataTypeName = dataTypeNode.@name>
	<#local params = []>
	<#list dataTypeNode.parameter as param>
		<#local params = params + [parseParam(dataTypeName, param, interface)]>
	</#list>
	<#return {"dataTypeName":dataTypeName,"params":params}>
</#function>

<#function parseEvent eventNode interface>
    <#local name=eventNode.@name>
    <#local description=eventNode.description>
    <#local params=[]>
    <#list eventNode.parameter as param>
        <#local params = params + [parseParam(name, param, interface)]>
    </#list>
    <#return {"name":name,"description":description,"params":params}>
</#function>

<#function parseException exceptionNode interface>
	<#local exceptionName = exceptionNode.@name>
	<#local codePrefix = exceptionNode.@prefix>
	<#local params = []>
	<#list exceptionNode.parameter as param>
		<#local params = params + [parseParam(exceptionName, param, interface)]>
	</#list>
	<#return {"exceptionName":exceptionName,"codePrefix":codePrefix,"params":params}>
</#function>

