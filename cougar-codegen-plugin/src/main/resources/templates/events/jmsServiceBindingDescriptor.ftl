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
<#include "../common.ftl"><#t>
<#include "../interfaceParser.ftl"><#t>
<#assign interface = parseInterface(doc)><#t>
<#assign serviceName = interface.serviceName><#t>
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
// Generated from epnServiceBindingDescriptor.ftl
package ${package}.${majorVersion}.events;

import java.util.ArrayList;
import java.util.List;


import ${package}.${majorVersion}.${serviceName}ServiceDefinition;

import com.betfair.cougar.core.api.ServiceVersion;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.protocol.events.EventServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.jms.JMSEventBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.jms.JMSParamBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.jms.JMSParamBindingDescriptor.ParamSource;

public class ${serviceName}JMSServiceBindingDescriptor implements EventServiceBindingDescriptor {

	private final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");
	private final String serviceName = "${serviceName}";

	public ${serviceName}JMSServiceBindingDescriptor() {
		<#list interface.events as event>
		List<JMSParamBindingDescriptor> ${event.name?uncap_first}ParamBindings = new ArrayList<JMSParamBindingDescriptor>();
            <#list event.params as param>
		${event.name?uncap_first}ParamBindings.add(new JMSParamBindingDescriptor("${param.paramName}", ParamSource.${param.paramStyle?upper_case}));
			</#list>
		${event.name?uncap_first}EventDescriptor = new JMSEventBindingDescriptor("${event.name}", ${event.name?uncap_first}ParamBindings, ${event.name?cap_first}.class);

		</#list>
		events = new JMSEventBindingDescriptor[] {
			<#list interface.events as event>
				${event.name?uncap_first}EventDescriptor<#if event_has_next>,</#if>
			</#list>
		};
	}

    @Override
    public String getServiceName() {
        return "${serviceName}";
    }

    @Override
    public String getServiceNamespace() {
        return "${package}";
    }

    @Override
    public ServiceVersion getServiceVersion() {
        return serviceVersion;
    }

	
	@Override
	public JMSEventBindingDescriptor[] getEventBindings() {
		return events;
	}

    @Override
    public Protocol getServiceProtocol() {
        return Protocol.JMS;
    }

	private final JMSEventBindingDescriptor[] events;
	
	<#list interface.events as event>
    private final JMSEventBindingDescriptor ${event.name?uncap_first}EventDescriptor;
	</#list>

}

