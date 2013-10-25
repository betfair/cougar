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
<#assign service = doc.@name><#t>
<#assign prefix = doc.@prefix><#t>
// Generated from exceptionCode.ftl
package ${package}.${majorVersion}.exception;

/**
  * Valid error codes for ${service} 
 */
@SuppressWarnings("all")
public enum ${service}Code {
	<#recurse doc><#t>
    
    private String errorCode;

	private ${service}Code(int code) {
		errorCode="${prefix}-"+String.format("%04d", code);
	}
	
	public String getCode() {
		return errorCode;
	}
}
<#macro validCodes><#t>
    <#recurse .node><#t>
    ;
</#macro><#t>

<#macro code><#t>
    <#assign name=.node.@name><#t>
    <#assign id=.node.@id><#t>
<#t>
    /**
     * ${.node?trim}
     */
    ${name}(${id}), 
</#macro>

<#macro @element></#macro><#t>

