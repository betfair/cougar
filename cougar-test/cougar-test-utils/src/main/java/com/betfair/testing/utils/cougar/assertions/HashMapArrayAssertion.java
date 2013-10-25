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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapArrayAssertion implements IAssertion {

	@Override
	public HashMap<Object, Object>[] preProcess(Object actualObject,
			AggregatedStepExpectedOutputMetaData expectedObjectMetaData)
			throws AssertionError {

		Boolean isExpectedResultAlreadyHashMapArray = false;

		if (expectedObjectMetaData == null) {
			return null;
		}

		if (expectedObjectMetaData.size() == 0) {
			// bean.setTransformedExpectedResponse(new HashMap[0]);
			// return this;
			return new HashMap[0];
		}

		if ((expectedObjectMetaData.size() == 1)
				&& (expectedObjectMetaData.getMetaDataAtIndex(0).size() == 0)) {
			return new HashMap[0];
		}

		if (expectedObjectMetaData.getMetaDataAtIndex(0).size() == 1) {
			if (expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0)
					.getClass().equals(HashMap[].class)) {
				isExpectedResultAlreadyHashMapArray = true;
			}
		}

		if (isExpectedResultAlreadyHashMapArray) {
			return (HashMap<Object, Object>[]) expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
		} else {
			HashMap<Object, Object>[] expectedResultsMap = new HashMap[expectedObjectMetaData.size()];

			for (int i = 0; i < expectedObjectMetaData.size(); i++) {
				expectedResultsMap[i] = new HashMap<Object, Object>();
				List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(i).getNameValuePairs();
				for (NameValuePair nvPair : nvPairs) {
					expectedResultsMap[i].put(nvPair.getName(), nvPair.getValue());
				}
			}
			return expectedResultsMap;
		}
	}

	/**
	 * Assert that the number of HashMaps contained in the Expected Result Map
	 * Array and the Actual Result Map Array are the same.
	 *
	 * For each element in each HashMap contained in the Expected Result Map
	 * Array:
	 *
	 * Convert expected object into corresponding data type held in the
	 * datasource: All numerics converted to java.math.BigDDecimal All
	 * date/times converted to java.sql.timestamps All strings left as the are
	 *
	 * Expected objects are asserted agaisnt corresponding Actual objects:
	 * Timestamps (date/times) asserted using:
	 * com.betfair.assertions.functions.Assert.logAssertDatesWithTolerance
	 * Everything else asserted using:
	 * com.betfair.assertions.functions.Assert.logAssertEquals
	 *
	 * @throws com.betfair.jett.exceptions.JETTException
	 * @throws AssertionError
	 * @throws com.betfair.assertions.asserter.exceptions.AssertionUtilityException
	 *
	 */

	public void execute(String message, Object passedExpObject,
                        Object passedActObject,
                        AggregatedStepExpectedOutputMetaData outputMetaData)
			throws AssertionError {

		// Map<String, Object>[] localExpectedResponse = (Map<String, Object>[])
		// testStep.getTransformedExpectedResponse();
		// Map<String, Object>[] localActualResponse = (Map<String, Object>[])
		// testStep.getActualResponse();

		Map<Object, Object>[] localExpectedResponse = (Map<Object, Object>[]) passedExpObject;
		Map<Object, Object>[] localActualResponse = (Map<Object, Object>[]) passedActObject;

		String errorMessage;

		if (localExpectedResponse == null) {
            AssertionUtils.jettAssertNull(
					"Expected null array, check Actual array:",
					localActualResponse);
		} else {

			/* Assert lengths of the Expected Result and Actual Map Arrays */
            AssertionUtils.jettAssertEquals("Number of Maps in Array: ",
					localExpectedResponse.length, localActualResponse.length);

			/* Loop through Expected Result Map Array */
			for (int i = 0; i < localExpectedResponse.length; i++) {

				int rowCounter = i + 1;

				/*
				 * Get the HashMap (row) from the Expected Result and Actual Map
				 * Arrays
				 */
				Map<Object, Object> expectedMap = localExpectedResponse[i];
				/*
				 * If no corresponding row in localActualResponse then set row
				 * to null
				 */
				Map<Object, Object> actualMap;
				if (localActualResponse.length < rowCounter) {
                    AssertionUtils.actionFail("Map at row " + rowCounter
							+ " missing.");
				} else {
                    AssertionUtils.actionPass("Map at row " + rowCounter
							+ " present.  Checking Map entries:");
					actualMap = localActualResponse[i];

					//Pass off the assertion of the actual HashMaps to the HashMap asserter
					IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(DataTypeEnum.HASHMAP);
					assertionProcessor.execute(message, expectedMap, actualMap, null);
				}
			}
			for (int i = localExpectedResponse.length; i < localActualResponse.length; i++) {
				Map<Object, Object> extraActualMap = localActualResponse[i];
                AssertionUtils.actionFail("Extra row in Actual Object: "
						+ extraActualMap.toString());
			}
		}
	}

}
