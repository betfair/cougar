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
import com.betfair.testing.utils.cougar.misc.StepMetaData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JSONObjectAssertion implements IAssertion {


    @Override
	public Object preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		if (ObjectUtil.isJSONObject(expectedObjectMetaData
                .getMetaDataAtIndex(0).getValueAtIndex(0).getClass())) {
			return (JSONObject) expectedObjectMetaData.getMetaDataAtIndex(0)
					.getValueAtIndex(0);
		} else {
			String jsonString = "";
			AggregatedStepExpectedOutputMetaData metaData = expectedObjectMetaData;

			for (StepMetaData stepMetaData : metaData.getValues()) {
				for (NameValuePair nameValuePair : stepMetaData
						.getNameValuePairs()) {
					Object value = nameValuePair.getValue();
					Object key = nameValuePair.getName();
					if ((key != null) && (value != null)) {
						jsonString = jsonString + String.valueOf(value);
					}
				}
			}

			try {
				return new JSONObject(jsonString);
			} catch (JSONException e) {
				throw new AssertionError(e);
			}
		}
		//return this;
	}

	@Override
	public void execute(String message, Object passedExpObject, Object passedActObject, AggregatedStepExpectedOutputMetaData outputMetaData) throws AssertionError {

		JSONObject expJObject;
		JSONObject actJObject;

		try {
			expJObject = (JSONObject) passedExpObject;
		} catch (ClassCastException e) {
            AssertionUtils.actionFail("Expected object is not a JSONObject, which the Actual object is.");
			return;
		}

		try {
			actJObject= (JSONObject) passedActObject;
		} catch (ClassCastException e) {
            AssertionUtils.actionFail("Actual object is not a JSONObject, which the Expected object is.");
			return;
		}

		// Only bother asserting invidiual objects if JSONStrings do not match
		if (!expJObject.toString().equals(actJObject.toString())) {

            AssertionUtils.jettAssertEquals("Check JSONObject: Number of keys: ", expJObject.length(), actJObject.length());

			try {
				for (Iterator expIter = expJObject.keys(); expIter.hasNext();) {
					String expKey = expIter.next().toString();

					if (!actJObject.has(expKey)) {
                        AssertionUtils.actionFail(
								"Check JSONObject: expected to contain key: <"
										+ expKey + "> but doesn't.");
						continue;
					} else {
                        AssertionUtils.actionPass(
								"Check JSONObject: contains key: <" + expKey
										+ "> as expected");
						Object expContainedObject = expJObject.get(expKey);
						Object actContainedObject = actJObject.get(expKey);
						if (expContainedObject.getClass() != actContainedObject
								.getClass()) {
                            AssertionUtils.actionFail("Check JSONObject: Contained object '"
									+ expKey + "' expected to be of type: <"
									+ expContainedObject.getClass().getName()
									+ "> but was: <"
									+ actContainedObject.getClass().getName()
									+ ">");
							continue;
						} else {
                            AssertionUtils.actionPass("Check JSONObject: Contained object '"
									+ expKey + "' of type: <"
									+ expContainedObject.getClass().getName()
									+ ">");
							if (expContainedObject instanceof JSONObject) {
								iterateJSONObject(expKey,
										(JSONObject) expContainedObject,
										(JSONObject) actContainedObject);
							} else if (expContainedObject instanceof JSONArray) {
								iterateJSONArray(expKey,
										(JSONArray) expContainedObject,
										(JSONArray) actContainedObject);
							} else {
                                AssertionUtils.jettAssertEquals(
										"Check JSONObject: Contained object '" + expKey + "' value: ",
										expContainedObject, actContainedObject);
							}
						}
					}
				}
			} catch (JSONException e) {
				throw new AssertionError(e);
			}
		} else {
            AssertionUtils.jettAssertEquals(
					"Check JSONObject (using JSONString): ", expJObject.toString(),
					actJObject.toString());
		}
	}

	private void iterateJSONObject(String key, JSONObject expJObject,
			JSONObject actJObject)
			throws AssertionError {

        AssertionUtils.jettAssertEquals("Check JSONObject: Contained JSONObject '" + key + "' number of keys: ", expJObject.length(), actJObject.length());

		// Only bother asserting invidiual objects if JSONStrings do not match
		if (!expJObject.toString().equals(actJObject.toString())) {

			try {
				for (Iterator expIter = expJObject.keys(); expIter.hasNext();) {
					String expKey = expIter.next().toString();

					if (!actJObject.has(expKey)) {
                        AssertionUtils.actionFail(
								"Check JSONObject: Contained JSONObject '" + key + "' expected to contain key: <"
										+ expKey + "> but doesn't.");
						continue;
					} else {
                        AssertionUtils.actionPass(
								"Check JSONObject: Contained JSONObject '" + key + "' contains key: <" + expKey
										+ ">");
						Object expContainedObject = expJObject.get(expKey);
						Object actContainedObject = actJObject.get(expKey);
						if (expContainedObject.getClass() != actContainedObject
								.getClass()) {
                            AssertionUtils.actionFail("Check JSONObject: Contained JSONObject '" + key + "': Contained Object '"
									+ expKey + "' expected to be of type: <"
									+ expContainedObject.getClass().getName()
									+ "> but was: <"
									+ actContainedObject.getClass().getName()
									+ ">");
							continue;
						} else {
                            AssertionUtils.actionPass("Check JSONObject: Contained JSONObject '" + key + "': Contained Object '"
									+ expKey + " of type: <"
									+ expContainedObject.getClass().getName()
									+ ">");
							if (expContainedObject instanceof JSONObject) {
								iterateJSONObject(expKey,
										(JSONObject) expContainedObject,
										(JSONObject) actContainedObject);
							} else if (expContainedObject instanceof JSONArray) {
								iterateJSONArray(expKey,
										(JSONArray) expContainedObject,
										(JSONArray) actContainedObject);
							} else {
                                AssertionUtils.jettAssertEquals("Check JSONObject: Contained JSONObject '" + key + "': Contained Object '"
										+ expKey + " value: ",
										expContainedObject, actContainedObject);
							}
						}
					}
				}
			} catch (JSONException e) {
				throw new AssertionError(e);
			}
		} else {
            AssertionUtils.jettAssertEquals("Check JSONObject:  Contained JSONObject '" + key
					+ "' (using JSONString): ", expJObject.toString(), actJObject
					.toString());
		}
	}

	private void iterateJSONArray(String key, JSONArray expJArray,
			JSONArray actJArray) throws AssertionError {

        AssertionUtils.jettAssertEquals("Check JSONObject: Contained JSONArray '" + key + "' number of entries: ", expJArray.length(),
				actJArray.length());
		try {
			for (int i = 0; i < expJArray.length(); i++) {
				if (i < actJArray.length()) {

					Object expContainedObject = expJArray.get(i);
					Object actContainedObject = actJArray.get(i);
					String expKey = "JSONArray '" + key + "' Entry " + i;

					if (expContainedObject.getClass() != actContainedObject
							.getClass()) {
                        AssertionUtils.actionFail(
								"Check JSONObject: Contained JSONArray '" + key + "': Contained Object: "
										+ expKey
										+ " expected to be of type: <"
										+ expContainedObject.getClass()
												.getName()
										+ "> but was: <"
										+ actContainedObject.getClass()
												.getName() + ">");
						continue;
					} else {
                        AssertionUtils.actionPass(
								"Check JSONObject: Contained JSONArray '" + key + "': Contained Object: "
										+ expKey
										+ " of type: <"
										+ expContainedObject.getClass()
												.getName() + ">");
						if (expContainedObject instanceof JSONObject) {
							iterateJSONObject(expKey,
									(JSONObject) expContainedObject,
									(JSONObject) actContainedObject);
						} else if (expContainedObject instanceof JSONArray) {
							iterateJSONArray(expKey,
									(JSONArray) expContainedObject,
									(JSONArray) actContainedObject);
						} else {
                            AssertionUtils.jettAssertEquals(
									"Check JSONObject: Contained JSONArray '" + key + "': Contained Object: "
										+ expKey
										+ " value: ",
									expContainedObject, actContainedObject);
						}
					}

				}
			}
		} catch (JSONException e) {
			throw new AssertionError(e);
		} finally {
		}

	}

}
