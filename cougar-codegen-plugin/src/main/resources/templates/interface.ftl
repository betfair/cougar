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
// Generated from interface.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.co.*;
import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import ${package}.${majorVersion}.exception.*;

import com.betfair.cougar.api.*;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.ev.*;

import java.util.*;

/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
@SuppressWarnings("all")
public interface  ${service}Service extends com.betfair.cougar.api.Service {<#t>
<#recurse doc><#t>
}<#t>
<#t>
<#macro operation><#t>
<#assign method = .node.@name><#t>
<#if (.node.@connected[0]!"false")?lower_case?trim=="true"><#t>
    <#assign responseType = "ConnectedResponse"><#t>
<#else>
    <#assign parsedOperation = parseOperation(.node, interface)>
    <#if parsedOperation.responseParam.isEnumType>
        <#assign responseType=parsedOperation.responseParam.paramType.javaType>
    <#else>
        <#assign responseType = translateTypes2(.node.parameters.simpleResponse.@type, doc, false, "CO", false, false)><#t>
    </#if>
</#if>

   /**
    <#if .node.description?has_content>
    * ${.node.description?trim}
    </#if>
    * @param ctx the context of the request.
      <#list .node.parameters.request.parameter as x><#t>
    * <@compress single_line=true>@param ${x.@name} ${x.description}
       <#if x.@mandatory?? && (x.@mandatory?size > 0)>
        (mandatory)
       </#if></@compress>

      </#list><#t>
    * @param expiryTime The time at which this execution request expires, at which point this call MAY return a timeout fault.
    * @return ${.node.parameters.simpleResponse.@type} ${.node.parameters.simpleResponse.description?trim}
      <#list .node.parameters.exceptions.exception as x><#t>
    * @exception ${x.@type} ${x.description?trim}
      </#list><#t>
    */
    public ${responseType} ${method} <@compress single_line=true> (
    	RequestContext ctx
	        <#recurse .node>   ;
	    </@compress>
</#macro>

<#macro parameters>
    <#recurse .node><#t>
</#macro>

<#macro request>
    <#recurse .node>
    , TimeConstraints timeConstraints)<#t>
</#macro>

<#macro exceptions>
    throws
    <#assign first=true>
    <#recurse .node>
</#macro><#t>

<#macro exception>
	<#if first><#assign first=false><#else>,</#if>
	${.node.@type}
</#macro><#t>

<#macro parameter>
    <#assign param=.node.@name>
    <#assign paramType=.node.@type>
  	<#if .node.extensions.style??>
    <#assign style = .node.extensions.style>
    </#if>
    <#assign paramCapFirst=.node.@name?cap_first><#t>
    <#assign isEnumType=.node.validValues[0]??><#t>
    <#if isEnumType>
    	<#assign javaType = "${method?cap_first}${paramCapFirst}Enum"><#t>
    <#else>
    	<#assign javaType = translateTypes(paramType)><#t>
    </#if>

    , ${javaType} ${param}
</#macro><#t>

<#macro event>
/** This allows the execution venue to subscribe to your application to facilitate publication
     * of ${.node.@name} events.  To publish an event, your application should hold on to the
     * observer passed to you by this method, and by calling onResult on that observer, an event
     * will be published.
     * @param ctx the context of the event
     * @param args the arguments passed with the subscription
     * @param executionObserver the observer to allow your application to publish events with (call executionObserver.onResult)
     */
    public void subscribeTo${.node.@name}(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver);
</#macro><#t>

<#macro @element><@compress single_line=true/></#macro><#t>
