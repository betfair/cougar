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
<#include "common.ftl">
<#include "interfaceParser.ftl">
<#assign parsedInterface=parseInterface(doc)>
<#assign service = parsedInterface.serviceName><#t>
// Generated from clientSyncServiceImpl.ftl
package ${package}.${majorVersion};

import ${package}.${majorVersion}.to.*;
import ${package}.${majorVersion}.enumerations.*;
import ${package}.${majorVersion}.exception.*;
import com.betfair.cougar.api.*;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.CougarInternalOperations;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;

import com.betfair.tornjak.monitor.MonitorRegistry;

import java.util.*;
import java.util.concurrent.TimeoutException;

<#macro fromStringSimpleType t>
    <#local type=t.paramType.javaType/>
    <#if type="Long">
        Long.valueOf<#t>
    <#elseif type="Integer">
        Integer.valueOf<#t>
    <#elseif type="Byte">
        Byte.valueOf<#t>
    <#elseif type="String">
    <#elseif type="Float">
        Float.valueOf<#t>
    <#elseif type="Double">
        Double.valueOf<#t>
    <#elseif type="Boolean">
        Boolean.valueOf<#t>
    <#elseif type="Date">
        new java.text.SimpleDateFormat().parse<#t>
    <#elseif type?ends_with("Enum")>
        ${type}.valueOf<#t>
    </#if><#t>
</#macro>


/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
<@compress single_line=true>
<#include "common.ftl">
<#include "interfaceParser.ftl">
<#assign operations=parseOperations(doc)>
<#assign service = doc.@name>
<#assign serviceDefinitionName = service+"ServiceDefinition">
<#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
@SuppressWarnings("all")
</@compress>
public class  ${service}SyncClientImpl implements ${service}SyncClient {<#t>

    private static final ServiceVersion serviceVersion = new ServiceVersion("${dotMajorMinorVersion}");

    private ExecutionVenue ev;
    private String namespace;

    /**
     * Protected constructor and setters for backward compatibility
     */
    protected ${service}SyncClientImpl() {
    }

    protected void setEv(ExecutionVenue ev) {
        this.ev = ev;
    }
    protected void setNamespace(String namespace) {
        if (namespace == null || "".equals(namespace)) {
            throw new IllegalArgumentException("Namespace must be a non-empty string: "+namespace);
        }
        this.namespace = namespace;
    }

    public ${service}SyncClientImpl(ExecutionVenue ev) {
        this(ev, CougarInternalOperations.COUGAR_IN_PROCESS_NAMESPACE);
    }

    public ${service}SyncClientImpl(ExecutionVenue ev, String namespace) {
        setEv(ev);
        setNamespace(namespace);
    }

    private OperationKey getOperationKey(OperationKey key) {
        return namespace == null ? key : new OperationKey(key, namespace);
    }

