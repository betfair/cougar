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
private static final class ${dataTypeName}MapEntryPair${mapIndex} {
    @XmlAttribute(name="key",required=true)
    <#if mapKeyType=="Date">
        @XmlJavaTypeAdapter(value=com.betfair.cougar.util.dates.XMLDateAdapter.class)
    </#if>
    ${mapKeyType} key${mapKeyType};
    @XmlElement(name="${mapValueType}")
    <#if mapValueType=="Date">
        @XmlJavaTypeAdapter(value=com.betfair.cougar.util.dates.XMLDateAdapter.class)
    </#if>
    ${mapValueType} ${mapValueType?uncap_first}Val;

    ${dataTypeName}MapEntryPair${mapIndex}() {}

    ${dataTypeName}MapEntryPair${mapIndex}(${mapKeyType} key, ${mapValueType} value) {
        this.key${mapKeyType} = key;
        this.${mapValueType?uncap_first}Val = value;
    }
}

private static final class ${dataTypeName}MapType${mapIndex} {
    @XmlElement(name="entry")
    ${dataTypeName}MapEntryPair${mapIndex}[] entries;
}

private static final class ${dataTypeName}MapAdapter${mapIndex} extends XmlAdapter<${dataTypeName}MapType${mapIndex},Map<${mapKeyType},${mapValueType}>> {
    @Override 
    public ${dataTypeName}MapType${mapIndex}  marshal(Map<${mapKeyType},${mapValueType}> hashmap)  {
        ${dataTypeName}MapType${mapIndex} map = new ${dataTypeName}MapType${mapIndex}();
        if(hashmap!=null) {
            map.entries = new ${dataTypeName}MapEntryPair${mapIndex}[hashmap.size()];
            int i = 0;
            for (Map.Entry<${mapKeyType},${mapValueType}> entry : hashmap.entrySet()) {
              map.entries[i++] = new ${dataTypeName}MapEntryPair${mapIndex}(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
    @Override
    public Map<${mapKeyType},${mapValueType}> unmarshal(${dataTypeName}MapType${mapIndex} map)  {
        Map<${mapKeyType},${mapValueType}> hashmap = null;
        if(map!=null) {
            hashmap = new HashMap<${mapKeyType},${mapValueType}>();
            if(map.entries != null) {
                for (${dataTypeName}MapEntryPair${mapIndex} entry : map.entries)
                    hashmap.put(entry.key${mapKeyType}, entry.${mapValueType?uncap_first}Val);
            }
        }
        return hashmap;
    }

}
