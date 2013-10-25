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
<#macro writePrim currentType currentParam>
  <#if currentType?matches("Boolean")>
  	  out.writeBoolean(${currentParam});
  <#elseif currentType?matches("Byte")>
      out.writeByte(${currentParam});
  <#elseif currentType?matches("Integer")>
      out.writeInt(${currentParam});
  <#elseif currentType?matches("Long")>
      out.writeLong(${currentParam});
  <#elseif currentType?matches("Float")>
      out.writeFloat(${currentParam});
  <#elseif currentType?matches("Double")>
      out.writeDouble(${currentParam});
  <#elseif currentType?matches("String")>
      out.writeUTF(${currentParam});
  <#elseif currentType?matches("Date")>
      out.writeLong(${currentParam}.getTime());
  <#else>
      ${currentParam}.writeExternal(out);
  </#if>
</#macro>

<#macro readPrim currentType currentParam>
  <#if currentType?matches("Boolean")>
  	  ${currentParam} = in.readBoolean();
  <#elseif currentType?matches("Byte")>
      ${currentParam} = in.readByte();
  <#elseif currentType?matches("Integer")>
      ${currentParam} = in.readInt();
  <#elseif currentType?matches("Long")>
      ${currentParam} = in.readLong();
  <#elseif currentType?matches("Float")>
      ${currentParam} = in.readFloat();
  <#elseif currentType?matches("Double")>
      ${currentParam} = in.readDouble();
  <#elseif currentType?matches("String")>
      ${currentParam} = in.readUTF();
  <#elseif currentType?matches("Date")>
      ${currentParam} = new Date(in.readLong());
  <#else>
      ${currentParam} = ${currentType}.getInstance(in);
  </#if>
</#macro>

<#macro writeExt currentType currentParam>
    <#if currentType?starts_with("List<")>
		  out.writeInt(${currentParam}.size());
		  
		  <#assign subType = currentType?substring(5, currentType?length-1)><#t>
          <#assign javaSubType = translateTypes(subType)>
		  <#assign subParam = "sub"+currentParam><#t>
		  
		  for (${javaSubType} ${subParam}: ${currentParam}){
		    <@writeExt currentType=javaSubType currentParam=subParam />
		  }
    <#elseif currentType?starts_with("Set<")>
		  out.writeInt(${currentParam}.size());
		  
		  <#assign subType = currentType?substring(4, currentType?length-1)><#t>
          <#assign javaSubType = translateTypes(subType)>
		  <#assign subParam = "sub"+currentParam><#t>
		  
		  for (${javaSubType} ${subParam}: ${currentParam}){
		    <@writeExt currentType=javaSubType currentParam=subParam />
		  }
    <#elseif currentType?starts_with("Map<")>
		  out.writeInt(${currentParam}.size());
		  
		  <#assign comma = currentType?index_of(",")>
          <#assign subType = currentType?substring(4, comma)><#t>
          <#assign javaKeySubType = translateTypes(subType)>
		  <#assign keyParam = "key"+currentParam><#t>
		  
          <#assign valueSubType = currentType?substring(comma+1, currentType?length-1)><#t>
          <#assign valueJavaSubType = translateTypes(valueSubType)>
          <#assign valueParam = "value"+currentParam><#t>
		  
		  for (${javaKeySubType} ${keyParam}: ${currentParam}.keySet()){
		    ${valueJavaSubType} ${valueParam} = ${currentParam}.get(${keyParam});
		    <@writeExt currentType=javaKeySubType currentParam=keyParam />
		    <@writeExt currentType=valueJavaSubType currentParam=valueParam />
		  }
    <#else>
      <@writePrim currentType=currentType currentParam=currentParam/>
    </#if>
</#macro>


<#macro readExt currentType currentParam>
    <#if currentType?starts_with("List<")>
		  <#assign subType = currentType?substring(5, currentType?length-1)><#t>
          <#assign javaSubType = translateTypes(subType)>
		  <#assign subParam = "sub"+currentParam><#t>
		  
		  int size = in.readInt();
		  ${currentParam} = new ArrayList<${javaSubType}>(size);
		  
		  for (int i=0;i<size;i++){
		  	${javaSubType} ${subParam} = null;
		    <@readExt currentType=javaSubType currentParam=subParam />
		    ${currentParam}.add(${subParam});
		  }
    <#elseif currentType?starts_with("Set<")>
		  <#assign subType = currentType?substring(4, currentType?length-1)><#t>
          <#assign javaSubType = translateTypes(subType)>
		  <#assign subParam = "sub"+currentParam><#t>
		  
		  int size = in.readInt();
		  ${currentParam} = new HashSet<${javaSubType}>(size);
		  
		  for (int i=0;i<size;i++){
		  	${javaSubType} ${subParam} = null;
		    <@readExt currentType=javaSubType currentParam=subParam />
		    ${currentParam}.add(${subParam});
		  }
    <#elseif currentType?starts_with("Map<")>
		  <#assign comma = currentType?index_of(",")>
          <#assign subType = currentType?substring(4, comma)><#t>
          <#assign javaKeySubType = translateTypes(subType)>
		  <#assign keyParam = "key"+currentParam><#t>

          <#assign valueSubType = currentType?substring(comma+1, currentType?length-1)><#t>
          <#assign valueJavaSubType = translateTypes(valueSubType)>
          <#assign valueParam = "value"+currentParam><#t>
          
		  int size = in.readInt();
		  ${currentParam} = new HashMap<${javaKeySubType},${valueJavaSubType}>();
		  
		  for (int i=0;i<size;i++){
		  	${javaKeySubType} ${keyParam}=null;
		    ${valueJavaSubType} ${valueParam}=null;
		    <@readExt currentType=javaKeySubType currentParam=keyParam />
		    <@readExt currentType=valueJavaSubType currentParam=valueParam />
		    ${currentParam}.put(${keyParam},${valueParam});
		  }
    <#else>
      <@readPrim currentType=currentType currentParam=currentParam/>
    </#if>
</#macro>
