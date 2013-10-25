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

/*
 * This class does not fit with the standard assertion pattern and purely exists to cater
 * for the scenario where the object being asserted is NULL and the type is unknown, hence
 * we do not know how to build the expected object.
 */
public class NullObjectAssertion implements IAssertion {

	@Override
	public void execute(String message, Object expectedObject, Object actualObject, AggregatedStepExpectedOutputMetaData expectedMetaData) throws AssertionError {

		Boolean isExpectedObjectNull = false;

		if (expectedMetaData == null) {
			isExpectedObjectNull = true;
		} else if (expectedMetaData.size()==1) {
			if (expectedMetaData.getMetaDataAtIndex(0) == null) {
				isExpectedObjectNull = true;
			} else if (expectedMetaData.getMetaDataAtIndex(0).size() == 1) {
				if (expectedMetaData.getMetaDataAtIndex(0).getValueAtIndex(0) == null) {
					isExpectedObjectNull = true;
				}
			}
		}

		if (expectedObject != null) {
			isExpectedObjectNull = false;
		}

		if (isExpectedObjectNull == false) {
            AssertionUtils.actionFail("Method Return parameter is <null>, checking if Expected Object is null.  Expected object not null.");
		} else {
            AssertionUtils.actionPass("Method Return parameter is <null>, checking if Expected Object is null.  Expected object is null.");
		}

	}

	@Override
	public Object preProcess(Object actuaObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {
		throw new IllegalAccessError("This method should never get called");
	}

}
