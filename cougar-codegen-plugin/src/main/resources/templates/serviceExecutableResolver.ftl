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
// Generated from serviceExecutableResolver.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ExecutableResolver;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.impl.ev.ServiceExceptionHandlingObserver;

import java.util.*;

/**
 * Resolves Executables for all operations in the ${service}Service.
 */
@SuppressWarnings("all")
public class ${service}ServiceExecutableResolver implements ExecutableResolver {<#t>

	private Map<OperationKey, Executable> executableMap = new HashMap<OperationKey, Executable>();
	private ${service}AsyncService service;

	public ${service}ServiceExecutableResolver() {
	  	<#list doc.operation as operation>
	  	<#assign method = operation.@name><#t>
	  	<#assign parameters = "">
	  	<#assign returnType = translateTypes(operation.parameters.simpleResponse.@type)>
	
	  	executableMap.put(${service}ServiceDefinition.${method}Key, 
	  			new Executable() {
					@Override
					public void execute(ExecutionContext ctx, OperationKey key,
							Object[] args, ExecutionObserver observer,
							ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
                    ServiceExceptionHandlingObserver exceptionHandlingObserver = new ServiceExceptionHandlingObserver(observer);
		  			service.${method}((RequestContext)ctx<@compress single_line=true>
		  			<#assign argPos = 0>
		        	<#list operation.parameters.request.parameter as parameter>
				    
					    <#assign paramType=parameter.@type>
					  	<#if parameter.extensions.style??>
					    <#assign style = parameter.extensions.style>
					    </#if>
					    <#assign paramCapFirst=parameter.@name?cap_first><#t>
					    <#assign isEnumType=parameter.validValues[0]??><#t>
					    <#if isEnumType>
					    	<#assign javaType = "${method?cap_first}${paramCapFirst}Enum"><#t>
					    <#else> 
					    	<#assign javaType = translateTypes(paramType)><#t>
					    </#if>
					
					    , (${javaType})args[${argPos}]
				  		<#assign argPos = argPos + 1>
			        </#list>
			    	, exceptionHandlingObserver, timeConstraints);
				    </@compress>
				    
				}
			}); 
		</#list>
        <#list doc.event as event>
        <#assign method = "subscribeTo" + event.@name><#t>

        executableMap.put(${service}ServiceDefinition.${method}OperationKey, 
            new Executable() {
                @Override
                public void execute(ExecutionContext ctx, OperationKey key,
							Object[] args, ExecutionObserver observer,
							ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
		  			service.${method}(ctx, args, observer);

            }
        });
        </#list><#t>
	}

	public void setService(${service}AsyncService service) {
    	this.service = service;
	}

	@Override
	public Executable resolveExecutable(OperationKey operationKey, ExecutionVenue ev) {
		return executableMap.get(operationKey);
	}
  

}
