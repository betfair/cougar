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
<#assign parsedInterface=parseInterface(doc)>
<#assign service = parsedInterface.serviceName>
// Generated from clientSyncServiceInterface.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import ${package}.${majorVersion}.exception.*;
import com.betfair.cougar.api.*;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.ev.*;

import java.util.*;
import java.util.concurrent.TimeoutException;


/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
@SuppressWarnings("all")
public interface  ${service}SyncClient extends com.betfair.cougar.api.Service {<#t>


<#list parsedInterface.operations as operation>
    <#if operation.connected><#t>
        <#assign responseType = "ConnectedResponse"><#t>
    <#else>
        <#if operation.responseParam.isEnumType>
            <#assign responseType="EnumWrapper<"+operation.responseParam.paramType.javaType+">">
        <#else>
            <#assign responseType = translateTypes2(operation.rawReturnType, doc, false, "CO", false, false)><#t>
        </#if>
    </#if>


  /**
     * ${operation.description?trim}.  Calls ${operation.operationName} allowing you to specify a timeout
     * @param ctx the context of the request.
    <#list operation.params as p><#t>
     * <@compress single_line=true>@param ${p.paramName} ${p.description}
     <#if p.isMandatory?? && (p.isMandatory)>
         (mandatory)
     </#if></@compress>

  </#list>
     * @param timeoutMillis - allows you to specify a timeout for this operation.  If you want a blocking call, use zero, or call the overloading without the timeout argument
     * @throws TimeoutException if call does not complete in the specified time (providing timeout > 0)
     * @throws InterruptedException if the blocking call thread was interrupted
     * @return returns ${responseType}
<#list operation.exceptions as e>
     * @throws ${e} if the remote Application threw ${e}
</#list>
     */
   public ${responseType} ${operation.operationName} (ExecutionContext ctx <@compress single_line=true>
      <#assign argsToBePassed="ctx">
      <#list operation.params as parameter>
          <#assign argsToBePassed = argsToBePassed + "," + parameter.paramName>
          , <@createTypeDecl parameter.paramType/> ${parameter.paramName}
      </#list>
          , long timeoutMillis) throws TimeoutException, InterruptedException <#t>
          <#list operation.exceptions as e><#t>
              , ${e}
          </#list>
      ; </@compress>

  /**
     * ${operation.description?trim}
     * @param ctx the context of the request.
     <#list operation.params as p><#t>
     * <@compress single_line=true>@param ${p.paramName} ${p.description}
      <#if p.isMandatory?? && (p.isMandatory)>
          (mandatory)
      </#if></@compress>

     </#list>
     * @return returns ${responseType}
   <#list operation.exceptions as e>
     * @throws ${e} if the remote Application threw ${e}
   </#list>
     */
   public ${responseType} ${operation.operationName} (ExecutionContext ctx <@compress single_line=true>
       <#list operation.params as parameter>
           , <@createTypeDecl parameter.paramType/> ${parameter.paramName}
       </#list>
           )
       <#if operation.exceptions?size!=0>
          throws
           <#list operation.exceptions as e>${e}<#if e_has_next>, </#if></#list>
       </#if>
       ; </@compress>

</#list>

  <#list parsedInterface.events as event>
  <#assign eventName = event.name?cap_first><#t>
  /**
     * This allows the execution venue to subscribe to your application to facilitate publication
     * of ${eventName} events.  To publish an event, your application should hold on to the
     * observer passed to you by this method, and by calling onResult on that observer, an event
     * will be published.
     * @param ctx the context of the event
     * @param args the arguments passed with the subscription
     * @param observer the observer to allow the application to publish events with
     */
   public void subscribeTo${eventName} (ExecutionContext ctx, Object[] args, ExecutionObserver observer);

  </#list>

}