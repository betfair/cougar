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
<#assign service = doc.@name>
<#assign interface = parseInterface(doc)>
<#assign className = service+"ServiceRegistrationHelper">
<#assign resolverClassName = service+"ServiceExecutableResolver">

// Generated from registrationHelper.ftl
package ${package}.${majorVersion};

import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.ExecutableResolver;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.util.AbstractRegistrationHelper;
import com.betfair.cougar.transport.api.protocol.events.EventTransport;

/**
 * Helper class for wiring together all the generated service dependencies for the ${service}Service.
 */
@SuppressWarnings("all")
public class  ${className} extends AbstractRegistrationHelper {
	private Service asyncService;
    private ${resolverClassName} resolver;
    private ServiceDefinition serviceDefinition;

    public ${className}() {
        resolver = new ${resolverClassName}();
        serviceDefinition = new ${service}ServiceDefinition();
    }

    <#if interface.events?has_content>
    //These components are only needed if you're implementing event based functionality in your app
    private EventTransport eventTransport;
    private ExecutionContext eventExecutionContext;

    public void setEventTransport(EventTransport eventTransport) {
        this.eventTransport = eventTransport;
    }

    public void setEventExecutionContext(ExecutionContext eventExecutionContext) {
        this.eventExecutionContext = eventExecutionContext;
    }

    public void subscribeToAppForEvents(ExecutionVenue ev) {
        ${service}Client ${service?uncap_first}Client = new ${service}ClientImpl(ev);

        <#list interface.events as event><#t>
        <#assign eventClassName = package + "." + version + ".events." + event.name + "Publisher"><#t>
        //Subscribe to the application to facilitate
        ${eventClassName} ${event.name?uncap_first}Publisher =
            new ${eventClassName}(${service?uncap_first}Client, eventExecutionContext, eventTransport);
        ${event.name?uncap_first}Publisher.bind(new Object[0]);
        </#list>
    }
    <#else>
    public void subscribeToAppForEvents(ExecutionVenue ev) {
    }
    </#if>

	public void setService(Service service) {
        //Check the implementation is an Async asyncService - if not wrap it
        if (service instanceof ${service}Service) {
            ${service}SynchronousAdapter adapter = new ${service}SynchronousAdapter();
            adapter.setSynchronousService((${service}Service) service);
            this.asyncService = adapter;
        } else {
            this.asyncService = service;
        }
        resolver.setService((${service}AsyncService)asyncService);
	}

    @Override
    public ExecutableResolver getApplicationExecutableResolver() {
        return resolver;
    }

    @Override
    public ServiceDefinition getServiceDefinition() {
        return serviceDefinition;
    }

    @Override
    public Service getService() {
        return asyncService;
    }
}