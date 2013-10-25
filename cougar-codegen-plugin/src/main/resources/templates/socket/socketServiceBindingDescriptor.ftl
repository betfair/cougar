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

// Generated from socketServiceBindingDescriptor.ftl
package ${package}.${majorVersion}.socket;

import ${package}.${majorVersion}.${serviceDefinitionName};

import java.util.ArrayList;
import java.util.List;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;

import com.betfair.cougar.transport.api.protocol.socket.SocketBindingDescriptor;

import com.betfair.cougar.transport.api.protocol.socket.SocketOperationBindingDescriptor;

public class ${serviceName}SocketServiceBindingDescriptor implements SocketBindingDescriptor {

    private final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");
    private final String serviceName = "${serviceName}";

    public ${serviceName}SocketServiceBindingDescriptor() {
        <#list doc.operation as operation>
          <#assign operationPath = "/" + operation.@name>
        ${operation.@name}Descriptor = new SocketOperationBindingDescriptor(${serviceDefinitionName}.${operation.@name}Key);
        </#list>
        operations = new SocketOperationBindingDescriptor[] {
            <#list doc.operation as operation>
                ${operation.@name}Descriptor<#if operation_has_next>,</#if>
            </#list>
        };
    }
    
    @Override
    public Protocol getServiceProtocol() {
        return Protocol.SOCKET;
    }

    @Override
    public SocketOperationBindingDescriptor[] getOperationBindings() {
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


    private final SocketOperationBindingDescriptor[] operations;
    
    <#list doc.operation as operation>
        private final SocketOperationBindingDescriptor ${operation.@name}Descriptor;
    </#list>

}
