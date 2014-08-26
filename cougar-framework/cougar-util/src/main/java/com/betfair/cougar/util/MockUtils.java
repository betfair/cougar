/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockUtils {
	final static Logger LOGGER = LoggerFactory.getLogger(MockUtils.class);
	public static <T> T generateMockResponse(Class<T> responseClass, RequestContext ctx, Object... params ) {
		try {
			Generator generator = new Generator();
//			T responseObject = responseClass.newInstance();

			T responseObject = generateObject(responseClass, generator);
			return responseObject;
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate response object: "+e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T generateObject(Class<T> clazz, Generator generator) throws Exception {
		if (Result.class.isAssignableFrom(clazz)) {
			T object = clazz.newInstance();
			Method[] methods = clazz.getMethods();
			for (Method m: methods) {
				if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
					populateMethod(m, object, generator);
				}
			}
			return object;
		} else {
			return (T)getValueIfbasicType(clazz, generator);
		}
	}
	private static void populateMethod(Method method, Object object, Generator generator) throws Exception {

		Class<?> fieldType = method.getParameterTypes()[0];
		Object result = getValueIfbasicType(fieldType, generator);
		if (result != null) {
			method.invoke(object, result);
		} else if ((List.class.isAssignableFrom(fieldType)) ||
					(Set.class.isAssignableFrom(fieldType))) {

			Class<?> fieldArgClass;
			Type genericFieldType = method.getGenericParameterTypes()[0];
			if(genericFieldType instanceof ParameterizedType){
			    ParameterizedType aType = (ParameterizedType) genericFieldType;
			    Type[] fieldArgTypes = aType.getActualTypeArguments();
		    	fieldArgClass = (Class<?>) fieldArgTypes[0];
			} else {
				throw new RuntimeException("parametised list not mockable - "+genericFieldType.getClass());
			}


			Collection<Object> coll;
			if (Set.class.isAssignableFrom(fieldType)) {
				coll = new HashSet<Object>();
			} else {
				coll = new ArrayList<Object>();
			}
			int length = (generator.getNumber() % 5) + 2;
			for (int i = 0; i < length; ++i) {
				Object listMember = generateObject(fieldArgClass, generator);
				coll.add(listMember);
			}
			method.invoke(object, coll);


		} else if (Map.class.isAssignableFrom(fieldType)) {
			Class<?> fieldArgKeyClass;
			Class<?> fieldArgValueClass;
			Type genericFieldType = method.getGenericParameterTypes()[0];
			if(genericFieldType instanceof ParameterizedType){
			    ParameterizedType aType = (ParameterizedType) genericFieldType;
			    Type[] fieldArgTypes = aType.getActualTypeArguments();
		    	fieldArgKeyClass = (Class<?>) fieldArgTypes[0];
		    	fieldArgValueClass = (Class<?>) fieldArgTypes[1];
			} else {
				throw new RuntimeException("parametised list not mockable - "+genericFieldType.getClass());
			}


			Map<Object, Object> map = new HashMap<Object, Object>();
			int length = (generator.getNumber() % 5) + 2;
			for (int i = 0; i < length; ++i) {
				Object key = generateObject(fieldArgKeyClass, generator);
				Object value = generateObject(fieldArgValueClass, generator);
				map.put(key, value);
			}
			method.invoke(object, map);

		} else if (Result.class.isAssignableFrom(fieldType)) {
			result = generateObject(fieldType, generator);
			method.invoke(object, result);
		} else {
			LOGGER.info("Could not mock data of type {} into method {}", fieldType, method.getName());
		}
	}

	private static Object getValueIfbasicType(Class<?> fieldType, Generator generator) {
		if (fieldType == Byte.class) {
			return (byte)generator.getNumber();
		} else if (fieldType == Short.class) {
			return (short)generator.getNumber();
		} else if (fieldType == Integer.class) {
			return generator.getNumber();
		} else if (fieldType == Long.class) {
			return (long)generator.getNumber();
		} else if (fieldType == Float.class) {
			return (float)generator.getNumber();
		} else if (fieldType == Double.class) {
			return (double)generator.getNumber();
		} else if (fieldType == Boolean.class) {
			return generator.getBoolean();
		} else if (fieldType == Character.class) {
			return (char)generator.getNumber();
		} else if (fieldType == String.class) {
			return generator.getString();
		} else if (fieldType == Date.class) {
			return generator.getDate();
		} else if (fieldType.isEnum()) {
			Object[] constants = fieldType.getEnumConstants();
			return constants[generator.getNumber()%constants.length];
		}
		return null;
	}
	private static class Generator {
		private int count = 1;

		String getString() {
			return "String-"+(count++);
		}

		int getNumber() {
			return count++;
		}

		boolean getBoolean() {
			return (count++)%2==0?true:false;
		}

		Date getDate() {
			return new Date(count++);
		}
	}
}
