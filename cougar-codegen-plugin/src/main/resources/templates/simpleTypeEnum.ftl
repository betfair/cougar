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
// Generated from simpleTypeEnum.ftl
package ${package}.${majorVersion}.enumerations;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.TranscribableEnum;

/**
  * Enumeration of valid values
 */
@SuppressWarnings("all") 
public enum ${name?cap_first} implements Externalizable, TranscribableEnum {
	<#assign first=true>
	<@compress single_line=true><#recurse doc>,UNRECOGNIZED_VALUE(null);</@compress>

    private static Set<${name?cap_first}> validValues = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(${name?cap_first}.UNRECOGNIZED_VALUE)));
    public static Set<${name?cap_first}> validValues() { return validValues; }
    
    private String value;

	private ${name?cap_first}(String value) {
		this.value=value;
	}

	private ${name?cap_first}(int id) {
		value=String.format("%04d", id);
	}
	
	public String getCode() {
		return value;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(ordinal());
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
			throw new UnsupportedOperationException();
	}
	
	public static ${className} getInstance(ObjectInput in) throws IOException, ClassNotFoundException{
		int index = in.readInt();
		if (index<0 || index>=(${className}.values().length)){
		  throw new ClassNotFoundException("Invalid enum value");
		}
		return ${className}.values()[index];
	}

    <#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
    public static final ServiceVersion SERVICE_VERSION = new ServiceVersion("${dotMajorMinorVersion}");

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }

}
<#macro value><#t>
    <#assign myName=.node.@name><#t>
    <#assign id=.node.@id><#t>
<#t>
    /**
    <#if .node.description?has_content>
     * ${.node.description?trim}
    </#if>
     */
     <#if first><#assign first=false><#else>,</#if>
    ${myName}(<#if id[0]??>${id[0]}<#else>"${myName}"</#if>) 
</#macro><#t>

<#macro @element></#macro><#t>

