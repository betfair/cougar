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

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.List;

public class TimeStampAssertion implements IAssertion {

	@Override
	public void execute(String message, Object passedExpObject, Object passedActObject, AggregatedStepExpectedOutputMetaData outputMetaData) throws AssertionError {

		Timestamp expectedTimestamp;

		//It is still possible to recieve string instead of timestamp at this point
		//so reflect
		if (passedExpObject.getClass() != Timestamp.class) {
			expectedTimestamp = convertObjectToTimestamp(passedExpObject.getClass(), passedExpObject);
		} else {
			expectedTimestamp = (Timestamp)passedExpObject;
		}
		Timestamp actualTimestamp = (Timestamp)passedActObject;

		if (expectedTimestamp==null) {
			AssertionUtils.jettAssertNull("Expected timestamp is null:", actualTimestamp);
		} else {
			if (actualTimestamp == null) {
				AssertionUtils.actionFail("Returned timestamp is null");
			} else {
				String assertionMessage = "Check if Timestamp within expected tolerance ("+AssertionUtils.getDateTolerance()+"ms):";
                AssertionUtils.jettAssertDatesWithTolerance(assertionMessage, expectedTimestamp, actualTimestamp);
			}
		}

	}

	@Override
	public Timestamp preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(0).getNameValuePairs();
		if (nvPairs.size() > 1) {
			throw new IllegalStateException("Only expecting one NV pair ... actual size is " + nvPairs.size());
		}

		NameValuePair nvPair = nvPairs.get(0);
		Object inputObject = nvPair.getValue();
		Class inputObjectClass = inputObject.getClass();

		return convertObjectToTimestamp(inputObjectClass, inputObject);
	}

	private Timestamp convertObjectToTimestamp (Class inputObjectClass, Object inputObject) {
		//Accepted input types are java.sql.Timestamp, String and Long
		if (inputObjectClass.equals(Timestamp.class)) {
			return (Timestamp)inputObject;
		} else if (inputObjectClass.equals(Long.class)) {
			return new Timestamp((Long)inputObject);
		} else if (inputObjectClass.equals(String.class)) {
			return (Timestamp)Reflect.getWrappedValue(Timestamp.class, (String) inputObject);
		} else if (inputObjectClass.equals(GregorianCalendar.class)) {
			GregorianCalendar gregorianCalendar = (GregorianCalendar)inputObject;
			return new Timestamp(gregorianCalendar.getTimeInMillis());
		} else {
			throw new IllegalStateException("Expected Value object is not of an accepted type");
		}
	}


}
