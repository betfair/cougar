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
<#include "common.ftl"><#t>
<#include "external.ftl"><#t>
<#include "interfaceParser.ftl"><#t>
<#assign dataTypeName = doc.@name><#t>
<#assign mandatoryCheck = "mandCheck">
<#assign mandatoryChildCheck = "">
<#assign toString = "\"\"">
<#assign mapKeyTypeArray=[]>
<#assign mapValueTypeArray=[]>
<#assign mapNameArray=[]>
<#assign mapNumber=0>
<#assign mapSuffix="">
// Generated from dataType.ftl
package ${package}.${majorVersion}.to;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import java.util.*;
import java.io.Externalizable;
import java.io.IOException;
import com.betfair.cougar.api.Result;
import com.betfair.cougar.api.Validatable;
import com.betfair.cougar.core.api.transcription.*;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.util.ValidationUtils;
import ${package}.${majorVersion}.enumerations.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 *
  <#if doc.description?has_content>
 * ${doc.description?trim}
  </#if>
 */
@XmlRootElement(name="${dataTypeName}")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
@XmlType(propOrder={})
@SuppressWarnings("all")
@JsonIgnoreProperties(ignoreUnknown=true)
public class  ${dataTypeName} implements Result, Validatable, Transcribable {
    private boolean mandCheck = false;
    private boolean sealed = false;
    private ${dataTypeName}Delegate delegate;
    public ${dataTypeName} (${dataTypeName}Delegate delegate ) {
        this();
        this.delegate = delegate;
    }
    public void seal() {
        sealed = true;
    }
<#assign paramNameTypeHash={}>
<#assign paramHash={}>

<#assign dataType=parseDataType(doc, interface)>

<#recurse doc><#t>

    public void validateMandatory() {
     <#if mandatoryCheck!="mandCheck" >
        if (${mandatoryCheck}) {
            throw new IllegalArgumentException("Mandatory attribute not set: "+this);
        }
    </#if>
     <#if validations?? >
        Set<javax.validation.ConstraintViolation<${dataTypeName}>> constraintViolations = validator.validate(this);
        for (javax.validation.ConstraintViolation<${dataTypeName}> constraintViolation : constraintViolations) {
            String message = constraintViolation.getMessage();
            throw new IllegalArgumentException(message);
        }
      </#if>
        ${mandatoryChildCheck}
    }

    public String toString() {
    	return "{"+${toString}+"}";
    }
   <#if validations?? >
    private final javax.validation.Validator validator;
    public ${dataTypeName} () {
        javax.validation.ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    <#else>
    public ${dataTypeName} () {}

    </#if>

<#if mapNameArray?size!=0>
<#list mapNameArray as map>
    <#assign mapKeyType=mapKeyTypeArray[map_index]>
    <#assign mapValueType=mapValueTypeArray[map_index]>
    <#assign mapName=map>
    <#assign mapIndex=map_index>
    <#include "mapAdapter.ftl">
</#list>
</#if>

    <#assign params="">
	<#list dataType.params as param>
	private static final Parameter __${param.paramName}Param = <@createParameterDecl param/>;
        <#assign params = params + "__" + param.paramName + "Param">
        <#if param_has_next>
            <#assign params = params + ", ">
        </#if>
    </#list>

    @XmlTransient
    @JsonIgnore
    public static final Parameter[] PARAMETERS = new Parameter[] { ${params} };

    @XmlTransient
    @JsonIgnore
    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        <#list dataType.params as param>
            <#if param.isEnumType>
        if (params.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            out.writeObject(get${param.paramName?cap_first}() != null ? get${param.paramName?cap_first}().name() : null, __${param.paramName}Param, client);
        }
        else {
            out.writeObject(get${param.paramName?cap_first}(), __${param.paramName}Param, client);
        }
            <#else>
        out.writeObject(get${param.paramName?cap_first}(), __${param.paramName}Param, client);
            </#if>
        </#list>
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        <#list dataType.params as param>
            <#if param.isEnumType>
        if (params.contains(TranscribableParams.EnumsWrittenAsStrings)) {
            setRaw${param.paramName?cap_first}Value((String)in.readObject(__${param.paramName}Param, client));
        }
        else {
            set${param.paramName?cap_first}((<@createTypeDecl param.paramType/>)in.readObject(__${param.paramName}Param, client));
        }
            <#else>
        set${param.paramName?cap_first}((<@createTypeDecl param.paramType/>)in.readObject(__${param.paramName}Param, client));
            </#if>
        </#list>
    }

    <#assign dotMajorMinorVersion = majorMinorVersion?replace("_",".")><#t>
    @XmlTransient
    @JsonIgnore
    public static final ServiceVersion SERVICE_VERSION = new ServiceVersion("${dotMajorMinorVersion}");