  <#list parsedInterface.operations as operation>
  <#assign executionResultType = operation.operationName?cap_first+"ExecutionResult">
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
  * @return returns ${responseType}
  * @throws TimeoutException if call does not complete in the specified time (providing timeout > 0)
  * @throws InterruptedException if the blocking call thread was interrupted
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
	    { </@compress>
	    <@compress single_line=true><#if operation.returnType.javaType!="void">return </#if>${operation.operationName}(${argsToBePassed},
                   DefaultTimeConstraints.fromTimeout(timeoutMillis));</@compress>
  }

 /**
  * ${operation.description?trim}.  Calls ${operation.operationName} allowing you to specify a timeout
  * @param ctx the context of the request.
    <#list operation.params as p><#t>
  * <@compress single_line=true>@param ${p.paramName} ${p.description}
     <#if p.isMandatory?? && (p.isMandatory)>
     	(mandatory)
     </#if></@compress>

    </#list>
  * @param timeConstraints - allows you to specify time constraints for this operation.  If you want a blocking call,
  *                          use DefaultTimeConstraints.NO_CONSTRAINTS, or call the overloading without the timeout argument
  * @return returns ${responseType}
  * @throws TimeoutException if call does not complete in the specified time (providing timeout > 0)
  * @throws InterruptedException if the blocking call thread was interrupted
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
            , TimeConstraints timeConstraints) throws TimeoutException, InterruptedException <#t>
            <#list operation.exceptions as e><#t>
                , ${e}
            </#list>
	    { </@compress>

        final WaitingObserver observer = new WaitingObserver();

        ev.execute(ctx,
                   getOperationKey(${serviceDefinitionName}.${operation.operationName}Key),
                   <@compress single_line=true>new Object[] {
                   <#assign firstObjectArrayArg=true>
                   <#list operation.params as param>
                        <#if firstObjectArrayArg><#assign firstObjectArrayArg=false><#else>,</#if>
                        ${param.paramName}
                   </#list>
                   },</@compress>
                   observer,
                   timeConstraints);

        if (!observer.await(timeConstraints)) {
            throw new CougarClientException(ServerFaultCode.Timeout, "Operation ${operation.operationName} timed out!");
        }

        final ExecutionResult er = observer.getExecutionResult();
        switch (er.getResultType()) {
            case Success:
                <#if operation.returnType.javaType!="void">
                return (${responseType}) er.getResult();
                <#else>
                return;
                </#if>

            case Fault:
                CougarException cex = er.getFault();

                if (cex.getServerFaultCode() == ServerFaultCode.ServiceCheckedException) {
                    List<String[]> exceptionParams = cex.getFault().getDetail().getFaultMessages();
                    String className = cex.getFault().getDetail().getDetailMessage();
                    <#assign i=0>
                    <#list operation.exceptions as exception>
                        <#if i=0>
                    if (className.equals("${exception}")) {
                        <#else>
                    else if (className.equals("${exception}")) {
                        </#if>
                        <#assign theException = parsedInterface.exceptionMap[exception]/>
                        throw new ${exception}(
                                                cex.getResponseCode(),
                                               <#assign e=0><#t>
                                               <#list theException.params as exp><#t>
                                                   <@fromStringSimpleType exp/>(exceptionParams.get(${e})[1])<#if exp_has_next>, </#if>
                                                   <#assign e=e+1><#t>
                                               </#list>
                                              );
                    }

                        <#assign i=i+1>
                    </#list>
                    <#if (i>0)>
                    else {
                        throw new IllegalArgumentException("An unanticipated exception was received of class [" + className + "]");
                    }
                    <#else>
                    throw new CougarClientException(ServerFaultCode.ServiceCheckedException, "Unknown checked exception received", cex);
                    </#if>
                } else if (cex instanceof CougarFrameworkException) {
                    CougarFrameworkException cfe = (CougarFrameworkException) cex;
                    throw new CougarClientException(cfe.getServerFaultCode(), cfe.getMessage(), cfe.getCause());
                } else {
                  throw cex;
                }
            default:
                throw new IllegalArgumentException("The Server returned an illegal result type [" + er.getResultType() + "]");
        }
  }

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
   */
   @Override
   public ${responseType} ${operation.operationName} (ExecutionContext ctx <@compress single_line=true>
         <#list operation.params as parameter>
             , <@createTypeDecl parameter.paramType/> ${parameter.paramName}
         </#list>
             )
         <#if operation.exceptions?size!=0>
            throws
             <#list operation.exceptions as e>${e}<#if e_has_next>, </#if></#list>
         </#if>
         { </@compress>
        try {
            <#if operation.returnType.javaType!="void">return </#if>${operation.operationName}(${argsToBePassed}, DefaultTimeConstraints.NO_CONSTRAINTS);
        } catch (TimeoutException ex) {
            //blocking call, so won't happen
        } catch (InterruptedException interrupted) {
            throw new RuntimeException("Operation ${operation.operationName} was interrupted!");
        }
        <#if operation.returnType.javaType != "void">return null;</#if>
   }



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
    public void subscribeTo${eventName} (ExecutionContext ctx, Object[] args, ExecutionObserver observer) {
        ev.execute(ctx, getOperationKey(${serviceDefinitionName}.subscribeTo${eventName}OperationKey), args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);
    }
    </#list>

    public void init(ContainerContext cc) {
        throw new UnsupportedOperationException("Not valid for CLIENT");
    }

}
