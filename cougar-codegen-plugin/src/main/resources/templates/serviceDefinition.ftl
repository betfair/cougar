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
<#include "common.ftl">
<#include "interfaceParser.ftl">

// Generated from serviceDefinition.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.transcription.*;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.*;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import java.util.*;

<#assign parsedInterface=parseInterface(doc)>

<#assign serviceName = doc.@name>
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>

public final class ${serviceName}ServiceDefinition extends ServiceDefinition {

	public static final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");
	public static final String serviceName = "${serviceName}";

	public ${serviceName}ServiceDefinition() {
		super();
		init();
	}

<#list parsedInterface.operations as operation>
  <#if operation.connected>
    <#assign operationKeyType="ConnectedObject">
  <#else>
    <#assign operationKeyType="Request">
  </#if>
public static final OperationKey ${operation.operationName}Key = new OperationKey(serviceVersion, serviceName, "${operation.operationName}", OperationKey.Type.${operationKeyType});
	<@compress single_line=true>
	private final OperationDefinition ${operation.operationName}Def = new SimpleOperationDefinition(
		${operation.operationName}Key,
		new Parameter [] 
		{
		<#list operation.params as param>
			<@createParameterDecl param/>
			<#if param_has_next>,</#if>
		</#list>
		},
        <#if operation.connected>
        new ParameterType(ConnectedResponse.class, null )
        <#else>
            <#if operation.responseParam.isEnumType>
        new ParameterType(EnumWrapper.class, new ParameterType[] { new ParameterType(${operation.responseParam.paramType.javaType}.class, null) })
            <#else>
		<@createParameterTypeDecl operation.returnType/>
            </#if>
        </#if>
		);
	</@compress>

</#list>

    //Event operation and key definitions
<#list parsedInterface.events as event>
public static final OperationKey subscribeTo${event.name?cap_first}OperationKey = new OperationKey(serviceVersion, serviceName, "${event.name?cap_first}", OperationKey.Type.Event);
    <@compress single_line=true>
    public final OperationDefinition subscribeTo${event.name?cap_first}OperationDef = new SimpleOperationDefinition(
        subscribeTo${event.name?cap_first}OperationKey,
    	new Parameter [] {
        <#list event.params as param>
            <@createParameterDecl param/>
            <#if param_has_next>,</#if>
        </#list>
        }, null);
    </@compress>

</#list>

    public final OperationDefinition [] operationDefs = new OperationDefinition[] {
<#list parsedInterface.operations as operation>
    ${operation.operationName}Def<#if operation_has_next>,</#if>
</#list>
<#if parsedInterface.operations?has_content>,</#if>
<#list doc.event as event>
    subscribeTo${event.@name?cap_first}OperationDef<#if event_has_next>,</#if>
</#list>
    };

	public final ServiceVersion getServiceVersion() {
		return serviceVersion;
	}
	public final String getServiceName() {
		return serviceName;
	}
	public final OperationDefinition [] getOperationDefinitions() {
		return operationDefs;
	}
	
}