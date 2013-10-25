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
<#include "../common.ftl">

<#assign serviceName = doc.@name>
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
<#assign serviceDefinitionName = serviceName+"ServiceDefinition">

<#assign interfacePath = "/"+serviceName>
<#if doc.extensions.path[0]??>
    <#assign interfacePath = doc.extensions.path>
</#if>

// Generated from rescriptServiceBindingDescriptor.ftl
package ${package}.${majorVersion}.rescript;

import ${package}.${majorVersion}.${serviceDefinitionName};

import java.util.ArrayList;
import java.util.List;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;

import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;

import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor.ParamSource;

public class ${serviceName}RescriptServiceBindingDescriptor implements HttpServiceBindingDescriptor {

    private final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");
    private final String serviceName = "${serviceName}";

    public ${serviceName}RescriptServiceBindingDescriptor() {
        <#list doc.operation as operation>
          <#assign operationPath = "/" + operation.@name>
          <#assign isConnected = (operation.@connected[0]!"false")?lower_case?trim=="true">
          <#if !isConnected>
            <#if operation.extensions.path[0]??>
                <#assign operationPath = operation.extensions.path>
            </#if>
        List<RescriptParamBindingDescriptor> ${operation.@name}ParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
        <#assign requiresRequestWrapper = false>
            <#list operation.parameters.request.parameter as parameter>     
            <#if parameter.extensions.style == 'body'><#assign requiresRequestWrapper = true></#if>
        ${operation.@name}ParamBindings.add(new RescriptParamBindingDescriptor("${parameter.@name}", ParamSource.${parameter.extensions.style?upper_case}));
            </#list>
        <#if !requiresRequestWrapper>
        ${operation.@name}Descriptor = new RescriptOperationBindingDescriptor(${serviceDefinitionName}.${operation.@name}Key, "${operationPath}", "${operation.extensions.method}", <@compress single_line=true>${operation.@name}ParamBindings
            <#if operation.parameters.simpleResponse.@type=="void">
            );
            <#else>
            , ${operation.@name?cap_first}Response.class);
            </#if>
            </@compress>
        <#else>
        ${operation.@name}Descriptor = new RescriptOperationBindingDescriptor(${serviceDefinitionName}.${operation.@name}Key, "${operation.extensions.path}", "${operation.extensions.method}", <@compress single_line=true>${operation.@name}ParamBindings,
            <#if operation.parameters.simpleResponse.@type=="void">
                null, ${operation.@name?cap_first}Request.class);
            <#else>
                ${operation.@name?cap_first}Response.class, ${operation.@name?cap_first}Request.class);
            </#if>
            </@compress>
          </#if>
        </#if>
        
        </#list>
        operations = new RescriptOperationBindingDescriptor[] {
            <#list doc.operation as operation>
              <#assign isConnected = (operation.@connected[0]!"false")?lower_case?trim=="true">
              <#if !isConnected>
                ${operation.@name}Descriptor<#if operation_has_next>,</#if>
              </#if>
            </#list>
        };
    }
    
    @Override
    public Protocol getServiceProtocol() {
        return Protocol.RESCRIPT;
    }

    @Override
    public String getServiceContextPath() {
        return "${interfacePath}/";
    }

    @Override
    public RescriptOperationBindingDescriptor[] getOperationBindings() {
        return operations;
    }

    @Override
    public ServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }


    private final RescriptOperationBindingDescriptor[] operations;
    
    <#list doc.operation as operation>
      <#assign isConnected = (operation.@connected[0]!"false")?lower_case?trim=="true">
      <#if !isConnected>
        private final RescriptOperationBindingDescriptor ${operation.@name}Descriptor;
      <#else>
        // Method ${operation.@name} is connected. Rescript doesn't support connected operations.
      </#if>
    </#list>

}
