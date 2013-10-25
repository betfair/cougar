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

package com.betfair.testing.utils.cougar.misc;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectUtil {
	public static boolean isAHashMapArray(Class o) {
		
		HashMap[] comparisonObject = new HashMap[0];
		if (o.toString().equalsIgnoreCase(comparisonObject.getClass().toString())) {
				return true;
		}
		return false;
	}
	
	public static boolean isALinkedHashMapArray(Class o) {
		
		LinkedHashMap[] comparisonObject = new LinkedHashMap[0];
		if (o.toString().equalsIgnoreCase(comparisonObject.getClass().toString())) {
				return true;
		}
		return false;
	}

	public static boolean isMapArray(Class o) {
		Class<?> componentType = o.getComponentType();
		return Map.class.isAssignableFrom(componentType);
	}
	
	public static boolean isJavaDotLangObjectArray(Class o) {
		if (o.isArray()) {
			if (isPrimitiveArray(o)) {
				return true;
			} else if (o.toString().contains("java.lang")) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean isPrimitiveArray(Class o) {
		if ((o.isArray()) && (o.getComponentType().isPrimitive())) {
			return true;
		} else if ((o.isArray()) && (o.getComponentType().isArray())) {
			return isPrimitiveArray(o.getComponentType());
		}
		return false;
	}
	
	public static boolean isAHashMap(Class o) {
		return o == HashMap.class;
	}

	public static boolean isALinkedHashMap(Class o) {
		return o == LinkedHashMap.class;
	}

	public static boolean isJavaDotLangObject(Class o) {
		if ((o.toString().startsWith("class java.lang")) || (o.toString().equalsIgnoreCase("class java.util.Date"))) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isTimeStamp(Class o) {
		return o.toString().equals("class java.sql.Timestamp");
	}
	
	public static boolean isString(Class o) {
		return o.toString().equals("class java.lang.String");
	}

	public static boolean isArray(Class o) {
		return o.isArray();
	}

	public static boolean isMap(String className) {
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return Map.class.isAssignableFrom(clazz);
	}

	public static boolean isLinkedHashMap(String name) {
		if (name.equals("java.util.LinkedHashMap")) {
			return true;
		}
		return false;
	}

	public static DataTypeEnum resolveType(Object o) throws AssertionError {
		if (o == null) {
			throw new IllegalStateException("Unable to handle null");
		}
		return resolveType(o.getClass());
	}
	
	public static boolean isDocument(Class clazz) throws AssertionError {
		/*if ((clazz.getName().equalsIgnoreCase("org.w3c.dom.Document")) || 
				(clazz.getName().equalsIgnoreCase("com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl")) || 
				(clazz.getName().equalsIgnoreCase("org.apache.xerces.dom.DeferredDocumentImpl")) ||
				(clazz.getName().equalsIgnoreCase("org.apache.xerces.dom.DocumentImpl")) ||
				(clazz.getName().equalsIgnoreCase("com.sun.org.apache.xerces.internal.dom.DocumentImpl"))) {
			return true;
		} else {
			return false;
		}*/
		
		final String interfaceName = "org.w3c.dom.Document";
		Class<?> behaviour;
		try{
		behaviour = Class.forName(interfaceName);
		}catch(Exception e){
			throw new AssertionError("Cannot resolve interface class using : " + interfaceName);
		}
		
		return checkBehaviour(clazz, behaviour);
		
	}
	
	public static boolean isHTMLDocument(Class clazz) throws AssertionError {
		if (clazz.getName().equalsIgnoreCase("org.w3c.dom.html.HTMLDocument")){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isJSONObject(Class o) {
		JSONObject comparisonObject = new JSONObject();
		if (o.toString().equalsIgnoreCase(comparisonObject.getClass().toString())) {
				return true;
		}
		return false;
	}
	
	public static boolean isList(Class o) {
		return List.class.isAssignableFrom(o);
	}
	
	public static boolean isThrowableObject(Class o) throws AssertionError {
		return Throwable.class.isAssignableFrom(o);
	}
	
	public static boolean isExceptionObject(Class o) throws AssertionError {
		
		/*if (o.getName().equalsIgnoreCase("java.lang.Exception")||
				o.getName().equalsIgnoreCase("javax.xml.soap.SOAPException")||
				o.getName().equalsIgnoreCase("com.sun.xml.messaging.saaj.SOAPExceptionImpl")
				||o.getName().equalsIgnoreCase("com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl")) {
				return true;
		}
		return false;*/
		
		final String behaviourString = "java.lang.Exception";
		Class<?> behaviour;
		try {
			behaviour = Class.forName(behaviourString);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"Unable to determine interface behaviour from : "
							+ behaviourString, e);
		}
		
		return checkBehaviour(o, behaviour);
	}
	
	public static boolean isStandard(Class o) {
		if (o.isPrimitive()) {
			return true;
		}
		if (isWrappedPrimitive(o)) {
			return true;
		}
		if (o.getName().equals("java.lang.String")) {
			return true;
		}
		if (o.getName().equals("java.math.BigDecimal")) {
			return true;
		}
		return false;
	}
	
	public static boolean isWrappedPrimitive(Class<?> clazz) {
        return clazz.equals(Boolean.class) || 
                clazz.equals(Integer.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Float.class);
	}
	
	public static DataTypeEnum resolveType(Class clazz) throws AssertionError {
		
		DataTypeEnum dataType = null;
		
		if (clazz.isArray()) {
			if (isAHashMapArray(clazz)) {
				dataType = DataTypeEnum.HASHMAPARRAY;
			} else if (isALinkedHashMapArray(clazz)) {
				dataType = DataTypeEnum.LINKEDHASHMAPARRAY;
			} else if (isMapArray(clazz)) {
				dataType = DataTypeEnum.HASHMAPARRAY;
			} else if (ObjectUtil.isJavaDotLangObjectArray(clazz)) {
				dataType = DataTypeEnum.ARRAY;
			} else {
				dataType = DataTypeEnum.JAVABEAN_ARRAY;
			}
		} else {
			if (isAHashMap(clazz)) {
				dataType = DataTypeEnum.HASHMAP;
			} else if (isString(clazz)) {
				dataType = DataTypeEnum.STRING;
			} else if (isALinkedHashMap(clazz)) {
				dataType = DataTypeEnum.LINKEDHASHMAP;
			} else if (isMap(clazz.getName())) {
				dataType = DataTypeEnum.HASHMAP;
			} else if (isTimeStamp(clazz)) {
				dataType = DataTypeEnum.TIMESTAMP;
			} else if (isDocument(clazz)) {
				dataType = DataTypeEnum.DOCUMENT;			
			} else if (isJSONObject(clazz)) {
				dataType = DataTypeEnum.JSONOBJECT;
			} else if(isThrowableObject(clazz)){
				dataType = DataTypeEnum.THROWABLE;
			} else if(isExceptionObject(clazz)){
				dataType = DataTypeEnum.EXCEPTION;
			} else if(isList(clazz)){
				dataType = DataTypeEnum.LIST;
			} else if(isInputStream(clazz)){
				dataType = DataTypeEnum.INPUTSTREAM;
			} else if (isJavaDotLangObject(clazz)) {
				dataType = DataTypeEnum.JAVA_DOT_LANG_OBJECT;			
			} else if(isStandard(clazz)){
				dataType = DataTypeEnum.STANDARD;
            } else if(isSet(clazz)){
                dataType = DataTypeEnum.SET;
            }

            else {
				if (clazz.isEnum()) {
					dataType = DataTypeEnum.ENUM;
				} else {
					// must be a bean...
					dataType =  DataTypeEnum.BEAN;
				}
			}
		}
		if (dataType == null) {
			throw new IllegalStateException("Don't know how to handle:" + clazz.getName());
		}
		
		return dataType;
	}
	
	
	/**
	 * Check that the specified class implements a given interface or extends a given parent class as the behaviour
	 * 
	 * @param clazz
	 * @param behaviour
	 * @return
	 */
	public static boolean checkBehaviour(Class<?> clazz, Class<?> behaviour) {

		boolean behaviourFlag = false;
		
		while (clazz != null) {
			
			if (clazz == behaviour) {
				behaviourFlag = true;
				break;
			}
			else {

				Class<?>[] interfaces = clazz.getInterfaces();

				for (Class<?> cls : interfaces) {
					if (cls == behaviour) {
						behaviourFlag = true;
						break;
					}
				}

				if (behaviourFlag) {
					break;
				}
				
				clazz = clazz.getSuperclass();
			}
		}
		return behaviourFlag;
	}

	public static Boolean isInputStream(Class<?> inputObjectClass)
			throws AssertionError {
		final String behaviourString = "java.io.InputStream";
		Class<?> behaviour;
		try {
			behaviour = Class.forName(behaviourString);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"Unable to determine interface behaviour from : "
							+ behaviourString, e);
		}
		return checkBehaviour(inputObjectClass, behaviour);
	}

    /**
     * returns whether a given clazz is a superclass of Set.
     * @param clazz Class object to be
     * @return
     */
    public static boolean isSet(Class clazz) {
        return Set.class.isAssignableFrom(clazz);
    }


}
