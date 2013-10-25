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
import com.betfair.testing.utils.cougar.misc.NameValuePair;

import java.util.List;

public class EnumAssertion implements IAssertion {

	@Override
	public void execute(String message, Object expectedObject, Object actualObject, AggregatedStepExpectedOutputMetaData expectedMetaData) throws AssertionError {

		if (expectedMetaData != null) {
			expectedObject = preProcess(actualObject, expectedMetaData);
		}

		if ((expectedObject == null) || (actualObject == null)) {
            AssertionUtils.jettAssertEquals("Comparing values: ", expectedObject, actualObject);
			return;
		}

		if ((!expectedObject.getClass().isEnum()) || (expectedObject.getClass() != actualObject.getClass())) {
			//If a string then will not have been converted from test data
			if (expectedObject.getClass() == String.class) {

				Class enumClass = actualObject.getClass();
				Object[] enumValues = enumClass.getEnumConstants();

				String expectedEnumValue = (String)expectedObject;

				boolean foundEnum = false;
				for (Object enumValue: enumValues) {
					if (enumValue.toString().equalsIgnoreCase(expectedEnumValue)) {
                        AssertionUtils.jettAssertEquals("Comparing enum values: ", enumValue, actualObject);
						foundEnum = true;
						break;
					}
				}
				if (!foundEnum) AssertionUtils.actionFail("Expected object '" + expectedObject.toString() + "' is not of correct type, as per the actual object '" + actualObject.getClass() + "'");

			} else {
                AssertionUtils.actionFail("Expected object '" + expectedObject.toString() + "' is not of correct type, as per the actual object '" + actualObject.getClass() + "'");
				return;
			}
		} else {
            AssertionUtils.jettAssertEquals("Comparing enum values: ", expectedObject, actualObject);
		}


	}

	@Override
	public Object preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(0).getNameValuePairs();
		if (nvPairs.size() > 1) {
			throw new IllegalStateException("Only expecting one NV pair ... actual size is " + nvPairs.size());
		}

		//If actual object is null we cannot work out what type of enum it is, so return
		//transformed expected object
		if (actualObject == null) {
			return expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
		}

		if (expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0) == null) {
			return null;
		}

		Class enumClass = actualObject.getClass();
		Object[] enumValues = enumClass.getEnumConstants();

		String expectedEnumValue = expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).toString();

		for (Object enumValue: enumValues) {
			if (enumValue.toString().equalsIgnoreCase(expectedEnumValue)) {
				return enumValue;
			}
		}

		throw new IllegalStateException("Unable to match expected enum value to a " + enumClass.getName() + " value. Expected value: " + expectedEnumValue);

	}

}
