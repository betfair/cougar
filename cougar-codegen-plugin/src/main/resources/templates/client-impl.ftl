/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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
// Generated from client-impl.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import ${package}.${majorVersion}.exception.*;
import com.betfair.cougar.api.*;

import com.betfair.cougar.api.annotations.*;

import javax.annotation.Resource;
import javax.jws.*;
import javax.xml.ws.*;
import javax.xml.ws.handler.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import java.util.*;
import java.util.concurrent.Executor;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.impl.CougarInternalOperations;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;

<@compress single_line=true>
<#include "common.ftl">
<#include "interfaceParser.ftl">
<#assign operations=parseOperations(doc)>
<#assign service = doc.@name>
<#assign serviceDefinitionName = service+"ServiceDefinition">
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
@SuppressWarnings("all")
</@compress>
@WebService(serviceName="${service}Service",portName="${service}Service",targetNamespace="http://www.betfair.com/serviceapi/${dotMajorMinorVersion}/${service}")
@IDLService(name="${service}", version="${dotMajorMinorVersion}")
public class ${service}ClientImpl implements ${service}Client {<#t>

	private final ExecutionVenue ev;
    private final Executor executor;
    private final String namespace;
	private static final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");

	public ${service}ClientImpl(ExecutionVenue ev, Executor executor) {
		this(ev, executor, CougarInternalOperations.COUGAR_IN_PROCESS_NAMESPACE);
    }<#t>

    public ${service}ClientImpl(ExecutionVenue ev, Executor executor, String namespace) {
        if (namespace == null || "".equals(namespace)) {
            throw new IllegalArgumentException("Namespace must be a non-empty string: "+namespace);
        }
        this.ev = ev;
        this.executor = executor;
        this.namespace = namespace;
    }<#t>


    private OperationKey getOperationKey(OperationKey key) {
        return namespace == null ? key : new OperationKey(key, namespace);
    }

    private void execute(final ExecutionContext ctx, final OperationKey operationKey,
                         final Object[] args, final ExecutionObserver observer, final TimeConstraints timeConstraints) {

        final ExecutionObserver wrappedObserver = new ExecutionObserver() {
            @Override
            public void onResult(final ExecutionResult executionResult) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        observer.onResult(executionResult);
                    }
                });
            }
        };

        ev.execute( ctx,
                    operationKey,
                    args,
                    wrappedObserver,
                    executor,
                    timeConstraints);
    }

<#list operations as operation>
	<@compress single_line=true>public void ${operation.operationName}(
		ExecutionContext ctx,
		<#list operation.params as param>
			<@createTypeDecl param.paramType/> ${param.paramName},
		</#list>
		ExecutionObserver obs)

		{<#t>
</@compress>
        <@compress single_line=true>${operation.operationName}(ctx,
		<#list operation.params as param>
			${param.paramName},
		</#list>
		obs, 0L);
</@compress>
    }

	<@compress single_line=true>public void ${operation.operationName}(
		ExecutionContext ctx,
		<#list operation.params as param>
			<@createTypeDecl param.paramType/> ${param.paramName},
		</#list>
		ExecutionObserver obs, long timeoutMillis)

		{<#t>
</@compress>


		<@compress single_line=true>execute(ctx,
			getOperationKey(${serviceDefinitionName}.${operation.operationName}Key),
			new Object[] {
			<#assign firstObjectArrayArg=true>
			<#list operation.params as param>
				<#if firstObjectArrayArg><#assign firstObjectArrayArg=false><#else>,</#if>
				${param.paramName}
			</#list>
			},
			obs, DefaultTimeConstraints.fromTimeout(timeoutMillis)
			);
	</@compress>

	}

<#t>
</#list><#t>

<#list doc.event as event><#t>
<#assign eventClassName = event.@name?cap_first><#t>
    public void subscribeTo${eventClassName}(ExecutionContext ctx, Object[] args, ExecutionObserver obs) {
        execute(ctx, getOperationKey(${serviceDefinitionName}.subscribeTo${event.@name?cap_first}OperationKey), args, obs, DefaultTimeConstraints.NO_CONSTRAINTS);
    }

</#list>


	public void init(ContainerContext cc) {
		throw new UnsupportedOperationException("Not valid for CLIENT");
	}
}<#t>