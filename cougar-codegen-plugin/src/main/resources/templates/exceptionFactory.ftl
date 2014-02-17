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
<#include "common.ftl"><#t>
<#include "interfaceParser.ftl"><#t>
<#assign interface=parseInterface(doc)>
<#assign service = interface.serviceName>
<#assign className = service + "ExceptionFactory">
// Generated from exceptionFactory.ftl
package ${package}.${majorVersion}.exception;

import ${package}.${majorVersion}.enumerations.*;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.client.AbstractExceptionFactory;

import java.util.Map;

/**
 *
 * Exception Instantiation Factory for the ${service} app.  Will create
 * checked exceptions defined by the BSIDL
 */
@SuppressWarnings("all")
public class  ${className} extends AbstractExceptionFactory {

    public ${className}() {
        registerExceptionInstantiators();
    }

    private void registerExceptionInstantiators() {
        <#list interface.exceptions as ex>
        //Register an ExceptionInstantiator for ${ex.exceptionName}
        registerExceptionInstantiator("${ex.codePrefix}", new ExceptionInstantiator() {
            @Override
            public Exception createException(ResponseCode responseCode, String prefix, String reason, Map<String,String> exceptionParams) {
                <#assign constructorArgs=""/><#t>
                <#assign i=0/><#t>
                <#list ex.params as p><#t>
                    <#assign argName="arg" + p_index/><#t>
                    <#assign constructorArgs = constructorArgs + argName><#t>
                    <#if p_has_next><#assign constructorArgs = constructorArgs + ", "/></#if><#t>
                ${p.paramType.javaType} ${argName} = ${p.paramType.javaType}.valueOf(exceptionParams.get("${p.paramName}"));
                </#list><#t>
                return new ${ex.exceptionName}(responseCode, ${constructorArgs});
            }
        });

        </#list>
    }
}