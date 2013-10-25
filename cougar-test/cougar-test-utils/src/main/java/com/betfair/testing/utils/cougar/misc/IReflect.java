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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IReflect {

	@SuppressWarnings("unchecked")
	public abstract Object getPropertyValue(ArrayList propertyList, Object obj);

	/**
	 * @param propertyList
	 * @param value
	 * @param obj
	 */
	public abstract void setValueToProperty(List<?> propertyList,
                                            Object value, Object obj);

	@SuppressWarnings("unchecked")
    // FIXME: Rename to getWrappedValue
	public abstract Object getRealProperty(Class realPropertyType, Object value);

	public abstract Class[] getParameterTypes(Object[] parameters);
	
	public abstract Class matchStringToClass(String classString);
	
	/*
	 * Set the attributes of the passed bean to the values specfied in the passed attributeMap.
	 * 
	 * In passed map key should equal bean attribute name and value the value attribute to
	 * be set to.  Only pass entries for attributes you want to set.
	 *  
	 */
	public void setBeanAttributes(Object bean, Map<String, ?> attributeMap);

    public Object invokeReflection(Method m, Object o, Object... args) throws RuntimeException;

}
