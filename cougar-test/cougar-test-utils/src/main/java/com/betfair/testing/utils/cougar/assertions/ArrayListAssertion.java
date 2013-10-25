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

import java.util.ArrayList;
import java.util.List;

public class ArrayListAssertion implements IAssertion {

	@Override
	public void execute(String message, Object expectedObject, Object actualObject, AggregatedStepExpectedOutputMetaData expectedMetaData) throws AssertionError {

		if (expectedMetaData != null) {

			if (actualObject == null) {

				Boolean isExpectedObjectNull = false;

				if (expectedMetaData == null) {
					isExpectedObjectNull = true;
				}

				if (isExpectedObjectNull == false) {
                    AssertionUtils.actionFail("Actual object is <null>, checking if Expected Object is null.  Expected object not null.");
				} else {
                    AssertionUtils.actionPass("Actual object is <null>, checking if Expected Object is null.  Expected object is null.");
				}

			} else {

				ArrayList<Object> actualList = (ArrayList<Object>)actualObject;

				//If metadata holds a list and not raw meta data
				if ((expectedMetaData.size() == 1) && (expectedMetaData.getMetaDataAtIndex(0).size() == 1)) {
					if (expectedMetaData.getMetaDataAtIndex(0).getValueAtIndex(0) == null) {
						ArrayList<Object> expectedListFromMetaData = null;
						assertPassedLists(expectedListFromMetaData, actualList);
					}
                    else {
                        throw new IllegalStateException("EEEK - better find out what the jett code below was trying to achieve..");
                    }
//                    else if (expectedMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass().equals(bean.getInjectorBean().getReturnType())) {
//						ArrayList<Object> expectedListFromMetaData = (ArrayList<Object>)expectedMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
//						assertPassedLists(expectedListFromMetaData, actualList);
//					}
				//If expected list is empty list
				} else if ((expectedMetaData.size()==1) && (expectedMetaData.getMetaDataAtIndex(0).size()==0)) {
                    AssertionUtils.jettAssertEquals(actualList.getClass().getName() + " - Check Size:", 0, actualList.size());
				} else {

					List<StepMetaData> stepMetaDataList = expectedMetaData.getValues();
                    AssertionUtils.jettAssertEquals(actualList.getClass().getName() + " - Check Size:", stepMetaDataList.size(), actualList.size());
					int rowCounter = 0;
					for (StepMetaData stepMetaData : stepMetaDataList) {

						int rowReportCounter = rowCounter + 1;

						StringBuffer errorMessageBuffer = new StringBuffer();
						errorMessageBuffer.append(actualList.getClass().getName() + " - Check Row '" + rowReportCounter + "':");

						Object actualValue = null;
						if (actualList.size() > rowCounter) {
							actualValue = actualList.get(rowCounter);
						} else {
                            AssertionUtils.actionFail(actualList.getClass().getName() + " - Check Row '" + rowReportCounter + "': Row Missing");
							break;
						}

						Object expectedValue = null;
						if ((stepMetaData.size() == 1) && (stepMetaData.getValueAtIndex(0) == null)) {
                            AssertionUtils.jettAssertNull(actualList.getClass().getName() + " - Check Entry '" + rowReportCounter + "': Expecting null. Checking actual is null. ", actualValue);
						} else if (actualValue == null) {
                            AssertionUtils.actionFail(actualList.getClass().getName() + " - Check Entry '" + rowReportCounter + "': Expecting something, but actual was null.");
						} else {
							//Build expectedValue object based on actualValue class
                            AssertionUtils.actionPass(errorMessageBuffer.toString());
							AggregatedStepExpectedOutputMetaData expectedValueMetaData = new AggregatedStepExpectedOutputMetaData();
							expectedValueMetaData.addMetaData(expectedMetaData.getMetaDataAtIndex(rowCounter));
							DataTypeEnum actualValueDataType = ObjectUtil.resolveType(actualValue);
							IAssertion expectedValueAsserter = AssertionProcessorFactory.getAssertionProcessor(actualValueDataType);
							expectedValue = expectedValueAsserter.preProcess(actualValue, expectedValueMetaData);
							expectedValueAsserter.execute(message, expectedValue, actualValue, expectedValueMetaData);
						}
						rowCounter++;
					}

					//Report on unexpected entries in List
					for (int i = expectedMetaData.size(); i < actualList.size(); i++) {
						Object unexpectedEntry = actualList.get(i);
                        AssertionUtils.actionFail("Unexpected ArrayList Entry Found: " + unexpectedEntry.toString());
					}
				}
			}
		} else {
			ArrayList<Object> expectedList = (ArrayList<Object>)expectedObject;
			ArrayList<Object> actualList = (ArrayList<Object>)actualObject;
			assertPassedLists(expectedList, actualList);
		}
	}

	@Override
	public ArrayList<Object> preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		if (expectedObjectMetaData == null) {
			return null;
		}

