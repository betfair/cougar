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

import java.util.List;

public class JavaBeanArrayAssertion implements IAssertion {

	@Override
	public Object[] preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		String arrayObjectClassName = actualObject.getClass().getName().toString().replace("[L", "");
		arrayObjectClassName = arrayObjectClassName.replace(";", "");
		Class arrayObjectClass;
		try {
			arrayObjectClass = Class.forName(arrayObjectClassName);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}

		//If we can create an expected JavaBean instance
		//if not return null
		Object arrayClassObjectInstance;
		try {
			arrayClassObjectInstance = arrayObjectClass.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (NullPointerException e) {
			return null;
		}

		if (expectedObjectMetaData == null) {
			return null;
		}

		if (expectedObjectMetaData.size() == 0) {
			return new Object[0];
		}

		Object[] expectedResultsArray = new Object[expectedObjectMetaData.size()];

		for (int i = 0; i < expectedObjectMetaData.size(); i++) {
			List<NameValuePair> nvPairs = expectedObjectMetaData.getMetaDataAtIndex(i).getNameValuePairs();
			if ((nvPairs.size() == 1) && (nvPairs.get(0).getValue().getClass() == arrayObjectClass)) {
				expectedResultsArray[i] = nvPairs.get(0).getValue();
			} else {
				AggregatedStepExpectedOutputMetaData javaBeanMetaData = new AggregatedStepExpectedOutputMetaData();
				javaBeanMetaData.addMetaData(expectedObjectMetaData.getMetaDataAtIndex(i));
				IAssertion javaBeanAssertion = AssertionProcessorFactory.getAssertionProcessor(DataTypeEnum.BEAN);
				expectedResultsArray[i] = javaBeanAssertion.preProcess(arrayClassObjectInstance, javaBeanMetaData);
			}
		}
		return expectedResultsArray;

	}

	public void execute(String message, Object expectedObject, Object actualObject, AggregatedStepExpectedOutputMetaData expectedMetaData) throws AssertionError {
		if (expectedMetaData == null) {
            nullMetaDataAssert(message, expectedObject, actualObject);
        } else {
            metaDataAssert(message, expectedObject, actualObject, expectedMetaData);
        }
    }

    private void nullMetaDataAssert(String message, Object expectedObject, Object actualObject) throws AssertionError {

        if (actualObject == null) {
            if (expectedObject != null) {
                AssertionUtils.actionFail("Actual object is null, a non null object is expected");
                return;
            }
        }

        if (expectedObject == null) {
            if (actualObject != null) {
                AssertionUtils.actionFail("Actual object is not null, a null object is expected");
                return;
            }
        }

        Object[] actualArrayObject;
		try {
			actualArrayObject = (Object[])actualObject;}
		catch (ClassCastException e) {
			throw new AssertionError("Unable to cast actual object into an Array");
		}

        Object[] expArrayObject;
		try {
			expArrayObject = (Object[])expectedObject;}
		catch (ClassCastException e) {
			throw new AssertionError("Unable to cast expected object into an Array");
		}

        AssertionUtils.jettAssertEquals("Array length check: ", expArrayObject.length, actualArrayObject.length);

        for (int i = 0; i < expArrayObject.length; i++) {
			if (actualArrayObject.length < i) {
                AssertionUtils.actionFail("Row " + i + " missing from actual array");
			} else {
                AssertionUtils.actionPass("Row " + i + " present in array, checking values");
				Object actualJavaBeanObject = actualArrayObject[i];
                Object expJavaBeanObject = expArrayObject[i];

				IAssertion javaBeanAssertion = AssertionProcessorFactory.getAssertionProcessor(DataTypeEnum.BEAN);
				javaBeanAssertion.execute(message, expJavaBeanObject, actualJavaBeanObject, null);

			}

		}



    }


    private void metaDataAssert(String message, Object expectedObject, Object actualObject, AggregatedStepExpectedOutputMetaData expectedMetaData) throws AssertionError {

		Object[] actualArrayObject;
		try {
			actualArrayObject = (Object[])actualObject;}
		catch (ClassCastException e) {
				throw new AssertionError("Unable to cast actual object into an Array");
		}

		if ((actualObject == null) && (expectedMetaData.getValues().size() != 0)) {
            AssertionUtils.actionFail("Actual object array was Null");
		}

        AssertionUtils.jettAssertEquals("Array length check: ", expectedMetaData.getValues().size(), actualArrayObject.length);

		for (int i = 0; i < expectedMetaData.getValues().size(); i++) {
			if (actualArrayObject.length < i) {
                AssertionUtils.actionFail("Row " + i + " missing from actual array");
			} else {
                AssertionUtils.actionPass("Row " + i + " present in array, checking values");
				Object actualJavaBeanObject = actualArrayObject[i];

				IAssertion javaBeanAssertion = AssertionProcessorFactory.getAssertionProcessor(DataTypeEnum.BEAN);
				if ((expectedMetaData.getMetaDataAtIndex(i).size() == 1) && (expectedMetaData.getMetaDataAtIndex(i).getValueAtIndex(0).getClass() == actualJavaBeanObject.getClass())) {
					javaBeanAssertion.execute(message, expectedMetaData.getMetaDataAtIndex(i).getValueAtIndex(0), actualJavaBeanObject, null);
				} else {
					AggregatedStepExpectedOutputMetaData javaBeanMetaData = new AggregatedStepExpectedOutputMetaData();
					javaBeanMetaData.addMetaData(expectedMetaData.getMetaDataAtIndex(i));
					javaBeanAssertion.execute(message, null, actualJavaBeanObject, javaBeanMetaData);
				}
			}

		}

	}

}