    @XmlTransient
    @JsonIgnore
    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ${dataTypeName})) {
            return false;
        }

        if (this == o) {
            return true;
        }
        ${dataTypeName} another = (${dataTypeName})o;

        return new EqualsBuilder()
            <#list dataType.params as param>
            .append(${param.paramName}, another.${param.paramName})
            </#list>
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            <#list dataType.params as param>
            .append(${param.paramName})
            </#list>
            .toHashCode();
    }
}


<#t>
<#t>
<#macro parameter><#t>
    <#assign param=parseParam(dataTypeName, .node, interface)><#t>
    <#assign paramName=param.paramName><#t>
    <#assign paramCapFirst=param.paramName?cap_first><#t>
    <#assign paramType=param.paramType><#t>
    <#assign isEnumType=param.isEnumType><#t>
    <#assign isMandatory=param.isMandatory><#t>

    <#if .node.validValues[0]??>
        <#assign javaType = "${dataTypeName}${paramCapFirst}Enum"><#t>
    <#else>
        <#assign javaType = translateTypes(.node.@type)><#t>
    </#if>



    // TYPE DEBUGGING FOR ${paramName}/<@createTypeDecl paramType/>
    <@parseParamDebug dataTypeName, .node, interface/>
    /**
    <#if param.description?has_content>
     * ${param.description?trim}
    </#if>
     */
    <#if javaType?starts_with("List<")>
     <#include "collections.ftl">
    <#elseif javaType?starts_with("Set<")>
     <#include "collections.ftl">
    <#elseif javaType?starts_with("Map<")>
     <#include "collections.ftl">
    <#else>
    private ${javaType} ${paramName};

    /**
    <#if param.description?has_content>
     * ${param.description?trim}
    </#if>
     */
    <#if isEnumType>
    @XmlTransient
    @JsonIgnore
    <#else>
    @XmlElement(name="${paramName}")
    </#if>
        <#if javaType=="Date">
            @XmlJavaTypeAdapter(value=com.betfair.cougar.util.dates.XMLDateAdapter.class)
        </#if>
    </#if>

     <#if .node.extensions.validations?? && (.node.extensions.validations?size > 0)>
    <#assign validations=.node.extensions.validations>
<#list validations?word_list as validation>
    @javax.validation.constraints.${validation}
</#list>
    </#if>
    public final <@createTypeDecl paramType/> get${paramCapFirst}()  {
        if (delegate != null) {
            return delegate.get${paramCapFirst}();
        }
        else {
            return ${paramName};
        }
    }

    /**
    <#if param.description?has_content>
     * ${param.description?trim}
    </#if>
     */
    public final void set${paramCapFirst}(<@createTypeDecl paramType/> ${paramName})  {
        <#if isEnumType>
        if (${paramName} == <@createTypeDecl paramType/>.UNRECOGNIZED_VALUE) {
            throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
        }
        </#if>
        if (sealed) {
            throw new IllegalStateException("This class is immutable following a call to seal()!");
        }
        if (delegate != null) {
            delegate.set${paramCapFirst}(${paramName});
        }
        else {
            this.${paramName}=${paramName};
        }
        <#if isEnumType>
        if (delegate != null) {
            delegate.setRaw${paramCapFirst}Value(${paramName} != null ? ${paramName}.name() : null);
        }
        else {
            this.raw${paramCapFirst}Value=${paramName} != null ? ${paramName}.name() : null;
        }
        </#if>
    }
    <#if isEnumType>
    private String raw${paramCapFirst}Value;

    @XmlElement(name="${paramName}")
    @JsonProperty(value = "${paramName}")
    public final String getRaw${paramCapFirst}Value()  {
        if (delegate != null) {
            return delegate.getRaw${paramCapFirst}Value();
        }
        else {
            return raw${paramCapFirst}Value;
        }
    }

    public final void setRaw${paramCapFirst}Value(String ${paramName})  {
        <@createTypeDecl paramType/> enumValue = ${paramName} != null ? EnumUtils.readEnum(<@createTypeDecl paramType/>.class, ${paramName}) : null;
        if (delegate != null) {
            delegate.set${paramCapFirst}(enumValue);
        }
        else {
            this.${paramName}=enumValue;
        }
        if (delegate != null) {
            delegate.setRaw${paramCapFirst}Value(${paramName});
        }
        else {
            this.raw${paramCapFirst}Value=${paramName};
        }
    }
    </#if>

        <#if isMandatory>
            <#assign mandatoryCheck = "${mandatoryCheck} || (get${paramCapFirst}() == null)">
        </#if>
    <#assign mandatoryChildCheck = "${mandatoryChildCheck}
        ValidationUtils.validateMandatory(get${paramCapFirst}());">
    <#assign toString = "${toString}+\"${paramName}=\"+get${paramCapFirst}()+\",\"">

</#macro><#t>

<#macro @element></#macro><#t>

