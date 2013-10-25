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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Asserts {@link java.util.Set} objects, actual and expected elements are compared according to their respective get*() values
 * this implementation uses {@link com.betfair.assertions.general.assertion.ArrayListAssertion} in the  {@link #preProcess(Object, com.betfair.assertions.core.beans.AggregatedStepExpectedOutputMetaData)}  }
 *
 * @see com.betfair.assertions.general.assertion.ArrayListAssertion
 */
public class SetAssertion implements IAssertion {

    private ArrayListAssertion arrayListAssertion = new ArrayListAssertion();

    /**
     * @param actualObject     4
     * @param expectedMetaData including stepmetadata list in which namevalue pairs exist.
     * @return Set object containing elements derived from expectedObjectMetaData
     * @throws com.betfair.jett.exceptions.JETTFailFastException
     * @see com.betfair.assertions.general.assertion.ArrayListAssertion#preProcess(Object, com.betfair.assertions.core.beans.AggregatedStepExpectedOutputMetaData)
     * @see com.betfair.assertions.general.assertion.ArrayListAssertion#preProcess(Object, com.betfair.assertions.core.beans.AggregatedStepExpectedOutputMetaData)
     */
    @Override
    public Set<Object> preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedMetaData)
        throws AssertionError {

        // check metadata is empty
        if (null == expectedMetaData || expectedMetaData.size() == 0 || expectedMetaData.getData().size() == 0
            || expectedMetaData.getData().get(0).size() == 0) {

            return new LinkedHashSet();
        }

        ArrayList listActual = new ArrayList();

        if (actualObject != null) {
            Set set = (Set) actualObject;
            listActual.addAll(set);
        }
        Set resultSet = new LinkedHashSet ();

        List preProcess =this.arrayListAssertion.preProcess(listActual, expectedMetaData);
        resultSet.addAll(preProcess);
        return resultSet;

    }

    /**
     * executes default null, size equivalency, boundrary checks
     *
     * @param expectedObject
     * @param actualObject
     * @return false if conditions are not met, otherwise true
     * @throws com.betfair.assertions.asserter.exceptions.AssertionUtilityException
     */
    private boolean doNullAndSizeCheck(Object expectedObject, Object actualObject)
        throws AssertionError {


        if (actualObject == null && expectedObject == null) {
            AssertionUtils.jettAssertTrue("Expected object is <Null>" +
                ", checking if Actual Object is <null>.  Actual object " + actualObject, true);
            return false;

        }
        Set setActual = (Set) actualObject;

        if (expectedObject == null && actualObject != null) {
            AssertionUtils.jettAssertFalse("Expected object is <Null>" +
                ", checking if Actual Object is <null>.  Actual object " + actualObject, true);
            return false;
        }

        //expectedObject is implicity an arraylist
        Set listExpected = (Set) expectedObject;

        if (actualObject == null && (expectedObject != null && listExpected.size() > 0)) {
            AssertionUtils.jettAssertFalse("Expected object is " + expectedObject + " " +
                ", checking if Actual Object is <null>.  Actual object " + actualObject, true);
            return false;
        }

        if ((actualObject != null && setActual.size() > 0) && expectedObject == null) {
            AssertionUtils.jettAssertFalse("Actual object is " + actualObject + " " +
                ", checking if Expected Object is <null>.  Expected object " + expectedObject, true);
            return false;
        }

        if ((actualObject == null || setActual.size() == 0) && (expectedObject == null || listExpected.size() == 0)) {
            AssertionUtils.jettAssertTrue("Actual object is empty " + actualObject + " " +
                ", checking if Expected Object is empty.  Expected object " + expectedObject, true);
            return false;
        }


        // size of the containers's are not same
        if (setActual.size() != listExpected.size()) {
            AssertionUtils.jettAssertFalse("Actual object is " + setActual + " " +
                ", checking if Expected entry size is not equal. Expected object " + listExpected, true);
            return false;
        }

        return true;

    }


    /**
     * compares expected and actual set elements according to their respective get*() function return values
     *
     *
     * @param expected
     * @param actual
     * @return
     */
    private void validateSets(Set expected, Set actual)
        throws AssertionError {

        Object nextExpected;
        Iterator iterExpected = expected.iterator();
        while (iterExpected.hasNext()) {
            nextExpected = iterExpected.next();
            validateObjects(actual, nextExpected);
        }

    }


    /**
     * compares given two primitive objects by calling {@link Object#equals(Object)} } function of expected object
     *
     * @param actual
     * @param expected
     * @return
     */
    private boolean validatePrimitiveField(Object actual, Object expected) {
        if ((expected == null) && (actual == null)) {
            return true;
        }
        if (!expected.toString().equals(actual.toString())) {
            return false;
        }

        return true;

    }

    /**
     * invokes given method for each actual and expected object,
     * then delegates to {@link #validatePrimitiveField(Object, Object)} to check reflected values
     *
     * @param method
     * @param actual
     * @param expected
     * @return
     * @throws AssertionError
     */
    private boolean validatePrimitiveField(Method method, Object actual, Object expected) throws AssertionError {
        // check primitive values
        Object expReflected = Reflect.invokeReflection(method, expected, new Object[]{});
        Object actReflected = Reflect.invokeReflection(method, actual, new Object[]{});
        return validatePrimitiveField(actReflected, expReflected);
    }


    /**
     * validation entry point for Complex java types (BEAN)
     * invokes given method for each actual and expected object, after null checks,
     * execution is delegated to
     *
     * @param method
     * @param actual
     * @param expected
     * @return
     * @throws AssertionError
     */
    private boolean validateBeanField(Method method, Object actual, Object expected)
        throws AssertionError {

        Object expReflected = Reflect.invokeReflection(method, expected, new Object[]{});
        Object actReflected = Reflect.invokeReflection(method, actual, new Object[]{});
        if ((expReflected == null) && (actReflected == null)) {
            return true;
        }
        if (expReflected == null && actReflected != null) {
            return false;
        }

        if (expReflected != null && actReflected == null) {
            return false;
        }

        if (expReflected == actReflected) {
            return true;
        }

        if (expReflected.equals(actReflected)) {
            return true;
        }

        // check if it is a collection
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            throw new AssertionError("Set of collections are not implemented so cannot check collection types, " +
                "Actual value " + actReflected + " Expected value "+ expReflected );
        }

        if (Map.class.isAssignableFrom(method.getReturnType())) {
            throw new AssertionError("Set of maps are not implemented so cannot check collection types, " +
                "Actual value " + actReflected + " Expected value "+ expReflected );
        }

        return validateObject(actReflected, expReflected);
    }

    /**
     * main validation entry point for get*() functions, validation is branched according to  method returnType,
     * validation is forwarded to whether
     *
     * @param method
     * @param actual
     * @param expected
     * @return
     * @throws AssertionError
     */
    private boolean validateField(Method method, Object actual, Object expected)
        throws AssertionError {

        DataTypeEnum type = ObjectUtil.resolveType(method.getReturnType());
        if (!type.equals(DataTypeEnum.JAVA_DOT_LANG_OBJECT) && !type.equals(DataTypeEnum.STRING)
            && !method.getReturnType().isPrimitive()) {

            return validateBeanField(method, actual, expected);
        }

        return validatePrimitiveField(method, actual, expected);
    }

    /**
     * main validation function for expected/actual object comparison
     * if actual object is primitive, java.dot.lang.* or String then {@link #validatePrimitiveField(Object, Object)} is
     * used for comparison, otherwise get* () methods of expected object are used for comparison
     *
     * @param actual
     * @param expected
     * @return true if every get*() function value of expected object matches to each field value of actual object respectively.
     * @throws AssertionError
     */
    private boolean validateObject(Object actual, Object expected)
        throws AssertionError {

        // check if primitive or String or java lang object
        DataTypeEnum type = ObjectUtil.resolveType(actual.getClass());
        if (type.equals(DataTypeEnum.JAVA_DOT_LANG_OBJECT) || type.equals(DataTypeEnum.STRING)
            || actual.getClass().isPrimitive()) {
            return validatePrimitiveField(actual, expected);
        }


        Method[] expectedMethods = actual.getClass().getDeclaredMethods();
        Class clazz = actual.getClass();
        // check every field matches
        // condition every field value must match
        boolean isMatched = false;
        for (Method m : expectedMethods) {
            int numberOfArgs = m.getParameterTypes().length;
            String methodName = m.getName();
            if ((numberOfArgs != 0) || (!methodName.startsWith("get"))) {
                continue;
            }
            if (!matchFieldName(methodName, clazz)) {
                // raise a failure
                AssertionUtils.jettAssertFalse(" Expected field name " + methodName + " not found ", true);
                return false;
            }


            isMatched = validateField(m, actual, expected);
            if (!isMatched) {
                return false;
            }

        }
        return isMatched;
    }

    /**
     * tries to to find a similiar object of expected one in the given actual list
     *
     *
     * @param actual
     * @param expected
     * @throws AssertionError
     */
    private void validateObjects(Set actual, Object expected)
        throws AssertionError {


        Iterator iterActual = actual.iterator();
        boolean valuesMatched = false;
        Object nextElement;
        while (iterActual.hasNext()) {
            nextElement = iterActual.next();
            valuesMatched = validateObject(nextElement, expected);
            if (!valuesMatched) {
                continue;
            }

            AssertionUtils.jettAssertTrue("Expected object " + expected + " found in the actual set " +
                actual, true);
            return;
        }

        if (!valuesMatched) {
            AssertionUtils.jettAssertFalse("Expected object " + expected + " not found in the actual set " +
                actual, true);
        }

    }

    /**
     * checks a given property name exist in the given clazz
     *
     * @param propertyName
     * @param clazz        a class to scan
     * @return true if, given propertyName matches one of the clazz's properties, otherwise false
     */
    private boolean matchFieldName(String propertyName, Class clazz) {
        //check existance of the method name in the actual object
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            int numberOfArgs = m.getParameterTypes().length;
            String methodName = m.getName();
            if ((numberOfArgs != 0) || (!methodName.startsWith("get"))) {
                continue;
            }
            if (!methodName.equals(propertyName)) {
                continue;
            }
            return true;
        }
        return false;
    }


    /**
     *
     * @param message
     * @param expectedObject
     * @param actualObject
     * @param expectedMetaData
     * @throws AssertionError
     */
    @Override
    public void execute(String message, Object expectedObject, Object actualObject,
                        AggregatedStepExpectedOutputMetaData expectedMetaData)
        throws AssertionError {

        //do some initial checks going further
        if (!doNullAndSizeCheck(expectedObject, actualObject)) {
            return;
        }

        Set setActual = (Set) actualObject;

        Set setExpected = (Set) expectedObject;

        validateSets(setExpected, setActual);
    }


}
