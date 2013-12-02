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
// Generated from asyncServiceInterface.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import com.betfair.cougar.api.*;
import com.betfair.cougar.core.api.ev.*;

import java.util.*;

/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
 
@SuppressWarnings("all")
public interface  ${service}AsyncService extends com.betfair.cougar.api.Service {<#t>
<#recurse doc><#t>
}<#t>
<#t>
<#macro operation><#t>
<#assign method = .node.@name><#t>
<#assign methodArgs = "new RequestContext(request)"><#t>

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
    * @param observer notified by the application of the result or any exception that occurred during execution
    */
    public void ${method} <@compress single_line=true> (
    	RequestContext ctx <#recurse .node> , ExecutionObserver observer) ;
	    </@compress>

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
    * @param observer notified by the application of the result or any exception that occurred during execution
    * @param expiryTime The time at which this execution request expires, at which point this call MAY return a timeout fault.
    */
    public void ${method} <@compress single_line=true> (
    	RequestContext ctx <#recurse .node> , ExecutionObserver observer, TimeConstraints timeConstraints) ;
	    </@compress>
</#macro><#t>

<#macro parameters>
    <#recurse .node>
</#macro><#t>

<#macro request>
    <#recurse .node>
</#macro><#t>

<#macro simpleResponse>
    <#recurse .node>
</#macro><#t>

<#macro simpleResponse>
    <#assign paramType=.node.@type>
    <#assign observerParameterType = translateTypes(paramType)><#t>
    <#if observerParameterType="void">
        <#assign observerParameterType="Void">
    <#else>
    </#if>
</#macro>

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
    <#assign methodArgs = methodArgs + ","+ param>
</#macro>

<#macro event>
 /**
     * This allows the execution venue to subscribe to your application to facilitate publication
     * of ${.node.@name} events.  To publish an event, your application should hold on to the
     * observer passed to you by this method, and by calling onResult on that observer, an event
     * will be published.
     * @param ctx the context of the event
     * @param args the arguments passed with the subscription
     * @param executionObserver the observer to allow the application to publish events with
     */
    public void subscribeTo${.node.@name}(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver);
</#macro><#t>

<#macro @element><#t>
</#macro><#t>
