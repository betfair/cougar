/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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
// Generated from client.ftl
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
import com.betfair.cougar.core.api.ev.*;
<@compress single_line=true>
<#include "common.ftl">
<#include "interfaceParser.ftl">
<#assign operations=parseOperations(doc)>

<#assign service = doc.@name>
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>

@SuppressWarnings("all")
</@compress>
@WebService(serviceName="${service}Service",portName="${service}Service",targetNamespace="http://www.betfair.com/serviceapi/${dotMajorMinorVersion}/${service}")
@IDLService(name="${service}", version="${dotMajorMinorVersion}")
public interface ${service}Client extends com.betfair.cougar.api.Service {<#t>

<#list operations as operation>
	<@compress single_line=true>
	public void ${operation.operationName}(
			ExecutionContext ctx,
			<#list operation.params as param>
				<@createTypeDecl param.paramType/> ${param.paramName},
			</#list>
			ExecutionObserver obs);
</@compress>

	<@compress single_line=true>public void ${operation.operationName}(
		ExecutionContext ctx,
		<#list operation.params as param>
			<@createTypeDecl param.paramType/> ${param.paramName},
		</#list>
		ExecutionObserver obs, long timeoutMillis);
</@compress>

</#list>

<#list doc.event as event><#t>
<#assign eventClassName = event.@name?cap_first><#t>
<@compress single_line=true>
    public void subscribeTo${eventClassName}(ExecutionContext ctx,
                                                      Object[] args,
                                                      ExecutionObserver executionObserver);</@compress>

</#list>



}<#t>