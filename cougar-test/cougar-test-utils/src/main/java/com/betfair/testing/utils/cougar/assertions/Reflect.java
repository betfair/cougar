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

package com.betfair.testing.utils.cougar.assertions;

import com.betfair.testing.utils.cougar.misc.StringHelpers;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reflect {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.betfair.reflection.IReflect#getPropertyValue(java.util.ArrayList,
	 * java.lang.Object)
	 */


	//-----DD/MM/YY hh:mm:ss.S
	//-----DD/MM/YYYY hh:mm:ss.S
	private static final String timestamPatternString = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((\\d\\d)|((19|20)\\d\\d))\\s([1-9]|([01][0-9])|(2[0-3])):((0[0-9])|([12345][0-9])):((0[0-9])|([12345][0-9])).[0-9]*";
	private static final Pattern timestamPattern = Pattern.compile(timestamPatternString);

	@SuppressWarnings("unchecked")
	public static Object getPropertyValue(ArrayList propertyList, Object obj) {
		// *** get the name of the property ...
		String propertyName = (String) propertyList.get(0);

		// *** this split is used to check whether we have
		// *** an array to deal with, e.g. "Bet 1" indicates
		// *** getBet(0) ...

		String[] split = propertyName.split(" ");

		String indexStr = null;

		// *** if this is an array then
		// *** get its index ...

		if (split.length == 2) {
			indexStr = split[1];
			propertyName = split[0];
		}

		// *** locate the get method ...
		Method getMethod = null;

		try {
			/* Make first letter of property name uppercase */
			String convertedPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			getMethod = searchForMethod(obj, convertedPropertyName);
//			getMethod = obj.getClass().getMethod("get" + convertedPropertyName);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException("Error: get" + propertyName + " does not exist in "
					+ obj.getClass().toString(), e);
		}

		Object propertyInstance = null;

		try {
			propertyInstance = getMethod.invoke(obj);

		} catch (Exception e) {
			throw new RuntimeException("Error: Invoking get" + propertyName + " on " + "object "
					+ obj.getClass().toString(), e);
		}

		// *** get the return type of the getter ...
		Class<?> returnType = getMethod.getReturnType();

		// *** if we are not at the actual get method we're looking for
		// *** then attempt to go through the nest until we reach it ...

		if (propertyList.size() >= 2) {
			// *** if this property has not been
			// *** set, then set it ...

			if (propertyInstance != null) {
				// *** if it is an array then we need to
				// *** make sure that the item referred to
				// *** isn't null + we assign propertyItem
				// *** is equal to the item ...

				if (returnType.isArray()) {
					propertyInstance = getArrayItem(indexStr, propertyInstance, returnType);
				}

				ArrayList newPropertyList = new ArrayList();

				for (int i = 1; i < propertyList.size(); i++) {
					newPropertyList.add(propertyList.get(i));
				}

				propertyInstance = getPropertyValue(newPropertyList, propertyInstance);
			}
		} else {
			if (propertyInstance != null) {
				if (returnType.isArray()) {
					propertyInstance = getArrayItem(indexStr, propertyInstance, returnType);
				}
			}
		}

		return propertyInstance;
	}

	private static Object getArrayItem(String indexStr, Object propertyInstance, Class<?> returnType) {
		// *** get the index ...
		int index = 0;

		try {
			index = Integer.parseInt(indexStr) - 1;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing array index.");
		}

		// *** get the current array length ...
		int currentArrayLength = Array.getLength(propertyInstance);

		// *** if the specified index is outside
		// *** current array length then create a
		// *** new item ...

		if (index < currentArrayLength) {
			// *** attempt to get item from current
			// *** array ...

			propertyInstance = Array.get(propertyInstance, index);
		} else {
			propertyInstance = null;
		}

		return propertyInstance;
	}


	public static void setValueToProperty(List<?> propertyList, Object value, Object obj) {
		// *** get the name of the property ...
		String propertyName = (String) propertyList.get(0);

		// *** if we are not at the primitive type
		// *** yet ...
		if (propertyList.size() >= 2) {
			// *** this split is used to check whether we have
			// *** an array to deal with, e.g. "Bet 1" indicates
			// *** getBet(0) ...

			String[] split = propertyName.split(" ");

			String indexStr = null;

			// *** if this is an array then
			// *** get its index ...

			if (split.length == 2) {
				indexStr = split[1];
				propertyName = split[0];
			}

			// *** locate the get method ...
			Method getMethod = null;

			try {
//				getMethod = obj.getClass().getMethod("get" + propertyName);
				String convertedPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

				getMethod = searchForMethod(obj, convertedPropertyName);
			} catch (Exception e) {
				throw new RuntimeException("Error: locating get" + propertyName + " on " + "object "
						+ obj.getClass().toString() + e.getMessage());
			}

			if (getMethod != null) {
				Object propertyInstance = null;

				try {
					propertyInstance = getMethod.invoke(obj);
				} catch (Exception e) {
					throw new RuntimeException("Error: Invoking get" + propertyName + " on " + "object "
							+ obj.getClass().toString() + e.getMessage());
				}

				// *** get the return type of the getter ...
				Class<?> returnType = getMethod.getReturnType();
				// *** if this property has not been
				// *** set, then set it ...

				if (propertyInstance == null) {
					// *** if this property is not an
					// *** array, then treat it normally ...

					if (!returnType.isArray()) {
						// *** create a new instance of the
						// *** return type ...

						try {
							propertyInstance = returnType.getConstructor().newInstance();
						} catch (Exception e) {
							throw new RuntimeException("Error: Creating an instance of type " + returnType
									+ "from get method " + getMethod.getName() + ". " + e.getMessage());
						}

						// *** get its equivalent setter method ...
						Method setMethod = null;

						try {
							setMethod = obj.getClass().getMethod("set" + propertyName, new Class[] { returnType });
						} catch (Exception e) {
							throw new RuntimeException("Error: locating set method set" + propertyName
									+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
						}

						try {
							// *** set the property ...
							setMethod.invoke(obj, propertyInstance);
						} catch (Exception e) {
							throw new RuntimeException("Error: invoking set method set" + propertyName
									+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
						}
					} else {
						// *** get the index we wish to set ...
						int index = 0;

						try {
							index = Integer.parseInt(indexStr.trim()) - 1;
						} catch (Exception e) {
							throw new RuntimeException("Error: parsing index " + indexStr + ".");
						}

						if (index < 0) {
							throw new RuntimeException("Array index is less " + "than zero: " + index + ".");
						}

						// *** get the component type ...
						Class<?> componentType = returnType.getComponentType();

						Object arr = Array.newInstance(componentType, index + 1);

						try {
							propertyInstance = componentType.getConstructor().newInstance();
						} catch (Exception e) {
							throw new RuntimeException("Error: creating instance of array item of type "
									+ componentType.toString() + ". " + e.getMessage());
						}

						Array.set(arr, index, propertyInstance);

						Method setMethod = null;

						try {
							setMethod = obj.getClass().getMethod("set" + propertyName, new Class[] { returnType });
						} catch (Exception e) {
							throw new RuntimeException("Error: locating set method set" + propertyName
									+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
						}

						try {
							// *** set the property ...
							setMethod.invoke(obj, arr);
						} catch (Exception e) {
							throw new RuntimeException("Error: invoking set method set" + propertyName
									+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
						}
					}
				} else
				// *** if it is an array then we need to
				// *** make sure that the item referred to
				// *** isn't null + we assign propertyItem
				// *** is equal to the item ...

				if (returnType.isArray()) {
					// *** get the index ...
					int index = Integer.parseInt(indexStr) - 1;

					// *** get the current array length ...
					int currentArrayLength = Array.getLength(propertyInstance);

					int newArrayLength = currentArrayLength < (index + 1) ? (index + 1) : currentArrayLength;

					// *** 1. Create a new array item
					// *** 2. Create a new ArrayList
					// *** 3. Store current array items in ArrayList
					// *** 4. Append new array item to ArrayList
					// *** 5. Set new array
					// *** 6. Assign propertyInstance to new item

					// *** this is the class type of the array item ...
					Class<?> componentType = returnType.getComponentType();

					// *** this is the array item ...
					Object arrItem = null;

					// *** if the specified index is outside
					// *** current array length then create a
					// *** new item ...

					if (newArrayLength > currentArrayLength) {
						try {
							arrItem = componentType.newInstance();
						} catch (Exception e) {
							throw new RuntimeException("Error: creating new array item instance of type "
									+ componentType.toString() + " " + e.getMessage());
						}
					} else {
						// *** attempt to get item from current
						// *** array ...

						arrItem = Array.get(propertyInstance, index);

						// *** if this item is null then create
						// *** a new instance of it ...

						if (arrItem == null) {
							try {
								arrItem = componentType.newInstance();
							} catch (Exception e) {
								throw new RuntimeException("Error: creating new array item instance of type "
										+ componentType.toString() + " " + e.getMessage());
							}
						}
					}

					Object arr = Array.newInstance(componentType, newArrayLength);

					int currentIndex = 0;

					// *** add current items to new array instance ...
					for (; currentIndex < currentArrayLength; currentIndex++) {
						// *** if we are at the index position then
						// *** add the new item ...

						Object item = null;

						if (currentIndex == index) {
							item = arrItem;
						} else {
							item = Array.get(propertyInstance, currentIndex);
						}

						Array.set(arr, currentIndex, item);
					}

					for (; currentIndex < newArrayLength; currentIndex++) {
						// *** if we are at the index position then
						// *** add the new item ...

						if (currentIndex == index) {
							Array.set(arr, currentIndex, arrItem);
						}
					}

					Method setMethod = null;

					try {
						setMethod = obj.getClass().getMethod("set" + propertyName, new Class[] { returnType });
					} catch (Exception e) {
						throw new RuntimeException("Error: locating set method 'set" + propertyName + "' "
								+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
					}

					try {
						// *** set the new array with new item ...
						setMethod.invoke(obj, arr);
					} catch (Exception e) {
						throw new RuntimeException("Error: invoking set method 'set" + propertyName + "' "
								+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
					}

					// *** set the new item to the
					// *** property instance ...

					propertyInstance = arrItem;
				}

				ArrayList newPropertyList = new ArrayList();

				for (int i = 1; i < propertyList.size(); i++) {
					newPropertyList.add(propertyList.get(i));
				}

				setValueToProperty(newPropertyList, value, propertyInstance);
			}
		} else {
			// *** locate get method return type ...
			Class<?> returnClassType = null;
			Method method = null;
			try {
				/* Make first letter of property name uppercase */
				String convertedPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

				method = searchForMethod(obj, convertedPropertyName);
				returnClassType = method.getReturnType();

			}catch (NoSuchMethodException e) {
				throw new RuntimeException("Error: locating return type from 'get" + propertyName + "' "
						+ "from object " + obj.getClass().toString() + e.getMessage());
			}catch (Exception e) {
				e.printStackTrace();
			}

			// *** locate set method ...
			Method setMethod = null;

			try {
				/* Make first letter of property name uppercase */
				String convertedPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
				setMethod = obj.getClass().getMethod("set" + convertedPropertyName, new Class[] { returnClassType });
			} catch (Exception e) {
				throw new RuntimeException("Error: locating set method 'set" + propertyName + "' "
						+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
			}

			if (isBoxable(returnClassType)) {
				Object realValue = getWrappedValue(setMethod.getParameterTypes()[0], value);

				try {
					setMethod.invoke(obj, realValue);
				} catch (Exception e) {
					throw new RuntimeException("Error: invoking set method 'set" + propertyName + "' "
							+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
				}
			} else { //if (returnClassType == Object.class) {
				try {
					setMethod.invoke(obj, value);
				} catch (Exception e) {
					throw new RuntimeException("Error: invoking set method 'set" + propertyName + "' "
							+ "from object " + obj.getClass().toString() + ". " + e.getMessage());
				}
			}
		}
	}


	@SuppressWarnings("unchecked")
	public static Object getWrappedValue(Class clazz, Object value) {
		if (value == null) {
			return null;
		}
		if (clazz.equals(int.class)) {
			return Integer.parseInt(value.toString());

		} else if (clazz.equals(Integer.class)) {
			return new Integer(value.toString());

		} else if (clazz.equals(String.class)) {
			// return value;
			return value.toString();

		} else if (clazz.equals(double.class)) {
			return Double.parseDouble(value.toString());

		} else if (clazz.equals(Double.class)) {
			return new Double(value.toString());

		} else if (clazz.equals(boolean.class)) {
			return Boolean.parseBoolean(value.toString());

		} else if (clazz.equals(Boolean.class)) {
			return new Boolean(value.toString());

		} else if (clazz.equals(float.class)) {
			return Float.parseFloat(value.toString());

		} else if (clazz.equals(Float.class)) {
			return new Float(value.toString());

		} else if (clazz.equals(byte.class)) {
			return Byte.parseByte(value.toString());

		} else if (clazz.equals(Byte.class)) {
			return new Byte(value.toString());

		} else if (clazz.equals(short.class)) {
			return Short.parseShort(value.toString());

		} else if (clazz.equals(Short.class)) {
			return new Short(value.toString());

		} else if (clazz.equals(long.class)) {
			return Long.parseLong(value.toString());

		} else if (clazz.equals(Long.class)) {
			return new Long(value.toString());

		} else if (clazz.isEnum()) {
			return Enum.valueOf(clazz, value.toString());

		} else if (clazz.equals(java.math.BigDecimal.class)) {
			return new java.math.BigDecimal(value.toString());

		} else if (clazz.equals(org.json.JSONObject.class)) {
			try {
				return new org.json.JSONObject(value.toString());
			} catch (JSONException e) {
				throw new RuntimeException("Problem reflecting to JSONObject", e);
			}

		} else if (clazz.isEnum()) {
			Object[] enums = clazz.getEnumConstants();
			for (Object enumValue: enums) {
				if (enumValue.toString().equalsIgnoreCase(value.toString())) {
					return enumValue;
				}
			}
			throw new RuntimeException("Unable to match specified enum value");
		} else if (clazz.equals(Timestamp.class)) {
			java.util.Date date = getJavaUtilDateFromString(value.toString());
		    return new java.sql.Timestamp(date.getTime());
		} else if (clazz.equals(java.util.Date.class)) {
			return getJavaUtilDateFromString(value.toString());
		} else if (clazz.equals(java.lang.Class.class)) {
            try {
                return (Class)value;
            } catch (ClassCastException e) {
                //Do nothing
            }
            try {
			    return Class.forName(value.toString());
            } catch (ClassNotFoundException e) {
                if (value.toString().startsWith("class ")) {
                    try {
                        return Class.forName(value.toString().substring(6));
                    } catch (ClassNotFoundException e1) {
                        throw new RuntimeException("Problem reflecting to Class object: " + value.toString().substring(6), e);
                    }
                }
				throw new RuntimeException("Problem reflecting to Class object: " + value.toString(), e);
			}
		} else {
					Method fromValueMethod;

			Object enumValue = null;

			try {
				fromValueMethod = clazz.getMethod("fromValue", new Class[] { String.class });

				try {
					enumValue = fromValueMethod.invoke(null, new Object[] { value });
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}

			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

			return enumValue;
		}
	}

	private static boolean isBoxable(Class<?> c) {
		boolean primitive = c.equals(int.class) || c.equals(Integer.class) || c.equals(String.class)
				|| c.equals(double.class) || c.equals(Double.class) || c.equals(boolean.class)
				|| c.equals(Boolean.class) || c.equals(float.class) || c.equals(Float.class) || c.equals(byte.class)
				|| c.equals(Byte.class) || c.equals(short.class) || c.equals(Short.class) || c.equals(long.class)
				|| c.equals(Long.class) || c.equals(char.class) || c.isEnum();

		if (!primitive) {
			try {
				primitive = c.getMethod("fromValue", new Class[] { String.class }) != null;
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				//throw new RuntimeException("No such method as 'fromValue' on " + c.getName(), e);
				return false;
			}
		}

		return primitive;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.betfair.reflection.IReflect#getParameterTypes(java.lang.Object[])
	 */
	public static Class[] getParameterTypes(Object[] parameters) {
		ArrayList parameterTypeList = new ArrayList();

		Class<?>[] p = null;

		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				parameterTypeList.add(parameters[i].getClass());
			}

			// *** create a definition of the types of
			// *** parameters ...

			p = (Class[]) parameterTypeList.toArray(new Class<?>[parameterTypeList.size()]);
		}

		return p;
	}

	public static Class matchStringToClass(String classString) {

		if ((classString == null) || (classString.equalsIgnoreCase(""))) {
			return null;
		}

		if (int.class.getName().endsWith(classString)) {
			return int.class;

		} else if (Integer.class.getName().endsWith(classString)) {
			return Integer.class;

		} else if (String.class.getName().endsWith(classString)) {
			return String.class;

		} else if (double.class.getName().endsWith(classString)) {
			return double.class;

		} else if (Double.class.getName().endsWith(classString)) {
			return Double.class;

		} else if (boolean.class.getName().endsWith(classString)) {
			return Boolean.class;

		} else if (Boolean.class.getName().endsWith(classString)) {
			return Boolean.class;

		} else if (float.class.getName().endsWith(classString)) {
			return float.class;

		} else if (Float.class.getName().endsWith(classString)) {
			return Float.class;

		} else if (byte.class.getName().endsWith(classString)) {
			return byte.class;

		} else if (Byte.class.getName().endsWith(classString)) {
			return Byte.class;

		} else if (short.class.getName().endsWith(classString)) {
			return short.class;

		} else if (Short.class.getName().endsWith(classString)) {
			return Short.class;

		} else if (long.class.getName().endsWith(classString)) {
			return long.class;

		} else if (Long.class.getName().endsWith(classString)) {
			return Long.class;

		} else if (BigDecimal.class.getName().endsWith(classString)) {
			return BigDecimal.class;

		} else if (classString.endsWith("Enum")) {
			throw new RuntimeException("Currently unable to match class strings to Enums: " + classString);

		} else if (Timestamp.class.getName().endsWith(classString)) {
			return Timestamp.class;

		} else {
			throw new RuntimeException("No mapping exists for class string: " + classString);
		}
	}

	private static java.util.Date getJavaUtilDateFromString(String dateTimeString) {
		String formattedDateString;
		try {
			formattedDateString = StringHelpers.formatDateString2((String) dateTimeString);
		} catch (ParseException e1) {
			throw new IllegalStateException("Unable to covert passed value to java.util.date as do not have a matching pattern: " + dateTimeString);
		}

		Matcher m;
		boolean matchFound;

		m = timestamPattern.matcher(formattedDateString);
		matchFound = m.matches();

		if (matchFound) {
	    	DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss.S");
	        java.util.Date date;
	        try {
				date = (java.util.Date)formatter.parse(formattedDateString);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
	        return date;
		} else {
			//maybe a long was passed
			try {
				Long longval = Long.valueOf(dateTimeString);
				return new java.util.Date(longval);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Unable to covert passed value to java.util.date as do not have a matching pattern: " + dateTimeString);
			}



		}
	}

	private static Method searchForMethod(Object obj, String convertedPropertyName) throws NoSuchMethodException{
		Method[] methods = obj.getClass().getMethods();
		for (Method method : methods) {
			//check if get*** exist; otherwise try is*** if the return type is boolean
			//Note: if the property is Boolean, it will use get***. If the property is boolean, it will use is***.
			if (method.getName().equalsIgnoreCase("get" + convertedPropertyName) ||
					(method.getReturnType().getSimpleName().equalsIgnoreCase("boolean") && method.getName().equalsIgnoreCase("is" + convertedPropertyName))) {
				return method;
			}
		}
		throw new NoSuchMethodException("Method get" + convertedPropertyName + " can't be found in object " + obj.getClass().getName());
	}

	/*
	 * Set the attributes of the passed bean to the values specfied in the passed attributeMap.
	 *
	 * In passed map key should equal bean attribute name and value the value attribute to
	 * be set to.  Only pass entries for attributes you want to set.
	 *
	 */
	public static void setBeanAttributes(Object bean, Map<String, ?> attributeMap) {
		for (String key: attributeMap.keySet()) {
			 ArrayList<String> tempList = new ArrayList<String>();
			 tempList.add(key);
			 try {
				 setValueToProperty(
						 tempList,
						 attributeMap.get(key),
						 bean);
			 } catch (Exception e) {
				throw new RuntimeException("Problem setting bean attribute '" + key + "' to: " + String.valueOf(attributeMap.get(key)), e);
			 }
		 }
	}

    /**
    * invokes given method of the given object o via reflection
    * @param m method
    * @param o object
    * @return invocation value
    * @throws RuntimeException
    */
    public static Object invokeReflection(Method m, Object o, Object... args) throws RuntimeException {
        try {
            return m.invoke(o, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access " + m.getClass().getName() + "." + m.getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Reflection failed for " + m.getClass().getName() + "." + m.getName(), e);
        }

    }


}
