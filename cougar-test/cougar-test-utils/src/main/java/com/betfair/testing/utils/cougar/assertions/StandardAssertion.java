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
import com.betfair.testing.utils.cougar.misc.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

public class StandardAssertion implements IAssertion {

	public Object preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) {

		List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(0).getNameValuePairs();
		if (nvPairs.size() > 1) {
			throw new IllegalStateException("Only expecting one NV pair ... actual size is " + nvPairs.size());
		} else if (nvPairs.size() == 0) {
			throw new IllegalStateException("No expected result data passed");
		}

		NameValuePair nvPair = nvPairs.get(0);

		//Class<?> returnType = bean.getInjectorBean().getReturnType();

		//If actual object is null, cannot determine was to reflect expected object to
		//So return unreflected object.
		if (actualObject == null) {
			return nvPair.getValue();
		}

		Class<?> returnType = actualObject.getClass();
		Object propertyValue = Reflect.getWrappedValue(returnType, nvPair.getValue());

		return propertyValue;
	}

	@Override
	public void execute(String message, Object passedExpectedValue, Object passedActValue, AggregatedStepExpectedOutputMetaData outputMetaData) throws AssertionError {

		Object expectedValue = passedExpectedValue;
		Object actualValue = passedActValue;

		if (actualValue==null) {
			AssertionUtils.jettAssertNull(message+": Actual is object is null, checking if expected object is null.",expectedValue);
		} else {

			String errorMessage = "Return parameter";

			if (ObjectUtil.isTimeStamp(passedActValue.getClass())) {
				if (ObjectUtil.isTimeStamp(passedExpectedValue.getClass())) {
					AssertionUtils.jettAssertDatesWithTolerance(message+": "+errorMessage, (Timestamp) expectedValue, (Timestamp) actualValue);
				} else {
                    throw new AssertionError(
							"When asserting Timestamps the expectedValue must also be a Timestamp");
				}

			} else {
				//TODO - Revisit
				//Objects imported from test data are read in as strings, so if expected object is
				//a string then cast it.  Not very safe.
				Object castedExpObj;
				//if (expectedValue.getClass() == String.class) {
				if (expectedValue==null) {
					AssertionUtils.jettAssertNull("Expected object is null, check if actual object is null.",actualValue);
				} else {


					if (expectedValue.getClass() != actualValue.getClass()) {
						castedExpObj = Reflect.getWrappedValue(actualValue.getClass(), expectedValue);
					} else {
						castedExpObj = expectedValue;
					}
				//} else {
				//	castedExpObj = expectedValue;
				//}
                    if (castedExpObj.getClass().equals(actualValue.getClass())) {

                        if (String.class == castedExpObj.getClass()) {

                            // Only bother checking JSON if the strings are not equal
                            if (!castedExpObj.equals(actualValue)) {

                                try {
                                    // try JSON String comparison (e.g. doesn't care about the order of keys)
                                    ObjectMapper mapper = new ObjectMapper();

                                    JsonNode expObj = mapper.readTree((String) castedExpObj);
                                    JsonNode actualObj = mapper.readTree((String) actualValue);

                                    assertEquals(message, expObj, actualObj);

                                } catch (JsonProcessingException e) {
                                    // not JSON, try normal String comparison
                                    assertEquals(message, castedExpObj, actualValue);
                                } catch (IOException e) {
                                    fail(message);
                                }

                            } // if the Strings are equal, the assertion has passed

                        } else {
                            // not String, normal object equality
                            assertEquals(message, castedExpObj, actualValue);
                        }

                    } else {
    					AssertionUtils.jettAssertEquals(message + ": " + errorMessage, castedExpObj, actualValue);
                    }
				}
			}
		}
	}
}