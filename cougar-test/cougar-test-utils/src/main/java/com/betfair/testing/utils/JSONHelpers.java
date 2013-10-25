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

package com.betfair.testing.utils;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Iterator;

public class JSONHelpers {
	
	private XMLHelpers xHelpers = new XMLHelpers();

	public JSONObject convertXMLDocumentToJSONObjectRemoveRootElement(Document document) {

		try {
			
			/*
			 * We do not want to convert the root node to JSON, so get it, remove it's
			 * attributes, then when we return the JSONObject we can just get
			 * the JSONObject beneath the root. 
			 * 
			 * If we leave the attributes present then they will be included
			 * in the JSONObject beneath the root.
			 */ 
			if (document == null) {
				return null;
			} else {
				Node rootNode = document.getDocumentElement();
				NamedNodeMap namedNodeMap = rootNode.getAttributes();
				Integer numberOfAtts = Integer.valueOf(namedNodeMap.getLength());
				for (int i = 0; i < numberOfAtts; i++) {
					Node attributeNode = namedNodeMap.item(0);
					Element rootNodeAsElement = (Element)rootNode;
					rootNodeAsElement.removeAttribute(attributeNode.getNodeName());
				}
	
				String rootName = rootNode.getNodeName();
				
				String xmlString = xHelpers.getXMLAsString(document);
				
			
				JSONObject initialJSONObject = XML.toJSONObject(xmlString);
                String initialJSONString = initialJSONObject.toString();
                // try to take out dodgy null element conversions
//                initialJSONString = initialJSONString.replace("{\"xsi:nil\":true,\"xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"},","null,");
//                initialJSONString = initialJSONString.replace(",{\"xsi:nil\":true,\"xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}",",null");
//                initialJSONString = initialJSONString.replace(":{\"xsi:nil\":true,\"xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}","null");
                initialJSONString = initialJSONString.replace("{\"xsi:nil\":true,\"xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}","null");
                initialJSONObject = new JSONObject(initialJSONString);

				JSONObject returnJSONObject;
				//if the result contains only one string value (like enum response)
				if (!initialJSONObject.get(rootName).getClass().equals(JSONObject.class)){
					//create a Json object by adding a custom "response" key 
					returnJSONObject = new JSONObject().put("response",  initialJSONObject.get(rootName));
				}
//				else if(!initialJSONObject.get(rootName).getClass().equals(JSONObject.class)){
//					returnJSONObject = new JSONObject().put(initialJSONObject.get(rootName).getClass().getSimpleName(), initialJSONObject.get(rootName));
//					
//				}
				else{
					returnJSONObject = (JSONObject)initialJSONObject.get(rootName);
				}
				
				//When converting from XML \n get set ro \r\n so fixing
				String jString = returnJSONObject.toString();
				jString = jString.replace("\\r\\n", "\\n");
				returnJSONObject = new JSONObject(jString);
				
				return returnJSONObject;
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}
	
	public void removeJSONObjectHoldingSameTypeList(JSONObject jObject) {
		try {
			if (jObject != null) {
				for (Iterator iter = jObject.keys(); iter.hasNext();) {
					String key = iter.next().toString();
					Object containedObject = jObject.get(key);
					if (containedObject instanceof JSONObject) {
						iterateAndProcessJSONbjectSameTypeList(jObject, key, (JSONObject)containedObject);
					} else if (containedObject instanceof JSONArray) {
						iterateAndProcessJSONArraySameTypeList((JSONArray)containedObject);
					}
				}	
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void iterateAndProcessJSONbjectSameTypeList(JSONObject jParentObject, String passedKey, JSONObject jObject) throws JSONException {
		if ((jObject!=null) && (jObject.length() == 1)) {
			String[] keyNames = JSONObject.getNames(jObject);
			String key = keyNames[0];
			Object containedObject = jObject.get(key);
			if (containedObject instanceof JSONArray) {
				jParentObject.put(passedKey, (JSONArray)containedObject);
			}
		}
		
		if(jObject != null){
			for (Iterator iter = jObject.keys(); iter.hasNext();) {
				String key = iter.next().toString();
				Object containedObject = jObject.get(key);
				if (containedObject instanceof JSONObject) {
					iterateAndProcessJSONbjectSameTypeList(jObject, key, (JSONObject)containedObject);
				} else if (containedObject instanceof JSONArray) {
					iterateAndProcessJSONArraySameTypeList((JSONArray)containedObject);
				}
			}	
		}
	}
	
	private void iterateAndProcessJSONArraySameTypeList(JSONArray jArray) throws JSONException {
		for (int i = 0; i < jArray.length(); i++) {
			Object containedObject = jArray.get(i);
			if (containedObject instanceof JSONObject) {
				iterateAndProcessJSONbjectSameTypeList(null, "ARRAY",(JSONObject)containedObject);
			} else if (containedObject instanceof JSONArray) {
				iterateAndProcessJSONArraySameTypeList((JSONArray)containedObject);
			}
		}
	}
	
	public JSONObject parseJSONObjectFromJSONString(String jsonString) throws JSONException {
		return new JSONObject(jsonString);
	}
	
	public JSONObject createAsJSONObject(JSONObject jObject) {
		return jObject;
	}

}
