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



import com.betfair.testing.utils.cougar.misc.DataTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class AssertionProcessorFactory {

	private static Map<DataTypeEnum, IAssertion> assertionMap;

    static {
        assertionMap = new HashMap<DataTypeEnum, IAssertion>();
        assertionMap.put(DataTypeEnum.ARRAY, new ArrayAssertion());
        assertionMap.put(DataTypeEnum.ARRAYLIST, new ArrayListAssertion());
        assertionMap.put(DataTypeEnum.BEAN, new JavaBeanAssertion());
        assertionMap.put(DataTypeEnum.DOCUMENT, new DocumentAssertion());
        assertionMap.put(DataTypeEnum.ENUM, new EnumAssertion());
        assertionMap.put(DataTypeEnum.EXCEPTION, new ExceptionAssertion());
        assertionMap.put(DataTypeEnum.HASHMAP, new HashMapAssertion());
        assertionMap.put(DataTypeEnum.HASHMAPARRAY, new HashMapArrayAssertion());
//        assertionMap.put(DataTypeEnum.INPUTSTREAM, new ArrayAssertion()); // todo??
        assertionMap.put(DataTypeEnum.JAVA_DOT_LANG_OBJECT, new StandardAssertion());
        assertionMap.put(DataTypeEnum.JAVABEAN_ARRAY, new JavaBeanArrayAssertion());
        assertionMap.put(DataTypeEnum.JSONOBJECT, new JSONObjectAssertion());
        assertionMap.put(DataTypeEnum.LINKEDHASHMAP, new LinkedHashMapAssertion());
        assertionMap.put(DataTypeEnum.LINKEDHASHMAPARRAY, new ArrayAssertion());
        assertionMap.put(DataTypeEnum.LIST, new ArrayListAssertion());
        assertionMap.put(DataTypeEnum.NULL_OBJECT, new NullObjectAssertion());
        assertionMap.put(DataTypeEnum.SET, new SetAssertion());
        assertionMap.put(DataTypeEnum.STANDARD, new StandardAssertion());
        assertionMap.put(DataTypeEnum.STRING, new StandardAssertion());
        assertionMap.put(DataTypeEnum.THROWABLE, new ExceptionAssertion());
        assertionMap.put(DataTypeEnum.TIMESTAMP, new TimeStampAssertion());

    }

	public static IAssertion getAssertionProcessor(DataTypeEnum dataType) {
		IAssertion assertion = assertionMap.get(dataType);
		if (assertion == null) {
			throw new IllegalStateException("Don't know how to handle:" + dataType);
		}
		return assertion;
	}
}