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
<#assign service = doc.@name>
// Generated from clientExecutableResolver.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.events.*;
import ${package}.${majorVersion}.enumerations.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.fault.CougarApplicationException;


import com.betfair.cougar.core.api.transports.EventTransport;
import com.betfair.cougar.core.api.transports.EventTransportMode;

import java.util.*;

/**
 * Resolves Executables for event based actions in the remote Cougar instance
 */
@SuppressWarnings("all")
public class ${service}ClientExecutableResolver implements RegisterableClientExecutableResolver {<#t>

	private volatile Map<OperationKey, Executable> executableMap = new HashMap<OperationKey, Executable>();
        
    private EventTransport eventTransport;
    private Executable defaultOperationTransport;

    
    @Override
	public void init() {
        Map<OperationKey, Executable> executableMap = new HashMap<OperationKey, Executable>();
	  	<#list doc.operation as operation>
	  	<#assign method = operation.@name><#t>
	  	<#assign parameters = "">
	  	<#assign returnType = translateTypes(operation.parameters.simpleResponse.@type)>
	  	executableMap.put(${service}ServiceDefinition.${method}Key, defaultOperationTransport); 
		</#list>

        <#list doc.event as event>
        <#assign method = "subscribeTo" + event.@name><#t>

        executableMap.put(${service}ServiceDefinition.${method}OperationKey,
            new Executable() {
                @Override
                public void execute(ExecutionContext ctx, OperationKey key,
                        Object[] args, ExecutionObserver observer,
                        ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
                            OperationDefinition opDef = executionVenue.getOperationDefinition(key);
                            eventTransport.subscribe("${event.@name}", args, observer);
                        }
                });

        </#list><#t>
        <#if doc.event?size!=0><#t>
        if (eventTransport != null) {
            eventTransport.notify(new ${service}JMSServiceBindingDescriptor(), EventTransportMode.Subscribe);
        }
        </#if><#t>
        this.executableMap = executableMap;
	}

    @Override
	public void setDefaultOperationTransport(Executable defaultOperationTransport) {
		this.defaultOperationTransport = defaultOperationTransport;
	}
	
	@Override
	public Executable resolveExecutable(OperationKey operationKey, ExecutionVenue ev) {
		return executableMap.get(operationKey);
	}

    public EventTransport getEventTransport() {
        return eventTransport;
    }

    @Override
    public void setEventTransport(EventTransport eventTransport) {
        this.eventTransport = eventTransport;
    }
}
