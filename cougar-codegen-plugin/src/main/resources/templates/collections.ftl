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
<#if javaType?starts_with("List<")>
<#t>                
        <#assign subType = javaType?substring(5, javaType?length-1)><#t>
        <#assign javaSubType = translateTypes(subType)>
        <#assign javaType="List<${javaSubType}>">
        private ${javaType} ${paramName} = null;        
        <#if !doNotWrap??>
           @XmlElementWrapper(name="${paramName}",nillable=true)
        </#if>
        <#if javaType?contains("Date")>
            @XmlJavaTypeAdapter(value=com.betfair.cougar.util.dates.XMLDateAdapter.class)
        </#if>
        @XmlElement(name="${javaSubType}",nillable=true)
<#t>                        
    <#elseif javaType?starts_with("Set<")>
<#t>                
        <#assign subType = javaType?substring(4, javaType?length-1)><#t>
        <#assign javaSubType = translateTypes(subType)>
        <#assign javaType="Set<${javaSubType}>">
        private ${javaType} ${paramName} = null;
        <#if !doNotWrap??>
	        @XmlElementWrapper(name="${paramName}",nillable=true)
        </#if>
        <#if javaType?contains("Date")>
            @XmlJavaTypeAdapter(value=com.betfair.cougar.util.dates.XMLDateAdapter.class)
        </#if>

        @XmlElement(name="${javaSubType}",nillable=true)
<#t>            
    <#elseif javaType?starts_with("Map<")>
<#t>                
        <#assign comma = javaType?index_of(",")>
        <#assign subType = javaType?substring(4, comma)><#t>
        <#assign javaSubType = translateTypes(subType)>
        <#assign valueSubType = javaType?substring(comma+1, javaType?length-1)><#t>
        <#assign valueJavaSubType = translateTypes(valueSubType)>
        <#assign mapKeyTypeArray = mapKeyTypeArray + [javaSubType]>
        <#assign mapValueTypeArray = mapValueTypeArray + [valueJavaSubType]>
        <#assign mapNameArray = mapNameArray + [param]>
<#t>
    private Map<${javaSubType},${valueJavaSubType}> ${paramName} = null;
        <#assign javaType="Map<${javaSubType},${valueJavaSubType}>">    
   		<#if doNotWrap??>
		    @XmlElement(name="entry",nillable=true)
		    private ${dataTypeName}MapEntryPair${mapNumber}${mapSuffix}[] mapAsEntries${mapNumber}${mapSuffix};
   		<#else>
	        @XmlElement(nillable=true)
	        @XmlJavaTypeAdapter(${dataTypeName}MapAdapter${mapNumber}${mapSuffix}.class)
   		</#if>
        <#assign mapNumber = mapNumber + 1>
    </#if>
