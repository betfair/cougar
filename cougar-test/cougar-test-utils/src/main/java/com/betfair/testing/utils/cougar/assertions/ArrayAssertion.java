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
import com.betfair.testing.utils.cougar.misc.NameValuePair;
import com.betfair.testing.utils.cougar.misc.ObjectUtil;

import java.lang.reflect.Array;
import java.util.List;

/**
 *
 * Assertion methods for 1d and 2d arrays
 *
 */
public class ArrayAssertion implements IAssertion {

	@Override
	public Object preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		if (expectedObjectMetaData == null) {
			return null;
		}

		if (expectedObjectMetaData.size() == 0) {
			return new Object[0];
		}

		//Check if already an array
		if (expectedObjectMetaData.getMetaDataAtIndex(0).size() == 1) {
			if ((expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0)==null) || (expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass().isArray())) {
				return expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
			}
		}

		/*Object[][] expectedResultsArray = new Object[expectedObjectMetaData.size()][expectedObjectMetaData.getMetaDataAtIndex(0).getNameValuePairs().size()];
		for (int i = 0; i < expectedObjectMetaData.size(); i++) {
			List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(i).getNameValuePairs();
			int pairCounter = 0;
			for (NameValuePair pair: nvPairs) {
				expectedResultsArray[i][pairCounter] = (Object)pair.getValue();
				pairCounter++;
			}
		}
		return expectedResultsArray;*/

		if (expectedObjectMetaData.getMetaDataAtIndex(0).size() == 1) {
			Object expectedResultsArray = Array.newInstance(Object.class, expectedObjectMetaData.size());
			for (int i = 0; i < expectedObjectMetaData.size(); i++) {
				Array.set(expectedResultsArray, i, expectedObjectMetaData.getMetaDataAtIndex(i).getNameValuePairs().get(0).value);
			}
			return expectedResultsArray;
		} else {
			Object[][] expectedResultsArray = new Object[expectedObjectMetaData.size()][expectedObjectMetaData.getMetaDataAtIndex(0).getNameValuePairs().size()];
			for (int i = 0; i < expectedObjectMetaData.size(); i++) {
				List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(i).getNameValuePairs();
				int pairCounter = 0;
				for (NameValuePair pair: nvPairs) {
					expectedResultsArray[i][pairCounter] = (Object)pair.getValue();
					pairCounter++;
				}
			}
			return expectedResultsArray;
		}
	}

	@Override
	public void execute(String message, Object passedExpObject, Object passedActObject, AggregatedStepExpectedOutputMetaData outputMetaData) throws AssertionError {

		AssertionUtils.jettAssertEquals("Number of rows: ", Array.getLength(passedExpObject), Array.getLength(passedActObject));

		for (int i = 0; i < Array.getLength(passedExpObject); i++) {
			String assertionMessage = message+ ": Array row " + i + " check value: ";
			Object expArrayObject = Array.get(passedExpObject, i);

			if (Array.getLength(passedActObject) < i+1) {
                AssertionUtils.actionFail(assertionMessage + "Row not present in actual array");
			} else {
				Object actArrayObject = Array.get(passedActObject, i);

				if (actArrayObject == null) {
                    AssertionUtils.jettAssertNull(assertionMessage + "Actual value was null, checking Expected value: ", expArrayObject);
					continue;
				}

				if (expArrayObject == null) {
                    AssertionUtils.jettAssertNull(assertionMessage + " Expected value is null, checking Actual value: ", actArrayObject);
					continue;
				}

				DataTypeEnum type = ObjectUtil.resolveType(actArrayObject);
				switch(type) {
				case JAVA_DOT_LANG_OBJECT:
				case STRING:
					Object castedExpArrayObject;
					try {
						castedExpArrayObject = Reflect.getWrappedValue(actArrayObject.getClass(), expArrayObject);
					} catch (Exception e) {
                        AssertionUtils.actionFail(assertionMessage + "Unable to convert expected array object to actual array object class.  Expected array object Class: " + expArrayObject.getClass() + ", Value: " + expArrayObject + ". Actual object Class: " + actArrayObject.getClass() + ", Value: " + actArrayObject + ". " + e.getMessage());
						continue;
					}
                    AssertionUtils.jettAssertEquals(assertionMessage, castedExpArrayObject, actArrayObject);
					break;
				default:
                    AssertionUtils.actionPass(assertionMessage + "Row present - Checking values");
					IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(type);
					assertionProcessor.execute(message, expArrayObject, actArrayObject, null);
					break;
				}

			}


		}


	}


}