		if (expectedObjectMetaData.size() == 0) {
			return new ArrayList<Object>();
		}

		ArrayList<Object> expectedResultsList = new ArrayList<Object>();

		Boolean isExpectedResultAlreadyArrayList = false;

		if (expectedObjectMetaData.getMetaDataAtIndex(0).size() == 1) {
			if ((expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0)==null) || (expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass().equals(ArrayList.class))) {
				isExpectedResultAlreadyArrayList = true;
			}
		}

		if (isExpectedResultAlreadyArrayList) {
			//expectedResultsMap = (HashMap<String, Object>)bean.getExpectedOutputMetaData().getMetaDataAtIndex(0).getValueAtIndex(0);
			return (ArrayList)expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
		} else {

			ArrayList actualArrayList = (ArrayList)actualObject;
			expectedResultsList = new ArrayList<Object>();

			//Bit poor but have to ensure all objects are of the same type
			Boolean allContainedObjectsTheSame = true;
			if (actualArrayList != null) {
				if (actualArrayList.size() > 0) {
					Object previousObject = actualArrayList.get(0);
					for (int i = 1; i < actualArrayList.size(); i++) {
						Object currentObject = actualArrayList.get(i);
						if (previousObject.getClass() != currentObject.getClass()) {
							allContainedObjectsTheSame = false;
							break;
						}
						previousObject = currentObject;
					}
				}
			}

			if ((actualArrayList != null) && (actualArrayList.size()!=0) && (allContainedObjectsTheSame)) {
				DataTypeEnum type = ObjectUtil.resolveType(actualArrayList.get(0));

				switch(type) {
				case JAVA_DOT_LANG_OBJECT:
				case STRING:
					for (StepMetaData stepMetaData: expectedObjectMetaData.getValues()) {
						Object castedExpectedObj = Reflect.getWrappedValue(actualArrayList.get(0).getClass(), stepMetaData.getValueAtIndex(0));
						expectedResultsList.add(castedExpectedObj);
					}
					break;
				default:
					for (StepMetaData stepMetaData: expectedObjectMetaData.getValues()) {
						AggregatedStepExpectedOutputMetaData metaDataToPass = new AggregatedStepExpectedOutputMetaData();
						metaDataToPass.addMetaData(stepMetaData);
						IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(type);
						expectedResultsList.add(assertionProcessor.preProcess(actualArrayList.get(0), metaDataToPass));
					}
					break;
				}
			} else {
				if (expectedObjectMetaData != null) {
					//Check if array list should be empty
					if ((expectedObjectMetaData.size()==1) && (expectedObjectMetaData.getMetaDataAtIndex(0).size()==0)) {
						//Do nothing leave list empty
					} else {
						for (StepMetaData stepMetaData: expectedObjectMetaData.getValues()) {
							expectedResultsList.add(stepMetaData.getValueAtIndex(0));
						}
					}
				} else {
					expectedResultsList = null;
				}
			}
		}

		return expectedResultsList;

	}

	private void assertPassedLists(ArrayList<Object> expectedList, ArrayList<Object> actualList) throws AssertionError {
		if ((expectedList == null) || (actualList == null)) {
            AssertionUtils.jettAssertEquals("Checking ArrayList: ", expectedList, actualList);
			return;
		}

        AssertionUtils.jettAssertEquals("Check number of entries in ArrayList: ", expectedList.size(), actualList.size());

		int assertionCoutner = 0;
		for (Object expectedListEntity: expectedList) {
			Object actualListEntity = actualList.get(assertionCoutner);

			String compareMessage = "Checking ArrayList Entry '" + assertionCoutner + "' ";

			if ((expectedListEntity==null) || (actualListEntity==null)) {
                AssertionUtils.jettAssertEquals(compareMessage + "value: ",expectedListEntity, actualListEntity);
			} else {
				DataTypeEnum type;
				type = ObjectUtil.resolveType(actualListEntity);
				switch(type) {
				case JAVA_DOT_LANG_OBJECT:
				case STRING:
					Object castedExpectedObj = Reflect.getWrappedValue(actualListEntity.getClass(), expectedListEntity);
                    AssertionUtils.jettAssertEquals(compareMessage + "value: ",castedExpectedObj, actualListEntity);
					break;
				default:
                    AssertionUtils.actionPass(compareMessage + "- Present - Checking values:");
					IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(type);
					assertionProcessor.execute(compareMessage, expectedListEntity, actualListEntity, null);
					break;
				}
			}
			assertionCoutner++;
		}

		//Report on unexpected entries in List
		for (int i = assertionCoutner; i < actualList.size(); i++) {
			Object unexpectedEntry = actualList.get(i);
            AssertionUtils.actionFail("Unexpected ArrayList Entry Found: " + unexpectedEntry.toString());
		}

	}




}
