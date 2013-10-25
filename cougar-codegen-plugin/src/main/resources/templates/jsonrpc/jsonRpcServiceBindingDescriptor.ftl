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
<#assign serviceName = doc.@name><#t>
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
// Generated from jsonRpcServiceBindingDescriptor.ftl
package ${package}.${majorVersion}.jsonrpc;


import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;

import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.jsonrpc.JsonRpcOperationBindingDescriptor;


public class ${serviceName}JsonRpcServiceBindingDescriptor implements HttpServiceBindingDescriptor {

	private final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");
	private final String serviceName = "${serviceName}";

    @Override
    public Protocol getServiceProtocol() {
        return Protocol.JSON_RPC;
    }

    @Override
    public String getServiceContextPath() {
        return "";
    }

    @Override
    public JsonRpcOperationBindingDescriptor[] getOperationBindings() {
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


    private JsonRpcOperationBindingDescriptor[] operations = {
    <#list doc.operation as operation>
      <#assign isConnected = (operation.@connected[0]!"false")?lower_case?trim=="true">
      <#if !isConnected>
        new JsonRpcOperationBindingDescriptor(new OperationKey(serviceVersion, serviceName, "${operation.@name}"))<#if operation_has_next>,</#if>
      </#if>
    </#list>
    };
}