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
import com.betfair.testing.utils.cougar.misc.StepMetaData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaBeanAssertion implements IAssertion {

	public Object preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError {

		//Class<?> returnType = bean.getInjectorBean().getReturnType();

		//if we cannot get the actual objects class return null
		if (actualObject == null) {
			return null;
		}

		Class<?> returnType = actualObject.getClass();

			if ((expectedObjectMetaData==null) || (expectedObjectMetaData.getValues() == null) || (expectedObjectMetaData.getValues().size() == 0)) {
				return null;
			}

			if ((expectedObjectMetaData.getMetaDataAtIndex(0).size() == 1) && (expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0) != null) && (expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass().equals(returnType))) {
				return expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
			} else {
				//try to create instance to return, if cannot return null
				Object expectedResponse;
				try {
					expectedResponse = returnType.newInstance();
				} catch (InstantiationException e) {
					return null;
				} catch (IllegalAccessException e) {
					return null;
				}


				List<StepMetaData> stepMetaDataList = expectedObjectMetaData.getValues();
				for (StepMetaData stepMetaData : stepMetaDataList) {
					List<NameValuePair> nvPairList = stepMetaData.getNameValuePairs();
					for (int i = 0 ; i < nvPairList.size() ; i++) {
						NameValuePair nvPair = nvPairList.get(i);
						ArrayList<Object> propList = new ArrayList<Object>();
						propList.add(nvPair.getName());
                        Reflect.setValueToProperty(propList, nvPair.getValue(), expectedResponse);
					}
				}

				//bean.setTransformedExpectedResponse(expectedResponse);
				return expectedResponse;
			}

	}
	/**
	 *
	 * Asserts that each field in the expectedResponse java bean matches the
	 * value held in the actualResponse java bean.
	 * @throws AssertionError
	 * @throws com.betfair.jett.exceptions.JETTException
	 * @throws com.betfair.assertions.asserter.exceptions.AssertionUtilityException
	 *
	 */
	public void execute(String message, Object expectedBean, Object actualBean, AggregatedStepExpectedOutputMetaData expectedMetaData) throws AssertionError {


		if ((expectedMetaData != null)) {

			if (actualBean == null) {
				Boolean isExpectedObjectNull = false;

				if (expectedMetaData == null) {
					isExpectedObjectNull = true;
				} else if (expectedMetaData.size()==1) {
					if (expectedMetaData.getMetaDataAtIndex(0) == null) {
						isExpectedObjectNull = true;
					}
				}

				if (isExpectedObjectNull == false) {
                    AssertionUtils.actionFail("Actual object is <null>, checking if Expected Object is null.  Expected object not null.");
				} else {
                    AssertionUtils.actionPass("Actual object is <null>, checking if Expected Object is null.  Expected object is null.");
				}

			}
            else {
                throw new IllegalStateException("EEEK - better find out what the jett code below was trying to achieve..");
            }
//            else if ((expectedMetaData.getMetaDataAtIndex(0).size() == 1) && (expectedMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass().equals(bean.getInjectorBean().getReturnType()))) {
//
//				Object expectedBeanFromMetaData = expectedMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);
//
//				String errorMessage = actualBean.getClass().getName() + " - Check Bean:";
//                AssertionUtils.multiAssertEquals(errorMessage.toString(),expectedBeanFromMetaData, actualBean);
//
//			} else {
//
//				if (actualBean==null) {
//
//					if (expectedMetaData.getValues().get(0).getNameValuePairs().size() > 0) {
//                        AssertionUtils.actionFail(actualBean.getClass().getName() + " - Check Bean: Actual Bean is null");
//					} else {
//                        AssertionUtils.actionPass(actualBean.getClass().getName() + " - Check Bean: Bean is null as expected");
//					}
//
//				} else {
//
//					List<StepMetaData> stepMetaDataList = expectedMetaData.getValues();
//					if (stepMetaDataList.size() > 1) {
//						throw new JETTFailFastException("There should only be one line of Expected Result data for asserting Javabeans.");
//					}
//
//					StepMetaData stepMetaData = stepMetaDataList.get(0);
//
//					List<NameValuePair> nvPairList = stepMetaData.getNameValuePairs();
//					for (int i = 0 ; i < nvPairList.size() ; i++) {
//
//						NameValuePair nvPair = nvPairList.get(i);
//
//						Object p = nvPair.getName();
//						ArrayList<Object> propList = new ArrayList<Object>();
//						propList.add(p);
//
//						StringBuffer errorMessageBuffer = new StringBuffer();
//
//						errorMessageBuffer.append(actualBean.getClass().getName() + " - Check Field '" + p + "':");
//
//						Object expectedValue = null;
//						Object actualValue = null;
//						actualValue = Reflect.getPropertyValue(propList, actualBean);
//
//						if (actualValue == null) {
//                            AssertionUtils.jettAssertNull(errorMessageBuffer.toString() + " Actual value was null, checking Expected value: ", nvPair.getValue());
//							continue;
//						}
//
//						if (nvPair.getValue() == null) {
//                            AssertionUtils.jettAssertNull(errorMessageBuffer.toString() + " Expected value is null, checking Actual value: ", actualValue);
//							continue;
//						}
//
//						DataTypeEnum type;
//						type = ObjectUtil.resolveType(actualValue);
//						switch(type) {
//						case JAVA_DOT_LANG_OBJECT:
//						case STRING:
//							try {
//								expectedValue = Reflect.getWrappedValue(actualValue.getClass(), nvPair.getValue());
//							} catch (Exception e) {
//                                AssertionUtils.actionFail("Unable to convert expected result to actual object class.  Expected object Class: " + nvPair.getValue().getClass() + ", Value: " + nvPair.getValue() + ". Actual object Class: " + actualValue.getClass() + ", Value: " + actualValue + ". " + e.getMessage());
//								continue;
//							}
//                            AssertionUtils.multiAssertEquals(errorMessageBuffer.toString(),expectedValue, actualValue);
//							break;
//						default:
//							expectedValue = nvPair.getValue();
//                            AssertionUtils.actionPass(errorMessageBuffer.toString() + " - Field Present - Checking values:");
//							IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(type);
//							assertionProcessor.execute(expectedValue, actualValue, null);
//							break;
//						}
//					}
//				}
//			}
		} else {

			if (actualBean == null) {
                AssertionUtils.jettAssertNull("Check Bean: Actual value was null, Expected value is: " + expectedBean, expectedBean);
				return;
			}

			if (expectedBean == null) {
                AssertionUtils.jettAssertNull(actualBean.getClass().getName() + " - Check Bean: Expected value is null, Actual value is " + actualBean, actualBean);
				return;
			}

			Class expBeanClass = expectedBean.getClass();
			Class actBeanClass = actualBean.getClass();

			if (expBeanClass != actBeanClass) {
                AssertionUtils.actionFail("Object is not of the correct type.  Expected <" + expBeanClass.getName() + "> but was <" + actBeanClass.getName() + ">");
				return;
			}

			Method[] beanMethods = expBeanClass.getDeclaredMethods();
			for (Method beanMethod: beanMethods) {
				int numberOfArgs = beanMethod.getParameterTypes().length;
				String methodName = beanMethod.getName();
				if ((numberOfArgs==0) && (methodName.startsWith("get"))) {

					String compareString = actualBean.getClass().getName() + " - Check Field '" + methodName.substring(3) + "':";

					try {
						Object expectedValue = beanMethod.invoke(expectedBean, (Object[])null);
						Object actualValue = beanMethod.invoke(actualBean, (Object[])null);

						if ((expectedValue==null) || (actualValue==null)) {
                            AssertionUtils.jettAssertEquals(compareString.toString(),expectedValue, actualValue);
						} else {
							DataTypeEnum type;
							type = ObjectUtil.resolveType(actualValue);
							switch(type) {
							case JAVA_DOT_LANG_OBJECT:
							case STRING:
								Object castedExpectedObj = Reflect.getWrappedValue(actualValue.getClass(), expectedValue);
                                AssertionUtils.jettAssertEquals(compareString.toString(),castedExpectedObj, actualValue);
								break;
							default:
                                AssertionUtils.actionPass(compareString.toString() + " - Field Present - Checking values:");
								IAssertion assertionProcessor = AssertionProcessorFactory.getAssertionProcessor(type);
								assertionProcessor.execute(message, expectedValue, actualValue, null);
								break;
							}
						}
					} catch (IllegalArgumentException e) {
						throw new AssertionError(e);
					} catch (IllegalAccessException e) {
						throw new AssertionError(e);
					} catch (InvocationTargetException e) {
						throw new AssertionError(e);
					}



				}
			}


		}
	}
}
