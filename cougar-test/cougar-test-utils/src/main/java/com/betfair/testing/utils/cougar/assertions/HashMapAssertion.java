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


import com.betfair.testing.utils.cougar.misc.AggregatedStepExpectedOutputMetaData;
import com.betfair.testing.utils.cougar.misc.DataTypeEnum;
import com.betfair.testing.utils.cougar.misc.ObjectUtil;
import com.betfair.testing.utils.cougar.misc.StepMetaData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapAssertion implements IAssertion {


	@Override
	//public HashMap preProcess(TestStepBean bean) throws JETTFailFastException {
	public HashMap preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expMetaData) throws AssertionError {

		if (expMetaData == null) {
			return null;
		}

		if (expMetaData.size() == 0) {
			return new HashMap<String, Object>();
		}

		HashMap<Object, Object> expectedResultsMap;

		Boolean isExpectedResultAlreadyHashMap = false;

		if (expMetaData.getMetaDataAtIndex(0).size() == 1) {
			if ((expMetaData.getMetaDataAtIndex(0).getValueAtIndex(0)==null) || (expMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass().equals(HashMap.class))) {
				isExpectedResultAlreadyHashMap = true;
			}
		}

		if (isExpectedResultAlreadyHashMap) {
			//expectedResultsMap = (HashMap<String, Object>)bean.getExpectedOutputMetaData().getMetaDataAtIndex(0).getValueAtIndex(0);
			return (HashMap<String, Object>)expMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
		} else {

			HashMap actualHashMap = (HashMap)actualObject;
			expectedResultsMap = new HashMap<Object, Object>();

			//Bit poor but have to ensure all objects are of the same type
			Boolean allContainedObjectsTheSame = true;
			Class currentClass = null;
			Class previousClass = null;
			for (Object keyObject: actualHashMap.keySet()) {
				String key = keyObject.toString();
				if (actualHashMap.get(key) != null) {
					currentClass = 	actualHashMap.get(key).getClass();
					if (previousClass != null) {
						if (previousClass != currentClass) {
							allContainedObjectsTheSame = false;
							break;
						}
					}
					previousClass = currentClass;
				} else {
					/*If item is null, then cannot guarentee that it is the same type as
					other objects*/
					allContainedObjectsTheSame = false;
					break;
				}
			}

			StepMetaData stepMetaData = expMetaData.getMetaDataAtIndex(0);

			if ((actualHashMap.size()!=0) && (allContainedObjectsTheSame)) {
				DataTypeEnum type = ObjectUtil.resolveType(currentClass);

				switch(type) {
				case JAVA_DOT_LANG_OBJECT:
				case STRING:
					for (int i = 0; i < stepMetaData.size(); i++) {
						Object castedExpectedObj = Reflect.getWrappedValue(currentClass, stepMetaData.getValueAtIndex(i));
						expectedResultsMap.put(stepMetaData.getNameAtIndex(i),castedExpectedObj);
					}
					break;
				default:
					//If a complex object have to assume it is already casted as we cannot do it here
					for (int i = 0; i < stepMetaData.size(); i++) {
						expectedResultsMap.put(stepMetaData.getNameAtIndex(i), stepMetaData.getValueAtIndex(i));
					}
					break;
				}
			} else {
				for (int i = 0; i < stepMetaData.size(); i++) {
					expectedResultsMap.put(stepMetaData.getNameAtIndex(i), stepMetaData.getValueAtIndex(i));
				}
			}
		}

		return expectedResultsMap;
	}

	public void execute(String message, Object passedExpObject, Object passedActObject, AggregatedStepExpectedOutputMetaData outputMetaData) throws AssertionError {

		/*
		 * Get a local version of the expected and actual object casted to
		 * correct type
		 */
		/*Map<String, Object> localExpectedResponse = (Map<String, Object>) bean.getTransformedExpectedResponse();
		Map localActualResponse = (Map<String, Object>) bean.getActualResponse();*/

		Map<Object, Object> expectedMap = (Map<Object, Object>) passedExpObject;
		Map<Object, Object> actualMap = (Map<Object, Object>) passedActObject;

		if ((expectedMap == null) || (actualMap == null)) {
            AssertionUtils.jettAssertEquals("Checking HashMap: ", expectedMap, actualMap);
			return;
		}

		DataTypeEnum keyType = null;
		Class<?> keyClass = null;
		if (actualMap.size() > 0) {
			Set<Object> keySet = actualMap.keySet();
			for(Object key: keySet) {
				if (keyClass == null) {
					keyClass = key.getClass();
				} else {
					if (key.getClass().isAssignableFrom(keyClass)) {
						keyClass = key.getClass();
					} else if (keyClass.isAssignableFrom(key.getClass())) {
						//Ok
					} else {
						throw new AssertionError("JETT can only assert maps where keys have the same class");
					}
				}
			}
			keyType = ObjectUtil.resolveType(keyClass);
		}

		for (Object expkey : expectedMap.keySet()) {

			String compareMessage = "Checking HashMap Entry '" + expkey + "' ";

			Object castedExpKey;
			switch (keyType) {
			case JAVA_DOT_LANG_OBJECT:
			case STRING:
				castedExpKey = Reflect.getWrappedValue(keyClass, expkey);
				break;
			default:
				try {
					castedExpKey = keyClass.cast(expkey);
				} catch (ClassCastException e) {
                    AssertionUtils.actionFail(compareMessage + ": Unable to cast expected key '" + expkey.toString() + "' to the class of actual keys '" + keyClass + "'");
					continue;
				}
				break;
			}

			if (!actualMap.containsKey(castedExpKey)) {
                AssertionUtils.actionFail(compareMessage + ": Key not present in HashMap");
				continue;
			}

			Object expectedObj = expectedMap.get(expkey);
			Object actualObj = actualMap.get(castedExpKey);

			if (expectedObj==null) {
                AssertionUtils.jettAssertNull(compareMessage + "expected object is null, check if actual object is null.",actualObj);
			} else if (actualObj==null) {
                AssertionUtils.jettAssertNull(compareMessage + "actual is object is null, checking if expected object is null.",expectedObj);
			} else {
				DataTypeEnum type;
				type = ObjectUtil.resolveType(actualObj);
				switch(type) {
				case JAVA_DOT_LANG_OBJECT:
				case STRING:
					Object castedExpectedObj = Reflect.getWrappedValue(actualObj.getClass(), expectedObj);
                    AssertionUtils.jettAssertEquals(compareMessage + "value: ",castedExpectedObj, actualObj);
					break;
				default:
                    AssertionUtils.actionPass(compareMessage + "- Key Present - Checking values:");
					IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(type);
					assertionProcessor.execute(message, expectedObj, actualObj, null);
					break;
				}
			}
		}

	}
}
