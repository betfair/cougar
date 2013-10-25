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

// Generated from soapServiceBindingDescriptor.ftl
package ${package}.${majorVersion}.soap;

import ${package}.${majorVersion}.${serviceDefinitionName};

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.ev.OperationKey;

import com.betfair.cougar.transport.api.protocol.http.soap.SoapServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapOperationBindingDescriptor;

public class ${serviceName}SoapServiceBindingDescriptor implements SoapServiceBindingDescriptor {

	private final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");
	private final String serviceName = "${serviceName}";

	public ${serviceName}SoapServiceBindingDescriptor() {
		operations = new SoapOperationBindingDescriptor[] {
			<#list doc.operation as operation>
              <#assign isConnected = (operation.@connected[0]!"false")?lower_case?trim=="true">
              <#if !isConnected>
				${operation.@name}Descriptor<#if operation_has_next>,</#if>
              </#if>
			</#list>
		};
	}

	@Override
	public String getServiceContextPath() {
		return "/${serviceName}Service/";
	}

	@Override
	public Protocol getServiceProtocol() {
		return Protocol.SOAP;
	}

	@Override
	public String getNamespacePrefix() {
		return "${serviceName?lower_case?substring(0,3)}";
	}

	@Override
	public String getNamespaceURI() {
		return "http://www.betfair.com/servicetypes/${majorVersion}/${serviceName}/";
	}

	@Override
	public SoapOperationBindingDescriptor[] getOperationBindings() {
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

    @Override
    public String getSchemaPath() {
        return "xsd/${serviceName}_${dotMajorMinorVersion}.xsd";
    }


	private final SoapOperationBindingDescriptor[] operations;
	
	<#list doc.operation as operation>
      <#assign isConnected = (operation.@connected[0]!"false")?lower_case?trim=="true">
      <#if !isConnected>
		private final SoapOperationBindingDescriptor ${operation.@name}Descriptor = new SoapOperationBindingDescriptor(${serviceDefinitionName}.${operation.@name}Key, "${operation.@name?cap_first}Request", "${operation.@name?cap_first}Response");
      <#else>
        // Method ${operation.@name} is connected. SOAP doesn't support connected operations.
      </#if>
	</#list>

}

