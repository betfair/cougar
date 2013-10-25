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

package com.betfair.cougar.baseline;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.co.LessComplexObjectCO;
import com.betfair.baseline.v2.co.SimpleConnectedObjectCO;
import com.betfair.baseline.v2.co.VeryComplexObjectCO;
import com.betfair.baseline.v2.co.client.SimpleConnectedObjectClientCO;
import com.betfair.baseline.v2.co.client.VeryComplexObjectClientCO;
import com.betfair.baseline.v2.enumerations.TestConnectedObjectsProtocolEnum;
import com.betfair.baseline.v2.enumerations.VeryComplexObjectEnumParameterEnum;
import com.betfair.baseline.v2.to.*;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.client.socket.ClientSubscription;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.platform.virtualheap.*;
import com.betfair.platform.virtualheap.HCollection;
import com.betfair.platform.virtualheap.projection.ScalarProjection;
import com.betfair.platform.virtualheap.updates.UpdateBlock;

import java.io.*;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.betfair.platform.virtualheap.projection.ProjectorFactory.listProjector;
import static com.betfair.platform.virtualheap.projection.ProjectorFactory.objectProjector;

/**
 * Utils for performing connected object testing, includes the full set of tests
 */
public class ConnectedObjectTestingUtils {

    public static final long MAX_PUSH_PROPAGATION_TIME_MS = 1000;
    public static final String startReason = "SUBSCRIPTION_START";
    public static final String logpath = ".//logs//dw//";

    public static TestResults testConnectedObjects(RequestContext ctx, BaselineSyncClient client, TestConnectedObjectsProtocolEnum protocol){
        boolean socketProtocol = false;
        String file = "";
        String fileSuffix = "-cougar-baseline-push-subscription.log";

        try {
            file = logpath + InetAddress.getLocalHost().getHostName() + fileSuffix;
        } catch (Exception e) {
            file = "";
        }

        socketProtocol = (protocol == TestConnectedObjectsProtocolEnum.SOCKET);

        final String simpleObjectUri = "simpleConnectedObject";
        final String simpleListUri = "simpleConnectedList";
        final String complexObjectUri = "complexConnectedObject";

        TestResults ret = new TestResults();
        List<TestResult> results = new ArrayList<TestResult>();
        ret.setResults(results);

        // forcibly initialise the state of the connected objects to a known initial state
        client.updateSimpleConnectedObject(ctx, new SimpleConnectedObject());
        client.updateSimpleConnectedList(ctx, new ArrayList<SimpleConnectedObject>());
        client.updateComplexConnectedObject(ctx, new VeryComplexObject());
        client.closeAllSubscriptions(ctx, simpleObjectUri);
        client.closeAllSubscriptions(ctx, simpleListUri);
        client.closeAllSubscriptions(ctx, complexObjectUri);

        //first pre timestamp
        Date pre = Calendar.getInstance().getTime();

        ConnectedResponse simpleConnectedObjectResponse = client.simpleConnectedObject(ctx);
        Heap simpleConnectedObjectHeap = protocol == TestConnectedObjectsProtocolEnum.IN_PROCESS ? new ImmutableHeap(simpleConnectedObjectResponse.getHeap().getUri(),simpleConnectedObjectResponse.getHeap(),true) : simpleConnectedObjectResponse.getHeap();
        Subscription simpleConnectedObjectSub = simpleConnectedObjectResponse.getSubscription();

        String subId1 = getSubscriptionId(simpleConnectedObjectSub, socketProtocol);

        //try subscription log check
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Testing a new subscription for a simple connected object is logged (redundant if protocol=IN_PROCESS)");

            assertEquals(tr, stepCount++, true, isLogged(file, subId1, simpleObjectUri, startReason, pre, socketProtocol), "Subscription not logged for " + simpleObjectUri);
            results.add(tr);
        }

        ConnectedResponse simpleConnectedListResponse = client.simpleConnectedList(ctx);
        Heap simpleConnectedListHeap = protocol == TestConnectedObjectsProtocolEnum.IN_PROCESS ? new ImmutableHeap(simpleConnectedListResponse.getHeap().getUri(),simpleConnectedListResponse.getHeap(),true) : simpleConnectedListResponse.getHeap();
        Subscription simpleConnectedListSub = simpleConnectedListResponse.getSubscription();

        String subId2 = getSubscriptionId(simpleConnectedListSub, socketProtocol);
        //try subscription log check
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Testing a new subscription for a simple connected object is logged (redundant if protocol=IN_PROCESS)");

            assertEquals(tr, stepCount++, true, isLogged(file, subId2, simpleListUri, startReason, pre, socketProtocol), "Subscription not logged for " + simpleListUri);
            results.add(tr);
        }

        ConnectedResponse complexConnectedObjectResponse = client.complexConnectedObject(ctx);
        Heap complexConnectedObjectHeap = protocol == TestConnectedObjectsProtocolEnum.IN_PROCESS ? new ImmutableHeap(complexConnectedObjectResponse.getHeap().getUri(),complexConnectedObjectResponse.getHeap(),true) : complexConnectedObjectResponse.getHeap();

        Subscription complexConnectedObjectSub = complexConnectedObjectResponse.getSubscription();

        String subId3 = getSubscriptionId(complexConnectedObjectSub, socketProtocol);
        //try subscription log check
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Testing a new subscription for a simple connected object is logged (redundant if protocol=IN_PROCESS)");

            assertEquals(tr, stepCount++, true, isLogged(file, subId3, complexObjectUri, startReason, pre, socketProtocol), "Subscription not logged for " + complexObjectUri);
            results.add(tr);
        }

        // 1. Simple connected object..
        // start with a simple change, from null to a value
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Changing a simple connected object's scalars from null to a value");
            String testId = "Test Id";
            String testMessage = "Test Value";

            //Test Setup
            SimpleConnectedObject testConnectedObject = createSimpleConnectedObject(testId, testMessage);
            SimpleConnectedObjectCO simpleConnectedObjectCO = SimpleConnectedObjectClientCO.rootFrom(simpleConnectedObjectHeap);

            if (simpleConnectedObjectCO.getId() != null) {
                SimpleConnectedObject setupConnectedObject = createSimpleConnectedObject(null, null);
                updateSimpleConnectedObject(client, simpleConnectedObjectHeap, ctx, setupConnectedObject);
            }

            assertEquals(tr, 0, null, simpleConnectedObjectCO.getId(), "TestSetup requires SimpleConnectedObject Scalars = null");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedObject(client, simpleConnectedObjectHeap, ctx, testConnectedObject), "UpdateSimpleConnectedObject('" + testId + "' = '" + testMessage + "')");

            assertNotNull(tr, ++stepCount, simpleConnectedObjectCO.getId(), "After update simpleConnectedObjectCO.id");
            assertNotNull(tr, ++stepCount, simpleConnectedObjectCO.getMessage(), "After update simpleConnectedObjectCO.message");

            assertEquals(tr, ++stepCount, testId, simpleConnectedObjectCO.getId(), "After update simpleConnectedObjectCO.id");
            assertEquals(tr, ++stepCount, testMessage, simpleConnectedObjectCO.getMessage(), "After update simpleConnectedObjectCO.message");

            results.add(tr);
        }
        // now go from one value to another
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Changing a simple connected object's scalars from a value to another value");
            String testId = "Test Id 2";
            String testMessage = "Test Value 2";

            //Test Setup
            SimpleConnectedObject testConnectedObject = createSimpleConnectedObject(testId, testMessage);
            SimpleConnectedObjectCO simpleConnectedObjectCO = SimpleConnectedObjectClientCO.rootFrom(simpleConnectedObjectHeap);

            if (simpleConnectedObjectCO.getId() == null) {
                String setupId = "Setup TestId";
                String setupMessage = "Setup TestValue";

                SimpleConnectedObject setupConnectedObject = createSimpleConnectedObject(setupId, setupMessage);
                updateSimpleConnectedObject(client, simpleConnectedObjectHeap, ctx, setupConnectedObject);
            }

            assertNotNull(tr, 0, simpleConnectedObjectCO.getId(), "TestSetup requires SimpleConnectedObject Scalar = not null");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedObject(client, simpleConnectedObjectHeap, ctx, testConnectedObject), "UpdateSimpleConnectedObject('" + testId + "' = '" + testMessage + "')");

            assertNotNull(tr, ++stepCount, simpleConnectedObjectCO.getId(), "After update simpleConnectedObjectCO.id");
            assertNotNull(tr, ++stepCount, simpleConnectedObjectCO.getMessage(), "After update simpleConnectedObjectCO.message");

            assertEquals(tr, ++stepCount, testId, simpleConnectedObjectCO.getId(), "After update simpleConnectedObjectCO.id");
            assertEquals(tr, ++stepCount, testMessage, simpleConnectedObjectCO.getMessage(), "After update simpleConnectedObjectCO.message");

            results.add(tr);
        }
        // now from one value to null
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Changing a simple connected object's scalars from a value to null");
            String testId = null;
            String testMessage = null;

            //Test Setup
            SimpleConnectedObject testConnectedObject = createSimpleConnectedObject(testId, testMessage);
            SimpleConnectedObjectCO simpleConnectedObjectCO = SimpleConnectedObjectClientCO.rootFrom(simpleConnectedObjectHeap);

            if (simpleConnectedObjectCO.getId() == null) {
                String setupId = "Setup TestId";
                String setupMessage = "Setup TestValue";

                SimpleConnectedObject setupConnectedObject = createSimpleConnectedObject(setupId, setupMessage);
                updateSimpleConnectedObject(client, simpleConnectedObjectHeap, ctx, setupConnectedObject);
            }

            assertNotNull(tr, 0, simpleConnectedObjectCO.getId(), "TestSetup requires SimpleConnectedObject Scalar = not null");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedObject(client, simpleConnectedObjectHeap, ctx, testConnectedObject), "UpdateSimpleConnectedObject('" + testId + "' = '" + testMessage + "')");

            assertEquals(tr, ++stepCount, null, simpleConnectedObjectCO.getId());
            assertEquals(tr, ++stepCount, null, simpleConnectedObjectCO.getMessage());
            results.add(tr);
        }

        // 2. Simple connected list..
        // start with a simple change, adding a row with null values
        List<SimpleConnectedObject> inputList;
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Add a row with null values to a connected list");
            SimpleConnectedObject testObject = createSimpleConnectedObject(null, null);
            inputList = new ArrayList<SimpleConnectedObject>();
            inputList.add(testObject);

            //Test Setup
            HListComplex<SimpleConnectedObjectClientCO> simpleConnectedList = listProjector(objectProjector(SimpleConnectedObjectClientCO.class)).project(simpleConnectedListHeap.getRoot());//SimpleConnectedObjectClientCO.rootFromAsList(simpleConnectedListHeap)

            int expectedListSize = simpleConnectedList.size();

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList), "SimpleConnectedList.Add( null row )");
            expectedListSize++;

            assertEquals(tr, ++stepCount, expectedListSize, simpleConnectedList.size(), "SimpleConnectedList.Count after Add");
            assertEquals(tr, ++stepCount, null, simpleConnectedList.get(expectedListSize-1).getId(), "SimpleConnectedList[" + (expectedListSize-1) + "].id after Add");
            assertEquals(tr, ++stepCount, null, simpleConnectedList.get(expectedListSize-1).getMessage(), "SimpleConnectedList[" + (expectedListSize-1) + "].message after Add");
            results.add(tr);
        }
        // now modify the values in that row..
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Modify a row in a connected list to change null values to actual values");
            inputList = new ArrayList<SimpleConnectedObject>();

            String testId = "Test Id";
            String testMessage = "Test Message";

            SimpleConnectedObject testObject = createSimpleConnectedObject(testId, testMessage);
            inputList = new ArrayList<SimpleConnectedObject>();
            inputList.add(testObject);

            //Test Setup
            HListComplex<SimpleConnectedObjectClientCO> simpleConnectedList = listProjector(objectProjector(SimpleConnectedObjectClientCO.class)).project(simpleConnectedListHeap.getRoot());

            if (simpleConnectedList.size() <1) {
                ArrayList<SimpleConnectedObject> testSetupList = new  ArrayList<SimpleConnectedObject>();
                testSetupList.add(createSimpleConnectedObject(null, null));
                updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, testSetupList);
            }
            else {
                if (simpleConnectedList.get(0).getId() != null) {
                    ArrayList<SimpleConnectedObject> testSetupList = new  ArrayList<SimpleConnectedObject>();
                    testSetupList.add(createSimpleConnectedObject(null, null));
                    updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, testSetupList);
                }
            }

            assertEquals(tr, 0, 1, simpleConnectedList.size(), "TestSetup requires simpleConnectedList.Count = 1");
            assertEquals(tr, 0, null, simpleConnectedList.get(0).getId(), "TestSetup Expects SimpleConnectedList[0].id = null");
            assertEquals(tr, 0, null, simpleConnectedList.get(0).getMessage(), "TestSetup Expects SimpleConnectedList[0].message = nul");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList), "Update simpleConnectedList[0]('" + testId + "' = '" + testMessage + "')");

            assertEquals(tr, ++stepCount, 1, simpleConnectedList.size(), "simpleConnectedList.Count after Update");
            assertEquals(tr, ++stepCount, testId, simpleConnectedList.get(0).getId(), "simpleConnectedList.id after Update");
            assertEquals(tr, ++stepCount, testMessage, simpleConnectedList.get(0).getMessage(),  "simpleConnectedList.message after Update");
            results.add(tr);
        }
        // now add another row
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Add a second row to a connected list");

            String testId = "Testing Id 2";
            String testMessage = "Testing Message 2";

            SimpleConnectedObject testObject = createSimpleConnectedObject(testId, testMessage);

            //Test Setup
            HListComplex<SimpleConnectedObjectClientCO> simpleConnectedList = listProjector(objectProjector(SimpleConnectedObjectClientCO.class)).project(simpleConnectedListHeap.getRoot());

            inputList = getSimpleConnectedList(simpleConnectedList);
            int initialListSize = inputList.size();

            if (initialListSize < 1) {
                inputList.add(createSimpleConnectedObject("TestSetup Id", "TestSetup Message"));
                updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList);
            }

            inputList.add(testObject);

            assertTrue(tr, 0, (simpleConnectedList.size() > 0), "TestSetup requires simpleConnectedList.Count > 0");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList), "simpleConnectedList.Add('" + testId + "' = '" + testMessage + "')");

            assertEquals(tr, ++stepCount, initialListSize + 1, simpleConnectedList.size(), "simpleConnectedList.Count after Add");

            for (int i =0; i < initialListSize; i++) {
                assertEquals(tr, ++stepCount, inputList.get(i).getId(), simpleConnectedList.get(i).getId(), "existing list Item simpleConnectedList[" + i + "].id");
                assertEquals(tr, ++stepCount, inputList.get(i).getMessage(), simpleConnectedList.get(i).getMessage(), "existing list Item simpleConnectedList[" + i + "].message");
            }

            assertEquals(tr, ++stepCount, testId, simpleConnectedList.get(initialListSize).getId(), "added list Item simpleConnectedList[" + initialListSize + "].id");
            assertEquals(tr, ++stepCount, testMessage, simpleConnectedList.get(initialListSize).getMessage(), "added list Item simpleConnectedList[" + initialListSize + "].message");
            results.add(tr);
        }
        // now null the second row and delete the first simpleConnectedObjectHeap
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Set the values in the second row to null and remove the first row");

            String testId = null;
            String testMessage = null;

            //Test Setup
            HListComplex<SimpleConnectedObjectClientCO> simpleConnectedList = listProjector(objectProjector(SimpleConnectedObjectClientCO.class)).project(simpleConnectedListHeap.getRoot());
            inputList = getSimpleConnectedList(simpleConnectedList);
            int initialListSize = inputList.size();

            while(simpleConnectedList.size() < 2) {
                inputList.add(createSimpleConnectedObject("TestSetup Id"+inputList.size(), "a TestSetup Message"+inputList.size()));
                updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList);
            }

            if (inputList.get(0).getId() == null || inputList.get(0).getMessage() == null ||
                    inputList.get(1).getId() == null || inputList.get(1).getMessage() == null) {
                inputList.get(0).setId("TestSetup Id 1");
                inputList.get(0).setMessage("TestSetup Message 1");

                inputList.get(1).setId("TestSetup Id 2");
                inputList.get(1).setMessage("TestSetup Message 2");

                updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList);
            }

            initialListSize = inputList.size();

            assertTrue(tr, 0, (simpleConnectedList.size() > 1), "TestSetup requires simpleConnectedList.Count > 1");
            assertNotNull(tr, 0, simpleConnectedList.get(0).getId(), "TestSetup requires SimpleConnectedList[0].id = not null");
            assertNotNull(tr, 0, simpleConnectedList.get(0).getMessage(), "TestSetup requires SimpleConnectedList[0].id = not null");
            assertNotNull(tr, 0, simpleConnectedList.get(1).getId(), "TestSetup requires SimpleConnectedList[1].message = not null");
            assertNotNull(tr, 0, simpleConnectedList.get(1).getMessage(), "TestSetup requires SimpleConnectedList[1].message = not null");

            //Test Steps
            inputList.get(1).setId(null);
            inputList.get(1).setMessage(null);
            inputList.remove(0);
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList), "Update simpleConnectedList Set(1).id = null, Set(1).message = null, Remove(0)");

            assertEquals(tr, ++stepCount, initialListSize -1, simpleConnectedList.size(), "After Update simpleConnectedList.Count");
            assertEquals(tr, ++stepCount, null, simpleConnectedList.get(0).getId(), "After Update simpleConnectedList[0].id");
            assertEquals(tr, ++stepCount, null, simpleConnectedList.get(0).getMessage(), "After Update simpleConnectedList[0].message");
            results.add(tr);
        }
        // now remove the last row
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Remove the last row in a connected list");

            //Test Setup
            HListComplex<SimpleConnectedObjectClientCO> simpleConnectedList = listProjector(objectProjector(SimpleConnectedObjectClientCO.class)).project(simpleConnectedListHeap.getRoot());
            inputList = getSimpleConnectedList(simpleConnectedList);

            while (simpleConnectedList.size() > 1) {
                inputList.remove(1);
                updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList);
            }

            if (simpleConnectedList.size() < 1) {
                inputList.add(createSimpleConnectedObject("Setup Id1", "Setup Message1"));
                updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList);
            }

            int initialListSize = inputList.size();

            assertEquals(tr, 0, 1, simpleConnectedList.size(), "TestSetup requires simpleConnectedList.Count = 1");

            //Test Steps
            inputList.remove(0);
            assertEquals(tr, ++stepCount, true, updateSimpleConnectedList(client, simpleConnectedListHeap, ctx, inputList), "Remove simpleConnectedList[" + (initialListSize -1) +"]");

            assertEquals(tr, ++stepCount, 0, simpleConnectedList.size(), "After Remove simpleConnectedList.Count");
            results.add(tr);
        }

        // 3. And now for the beast..
        VeryComplexObject complexObject;
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Change an enum parameter in a complex connected object from null to a value");
            VeryComplexObjectEnumParameterEnum testEnumValue = VeryComplexObjectEnumParameterEnum.BAR;

            //Test Setup
            VeryComplexObjectCO complexConnectedObject = VeryComplexObjectClientCO.rootFrom(complexConnectedObjectHeap);
            complexObject = new VeryComplexObject();
            complexObject.setEnumParameter(testEnumValue);

            if (complexConnectedObject.getEnumParameter() != null) {
                updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, null);
            }

            assertEquals(tr, 0, null, complexConnectedObject.getEnumParameter(), "TestSetup requires ComplexConnectedObject.enumParameter = null");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, complexObject), "Update ComplexConnectedObject.enumParameter = " + testEnumValue);

            assertEquals(tr, ++stepCount, testEnumValue.name(), complexConnectedObject.getEnumParameter());
            results.add(tr);
        }
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Change an enum parameter in a complex connected object from a value to another value");
            VeryComplexObjectEnumParameterEnum testEnumValue = VeryComplexObjectEnumParameterEnum.FOOBAR;

            //Test Setup
            VeryComplexObjectCO complexConnectedObject = VeryComplexObjectClientCO.rootFrom(complexConnectedObjectHeap);
            complexObject = new VeryComplexObject();
            complexObject.setEnumParameter(testEnumValue);

            if(complexConnectedObject.getEnumParameter() == null || complexConnectedObject.getEnumParameter().equals(testEnumValue.name()))
            {
                VeryComplexObject setupComplexObject = new VeryComplexObject();

                if (testEnumValue == VeryComplexObjectEnumParameterEnum.BAR) {
                    setupComplexObject.setEnumParameter(VeryComplexObjectEnumParameterEnum.FOOBAR);
                }
                else {
                    setupComplexObject.setEnumParameter(VeryComplexObjectEnumParameterEnum.BAR);
                }

                updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, setupComplexObject);
            }

            assertNotNull(tr, 0, complexConnectedObject.getEnumParameter(), "TestSetup requires ComplexConnectedObject.enumParameter != null");
            assertTrue(tr, 0, !complexConnectedObject.getEnumParameter().equals(testEnumValue.name()), "TestSetup requires complexConnectedObject.enumParameter != " + testEnumValue);

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, complexObject), "Update complexConnectedObject.enumParameter = " + testEnumValue);
            assertEquals(tr, ++stepCount, testEnumValue.name(), complexConnectedObject.getEnumParameter(), "After Update complexConnectedObject.enumParameter");
            results.add(tr);
        }
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Change an enum parameter in a complex connected object from a value to null");

            //Test Setup
            VeryComplexObjectCO complexConnectedObject = VeryComplexObjectClientCO.rootFrom(complexConnectedObjectHeap);
            complexObject = new VeryComplexObject();
            complexObject.setEnumParameter(null);

            if (complexConnectedObject.getEnumParameter() == null) {
                VeryComplexObject setupComplexObject = new VeryComplexObject();
                setupComplexObject.setEnumParameter(VeryComplexObjectEnumParameterEnum.FOOBAR);

                updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, setupComplexObject);
            }

            assertNotNull(tr, 0, complexConnectedObject.getEnumParameter(), "TestSetup requires ComplexConnectedObject.enumParameter != null");

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, complexObject), "Update complexConnectedObj.enumParameter = null");

            assertEquals(tr, ++stepCount, null, complexConnectedObject.getEnumParameter(), "After Update complexConnectedObject.enumParameter");
            results.add(tr);
        }
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Add an item to each top level collection in a complex connected object");

            String testChildObjectKey = "ChildObjectKey 1";
            String testChildObjectItemId1 = "lessComplex Id 1";
            String testChildObjectItemType1 = "lessComplex SimpleType 1";

            LessComplexObject testChildObject = new LessComplexObject();
            testChildObject.setId(testChildObjectItemId1);
            testChildObject.setSimpleType(testChildObjectItemType1);

            complexObject = new VeryComplexObject();

            complexObject.setMap(new HashMap<String, LessComplexObject>());
            complexObject.getMap().put(testChildObjectKey, testChildObject);

            complexObject.setSet(new HashSet<LessComplexObject>());
            complexObject.getSet().add(testChildObject);

            complexObject.setList(new ArrayList<LessComplexObject>());
            complexObject.getList().add(testChildObject);

            //Test Setup
            VeryComplexObjectCO complexConnectedObject = VeryComplexObjectClientCO.rootFrom(complexConnectedObjectHeap);

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, complexObject), "Add complexConnectedObject[" + testChildObjectKey +"]"
                    + ".HashMap / .HashSet / .ArrayList values (" + testChildObjectItemId1 + "," + testChildObjectItemType1 + ")" );

            assertEquals(tr, ++stepCount, 1, getSize(complexConnectedObject.getMap()), "After Add complexConnectedObject.map.Count");
            assertEquals(tr, ++stepCount, testChildObjectItemId1, complexConnectedObject.getMap().get(testChildObjectKey).getId(), "After Add complexConnectedObject.map[" + testChildObjectItemId1 + "].id");
            assertEquals(tr, ++stepCount, testChildObjectItemType1, complexConnectedObject.getMap().get(testChildObjectKey).getSimpleType(), "After Add complexConnectedObject.map[" + testChildObjectItemId1 + "].simpleType");
            assertEquals(tr, ++stepCount, 1, getSize(complexConnectedObject.getSet()), "After Add complexConnectedObject.set.Count");
            assertEquals(tr, ++stepCount, testChildObjectItemId1, complexConnectedObject.getSet().iterator().next().getId(), "After Add complexConnectedObject.set[" + testChildObjectItemId1 + "].id");
            assertEquals(tr, ++stepCount, testChildObjectItemType1, complexConnectedObject.getSet().iterator().next().getSimpleType(), "After Add complexConnectedObject.set[" + testChildObjectItemId1 + "].simpleType");
            assertEquals(tr, ++stepCount, 1, getSize(complexConnectedObject.getList()), "After Add complexConnectedObject.list.Count");
            assertEquals(tr, ++stepCount, testChildObjectItemId1, complexConnectedObject.getList().get(0).getId(), "After Add complexConnectedObject.list[" + testChildObjectItemId1 + "].id");
            assertEquals(tr, ++stepCount, testChildObjectItemType1, complexConnectedObject.getList().get(0).getSimpleType(), "After Add complexConnectedObject.list[" + testChildObjectItemId1 + "].simpleType");
            results.add(tr);
        }
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Simple mutation of each top level collection in a complex connected object");

            String testChildObjectKey = "ChildObjectKey 1";

            String randNumber = "_" + (Math.random() * 100 + 1);

            String testChildObjectMapId = "";
            String testChildObjectMapType = "modified lessComplex Map SimpleType" + randNumber;

            String testChildObjectSetId = "";
            String testChildObjectSetType = "modified lessComplex Set SimpleType"  + randNumber;

            String testChildObjectListId = "";
            String testChildObjectListType = "modified lessComplex List SimpleType"  + randNumber;

            //Test Setup
            VeryComplexObjectCO complexConnectedObject = VeryComplexObjectClientCO.rootFrom(complexConnectedObjectHeap);

            //Get a copy of the Existing VeryComplexObject (so we can query the existing Map, Set & List)
            VeryComplexObject existingVeryComplexObj = getVeryComplexObject(complexConnectedObject);

            //If Existing VeryComplexObject does not contain a MAP, SET or LIST is set to null or does not contain at least 1 item create it (or them) with 1 item
            if (existingVeryComplexObj.getMap().size() < 1 || existingVeryComplexObj.getSet().size() < 1 || existingVeryComplexObj.getList().size() < 1) {
                LessComplexObject testSetupChildObject = new LessComplexObject();
                testSetupChildObject.setId("testSetup Id");
                testSetupChildObject.setSimpleType("testSetup Type");

                if (existingVeryComplexObj.getMap().size() < 1) {
                    existingVeryComplexObj.setMap(new HashMap<String, LessComplexObject>());
                    existingVeryComplexObj.getMap().put(testChildObjectKey, testSetupChildObject);
                }

                if (existingVeryComplexObj.getSet().size() < 1) {
                    existingVeryComplexObj.setSet(new HashSet<LessComplexObject>());
                    existingVeryComplexObj.getSet().add(testSetupChildObject);
                }

                if (existingVeryComplexObj.getList().size() < 1) {
                    existingVeryComplexObj.setList(new ArrayList<LessComplexObject>());
                    existingVeryComplexObj.getList().add(testSetupChildObject);
                }
                updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, existingVeryComplexObj);
            }

            //Check the Test Entry Criteria
            assertTrue(tr, 0, (existingVeryComplexObj.getMap().size() > 0), "TestSetup requires complexConnectedObject.map.Count > 0" );
            assertTrue(tr, 0, (existingVeryComplexObj.getSet().size() > 0), "TestSetup requires complexConnectedObject.set.Count > 0" );
            assertTrue(tr, 0, (existingVeryComplexObj.getList().size() > 0), "TestSetup requires complexConnectedObject.list.Count > 0" );

            //Set the complexObject (used within the UpdateCall) to have existing Keys for the MAP, SET & List, but with modified SimpleTypes
            testChildObjectMapId = complexConnectedObject.getMap().get(complexConnectedObject.getMap().keySet().iterator().next()).getId();
            testChildObjectSetId = complexConnectedObject.getSet().iterator().next().getId();
            testChildObjectListId = complexConnectedObject.getList().get(0).getId();

            LessComplexObject testChildObject = new LessComplexObject();
            testChildObject.setId(testChildObjectMapId);
            testChildObject.setSimpleType(testChildObjectMapType);
            complexObject.setMap(new HashMap<String, LessComplexObject>());
            complexObject.getMap().put(testChildObjectMapId, testChildObject);

            testChildObject = new LessComplexObject();
            testChildObject.setId(testChildObjectSetId);
            testChildObject.setSimpleType(testChildObjectSetType);
            complexObject.setSet(new HashSet<LessComplexObject>());
            complexObject.getSet().add(testChildObject);

            testChildObject = new LessComplexObject();
            testChildObject.setId(testChildObjectListId);
            testChildObject.setSimpleType(testChildObjectListType);
            complexObject.setList(new ArrayList<LessComplexObject>());
            complexObject.getList().add(testChildObject);

            //Test Steps
            assertEquals(tr, ++stepCount, true, updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, complexObject), "Update complexConnectedObject.map/ .set /.list, with new Key and LessComplexObject");

            assertEquals(tr, ++stepCount, existingVeryComplexObj.getMap().size(), getSize(complexConnectedObject.getMap()), "After Update complexConnectedObject.map.Count");
            assertEquals(tr, ++stepCount, testChildObjectMapId, complexConnectedObject.getMap().get(testChildObjectMapId).getId(), "After Updated complexConnectedObject.map["+ testChildObjectMapId +"].id");
            assertEquals(tr, ++stepCount, testChildObjectMapType, complexConnectedObject.getMap().get(testChildObjectMapId).getSimpleType(), "After Updated complexConnectedObject.map["+ testChildObjectMapId +"]simpleType");

            assertEquals(tr, ++stepCount, existingVeryComplexObj.getSet().size(), getSize(complexConnectedObject.getSet()), "After Updated complexConnectedObject.set.Count");
            assertEquals(tr, ++stepCount, testChildObjectSetId, complexConnectedObject.getSet().iterator().next().getId(), "After Updated complexConnectedObject.set[0].id");
            assertEquals(tr, ++stepCount, testChildObjectSetType, complexConnectedObject.getSet().iterator().next().getSimpleType(), "After Updated complexConnectedObject.set[0].type");

            assertEquals(tr, ++stepCount, existingVeryComplexObj.getList().size(), getSize(complexConnectedObject.getList()), "After Updated complexConnectedObject.list.Count");
            assertEquals(tr, ++stepCount, testChildObjectListId, complexConnectedObject.getList().get(0).getId(), "After Updated complexConnectedObject.list[0].id");
            assertEquals(tr, ++stepCount, testChildObjectListType, complexConnectedObject.getList().get(0).getSimpleType(), "After Updated complexConnectedObject.list[0].id");

            results.add(tr);
        }
        {
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Complex mutation of each of those items");

            // map
            LessComplexObject child = new LessComplexObject();
            child.setMap(new HashMap<String, Date>());
            child.setSet(new HashSet<Integer>());
            child.setList(new ArrayList<Double>());
            child.setId("2");
            child.setSimpleType("SimpleType2");
            child.getMap().put("aa", new Date(1000L));
            child.getMap().put("bb", new Date(2000L));
            child.getSet().add(3);
            child.getSet().add(5);
            child.getList().add(1.5);
            child.getList().add(22.5);
            complexObject.setMap(new HashMap<String, LessComplexObject>());
            complexObject.getMap().put("a", child);
            complexObject.getMap().put("b", child);
            complexObject.getMap().put("c", child);
            complexObject.getMap().put("d", child);
            // set
            child = new LessComplexObject();
            child.setMap(new HashMap<String, Date>());
            child.setSet(new HashSet<Integer>());
            child.setList(new ArrayList<Double>());
            child.setId("3");
            child.setSimpleType("SimpleType3");
            child.getMap().put("cc", new Date(Long.MIN_VALUE));
            child.getMap().put("dd", new Date(Long.MAX_VALUE));
            child.getSet().add(Integer.MAX_VALUE);
            child.getSet().add(Integer.MIN_VALUE);
            child.getList().add(Double.MAX_VALUE);
            child.getList().add(Double.MIN_VALUE);
            complexObject.setSet(new HashSet<LessComplexObject>());
            complexObject.getSet().add(child);
            // list
            child = new LessComplexObject();
            child.setMap(new HashMap<String, Date>());
            child.setSet(new HashSet<Integer>());
            child.setList(new ArrayList<Double>());
            child.setId("4");
            child.setSimpleType("SimpleType4");
            child.getMap().put("ee", new Date(1234L));
            long now = System.currentTimeMillis();
            child.getMap().put("ff", new Date(now));
            child.getSet().add(666);
            child.getSet().add(0);
            child.getList().add(0.0);
            child.getList().add(-1.0);
            complexObject.setList(new ArrayList<LessComplexObject>());
            complexObject.getList().add(child);
            complexObject.getList().add(child);
            complexObject.getList().add(child);
            // enum
            complexObject.setEnumParameter(VeryComplexObjectEnumParameterEnum.BAR);
            assertEquals(tr, ++stepCount, true, updateComplexConnectedObject(client, complexConnectedObjectHeap, ctx, complexObject));
            VeryComplexObjectCO complexConnectedObject = VeryComplexObjectClientCO.rootFrom(complexConnectedObjectHeap);

            // map
            assertEquals(tr, ++stepCount, 4, getSize(complexConnectedObject.getMap()));
            assertEquals(tr, ++stepCount, "2", complexConnectedObject.getMap().get("a").getId());
            assertEquals(tr, ++stepCount, "SimpleType2", complexConnectedObject.getMap().get("a").getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("a").getMap()));
            assertEquals(tr, ++stepCount, new Date(1000L), complexConnectedObject.getMap().get("a").getMap().get("aa"));
            assertEquals(tr, ++stepCount, new Date(2000L), complexConnectedObject.getMap().get("a").getMap().get("bb"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("a").getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getMap().get("a").getSet(), 3, 5);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("a").getList()));
            assertEquals(tr, ++stepCount, 1.5, complexConnectedObject.getMap().get("a").getList().get(0));
            assertEquals(tr, ++stepCount, 22.5, complexConnectedObject.getMap().get("a").getList().get(1));
            assertEquals(tr, ++stepCount, "2", complexConnectedObject.getMap().get("b").getId());
            assertEquals(tr, ++stepCount, "SimpleType2", complexConnectedObject.getMap().get("b").getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("b").getMap()));
            assertEquals(tr, ++stepCount, new Date(1000L), complexConnectedObject.getMap().get("b").getMap().get("aa"));
            assertEquals(tr, ++stepCount, new Date(2000L), complexConnectedObject.getMap().get("b").getMap().get("bb"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("b").getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getMap().get("b").getSet(), 3, 5);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("b").getList()));
            assertEquals(tr, ++stepCount, 1.5, complexConnectedObject.getMap().get("b").getList().get(0));
            assertEquals(tr, ++stepCount, 22.5, complexConnectedObject.getMap().get("b").getList().get(1));
            assertEquals(tr, ++stepCount, "2", complexConnectedObject.getMap().get("c").getId());
            assertEquals(tr, ++stepCount, "SimpleType2", complexConnectedObject.getMap().get("c").getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("c").getMap()));
            assertEquals(tr, ++stepCount, new Date(1000L), complexConnectedObject.getMap().get("c").getMap().get("aa"));
            assertEquals(tr, ++stepCount, new Date(2000L), complexConnectedObject.getMap().get("c").getMap().get("bb"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("c").getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getMap().get("c").getSet(), 3, 5);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("c").getList()));
            assertEquals(tr, ++stepCount, 1.5, complexConnectedObject.getMap().get("c").getList().get(0));
            assertEquals(tr, ++stepCount, 22.5, complexConnectedObject.getMap().get("c").getList().get(1));
            assertEquals(tr, ++stepCount, "2", complexConnectedObject.getMap().get("d").getId());
            assertEquals(tr, ++stepCount, "SimpleType2", complexConnectedObject.getMap().get("d").getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("d").getMap()));
            assertEquals(tr, ++stepCount, new Date(1000L), complexConnectedObject.getMap().get("d").getMap().get("aa"));
            assertEquals(tr, ++stepCount, new Date(2000L), complexConnectedObject.getMap().get("d").getMap().get("bb"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("d").getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getMap().get("d").getSet(), 3, 5);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getMap().get("d").getList()));
            assertEquals(tr, ++stepCount, 1.5, complexConnectedObject.getMap().get("d").getList().get(0));
            assertEquals(tr, ++stepCount, 22.5, complexConnectedObject.getMap().get("d").getList().get(1));
            // set
            assertEquals(tr, ++stepCount, 1, getSize(complexConnectedObject.getSet()));
            assertEquals(tr, ++stepCount, "3", complexConnectedObject.getSet().iterator().next().getId());
            assertEquals(tr, ++stepCount, "SimpleType3", complexConnectedObject.getSet().iterator().next().getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getSet().iterator().next().getMap()));
            assertEquals(tr, ++stepCount, new Date(Long.MIN_VALUE), complexConnectedObject.getSet().iterator().next().getMap().get("cc"));
            assertEquals(tr, ++stepCount, new Date(Long.MAX_VALUE), complexConnectedObject.getSet().iterator().next().getMap().get("dd"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getSet().iterator().next().getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getSet().iterator().next().getSet(), Integer.MAX_VALUE, Integer.MIN_VALUE);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getSet().iterator().next().getList()));
            assertEquals(tr, ++stepCount, Double.MAX_VALUE, complexConnectedObject.getSet().iterator().next().getList().get(0));
            assertEquals(tr, ++stepCount, Double.MIN_VALUE, complexConnectedObject.getSet().iterator().next().getList().get(1));
            // list
            assertEquals(tr, ++stepCount, 3, getSize(complexConnectedObject.getList()));
            assertEquals(tr, ++stepCount, "4", complexConnectedObject.getList().get(0).getId());
            assertEquals(tr, ++stepCount, "SimpleType4", complexConnectedObject.getList().get(0).getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(0).getMap()));
            assertEquals(tr, ++stepCount, new Date(1234L), complexConnectedObject.getList().get(0).getMap().get("ee"));
            assertEquals(tr, ++stepCount, new Date(now), complexConnectedObject.getList().get(0).getMap().get("ff"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(0).getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getList().get(0).getSet(), 666, 0);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(0).getList()));
            assertEquals(tr, ++stepCount, 0.0, complexConnectedObject.getList().get(0).getList().get(0));
            assertEquals(tr, ++stepCount, -1.0, complexConnectedObject.getList().get(0).getList().get(1));
            assertEquals(tr, ++stepCount, "4", complexConnectedObject.getList().get(1).getId());
            assertEquals(tr, ++stepCount, "SimpleType4", complexConnectedObject.getList().get(1).getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(1).getMap()));
            assertEquals(tr, ++stepCount, new Date(1234L), complexConnectedObject.getList().get(1).getMap().get("ee"));
            assertEquals(tr, ++stepCount, new Date(now), complexConnectedObject.getList().get(1).getMap().get("ff"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(1).getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getList().get(1).getSet(), 666, 0);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(1).getList()));
            assertEquals(tr, ++stepCount, 0.0, complexConnectedObject.getList().get(1).getList().get(0));
            assertEquals(tr, ++stepCount, -1.0, complexConnectedObject.getList().get(1).getList().get(1));
            assertEquals(tr, ++stepCount, "4", complexConnectedObject.getList().get(2).getId());
            assertEquals(tr, ++stepCount, "SimpleType4", complexConnectedObject.getList().get(2).getSimpleType());
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(2).getMap()));
            assertEquals(tr, ++stepCount, new Date(1234L), complexConnectedObject.getList().get(2).getMap().get("ee"));
            assertEquals(tr, ++stepCount, new Date(now), complexConnectedObject.getList().get(2).getMap().get("ff"));
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(2).getSet()));
            stepCount += assertList(tr, stepCount, complexConnectedObject.getList().get(2).getSet(), 666, 0);
            assertEquals(tr, ++stepCount, 2, getSize(complexConnectedObject.getList().get(2).getList()));
            assertEquals(tr, ++stepCount, 0.0, complexConnectedObject.getList().get(2).getList().get(0));
            assertEquals(tr, ++stepCount, -1.0, complexConnectedObject.getList().get(2).getList().get(1));
            // enum
            assertEquals(tr, ++stepCount, "BAR", complexConnectedObject.getEnumParameter());
            results.add(tr);
        }
        // 4. Subscription lifecycle tests
        // check we've only got one sub to each heap
        {
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Pre-check each heap has only one subscription");

            assertEquals(tr, ++stepCount, 1, client.getNumSubscriptions(ctx, "simpleConnectedObject"));
            assertEquals(tr, ++stepCount, 1, client.getNumSubscriptions(ctx, "simpleConnectedList"));
            assertEquals(tr, ++stepCount, 1, client.getNumSubscriptions(ctx, "complexConnectedObject"));
            results.add(tr);
        }

        //Get another pre-timestamp
        Date preClose = Calendar.getInstance().getTime();

        // terminate a sub from the client side and ensure it's got through to the server side
        {
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Client side subscription termination");

            simpleConnectedObjectSub.close();
            // give time to propagate up..
            sleep(1000);



            assertEquals(tr, ++stepCount, Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER, simpleConnectedObjectSub.getCloseReason());
            assertEquals(tr, ++stepCount, 0, client.getNumSubscriptions(ctx, "simpleConnectedObject"));
            // make sure we haven't affected the other live subs
            assertEquals(tr, ++stepCount, 1, client.getNumSubscriptions(ctx, "simpleConnectedList"));
            assertEquals(tr, ++stepCount, 1, client.getNumSubscriptions(ctx, "complexConnectedObject"));
            results.add(tr);
        }

        //check subscription log for sub close
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Testing a subscription closed by client is logged (redundant if protocol=IN_PROCESS)");

            assertEquals(tr, stepCount++, true, isLogged(file, subId1, simpleObjectUri, Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER.name(), preClose, socketProtocol), "Subscription Close not logged for " + simpleObjectUri);
            results.add(tr);
        }

        // terminate a sub from the server side and ensure it's got through to the client side
        {
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Server side subscription termination");

            client.closeAllSubscriptions(ctx, "simpleConnectedList");
            // give time to propagate up..
            sleep(1000);

            assertEquals(tr, ++stepCount, Subscription.CloseReason.REQUESTED_BY_PUBLISHER, simpleConnectedListSub.getCloseReason());
            assertEquals(tr, ++stepCount, 0, client.getNumSubscriptions(ctx, "simpleConnectedList"));
            // make sure we haven't affected the other live sub
            assertEquals(tr, ++stepCount, 1, client.getNumSubscriptions(ctx, "complexConnectedObject"));
            results.add(tr);
        }

        //check subscription log for sub close
        {
            //Test Data
            int stepCount = 0;
            TestResult tr = new TestResult();
            tr.setDescription("Testing a subscription closed by server is logged (redundant if protocol=IN_PROCESS)");

            assertEquals(tr, stepCount++, true, isLogged(file, subId2, simpleListUri, Subscription.CloseReason.REQUESTED_BY_PUBLISHER.name(), preClose, socketProtocol), "Subscription Close not logged for " + simpleListUri);
            results.add(tr);
        }

        boolean success = true;
        for (TestResult tr : results) {
            success = success && (tr.getSuccess() != null && tr.getSuccess());
        }
        ret.setSuccess(success);

        return ret;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static boolean updateSimpleConnectedObject(BaselineSyncClient client, Heap heap, RequestContext ctx, SimpleConnectedObject simpleConnectedObject) {
        final CountDownLatch latch = new CountDownLatch(1);
        heap.addListener(new HeapListener() {
            @Override
            public void applyUpdate(UpdateBlock update) {
                latch.countDown();
            }
        }, false);
        client.updateSimpleConnectedObject(ctx, simpleConnectedObject);
        //  return false;

        try {
            return latch.await(MAX_PUSH_PROPAGATION_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }

    }

    private static boolean updateSimpleConnectedList(BaselineSyncClient client, Heap heap, RequestContext ctx, List<SimpleConnectedObject> inputList) {
        final CountDownLatch latch = new CountDownLatch(1);
        heap.addListener(new HeapListener() {
            @Override
            public void applyUpdate(UpdateBlock update) {
                latch.countDown();
            }
        }, false);
        client.updateSimpleConnectedList(ctx, inputList);
        try {
            return latch.await(MAX_PUSH_PROPAGATION_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private static boolean updateComplexConnectedObject(BaselineSyncClient client, Heap heap, RequestContext ctx, VeryComplexObject complexObject) {
        final CountDownLatch latch = new CountDownLatch(1);
        heap.addListener(new HeapListener() {
            @Override
            public void applyUpdate(UpdateBlock update) {
                latch.countDown();
            }
        }, false);
        client.updateComplexConnectedObject(ctx, complexObject);
        try {
            return latch.await(MAX_PUSH_PROPAGATION_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private static <T> int assertList(TestResult tr, int stepCount, HListScalar<T> set, T... expectedContent) {
        List<T> actualValues = new ArrayList<T>();
        for (T sn : set) {
            actualValues.add(sn);
        }
        for (T value : expectedContent) {
            boolean existed = actualValues.remove(value);
            assertTrue(tr, ++stepCount, existed, "Couldn't find value " + value + " in set");
        }

        return stepCount;
    }

    private static int getSize(HCollection node) {
        if (node == null) {
            return 0;
        }
        return node.size();
    }

    private static void assertEquals(TestResult tr, int stepCount, Object expected, Object actual) {
        if(actual instanceof ScalarProjection) {
            assertEquals(tr, stepCount, expected, ((ScalarProjection)actual).get(), "");
        } else {
            assertEquals(tr, stepCount, expected, actual, "");
        }

    }

    private static void assertEquals(TestResult tr, int stepCount, Object expected, Object actual, String failInfo) {
        if (tr.getSuccess() != null && !tr.getSuccess()) {
            return;
        }

        boolean worked;
        if (expected == null && actual == null) {
            worked = true;
        }
        else if (expected == null || actual == null) {
            worked = false;
        }
        else {
            worked = expected.equals(actual);
        }

        tr.setSuccess(worked);

        if (!worked) {
            tr.setStep(stepCount);
            if (expected instanceof Date) {
                tr.setFailText(failInfo + " | expected='" + expected + "' ("+((Date)expected).getTime()+"), actual='" + actual + "'("+(actual!=null?((Date)actual).getTime():0)+") |");
            }
            else {
                tr.setFailText(failInfo + " | expected='" + expected + "', actual='" + actual + "' |");
            }
        }
    }

    private static void assertNotNull(TestResult tr, int stepCount, Object actual, String failInfo) {
        if (tr.getSuccess() != null && !tr.getSuccess()) {
            return;
        }

        boolean worked = actual != null ? true : false;

        tr.setSuccess(worked);

        if (!worked) {
            tr.setStep(stepCount);
            tr.setFailText(failInfo + " | expected='not null', actual='" + actual + "' |");
        }
    }

    private static void assertTrue(TestResult tr, int stepCount, boolean worked, String failText) {
        if (tr.getSuccess() != null && !tr.getSuccess()) {
            return;
        }
        tr.setSuccess(worked);
        if (!worked) {
            tr.setStep(stepCount);
            tr.setFailText(failText + " | expected='true', actual='false' |");
        }
    }

    private static SimpleConnectedObject createSimpleConnectedObject(String id, String message) {
        SimpleConnectedObject ret = new SimpleConnectedObject();
        ret.setId(id);
        ret.setMessage(message);
        return ret;
    }

    // utils for playing with connected objects..

    private static interface ObjectConverter<S, T> {
        void convert(S src, T target);
    }

    private static interface IdSource<S> {
        String getId(S src);
    }

    private static ObjectConverter<Date, ScalarProjection<Date>> dateConverter = new ObjectConverter<Date, ScalarProjection<Date>>() {
        @Override
        public void convert(Date src, ScalarProjection<Date> target) {
            target.set(src);
        }
    };

    private static ObjectConverter<Double, ScalarProjection<Double>> doubleConverter = new ObjectConverter<Double, ScalarProjection<Double>>() {
        @Override
        public void convert(Double src, ScalarProjection<Double> target) {
            target.set(src);
        }
    };

    private static IdSource<Double> doubleIdSource = new IdSource<Double>() {
        @Override
        public String getId(Double src) {
            return String.valueOf(src);
        }
    };

    private static IdSource<ScalarProjection<Double>> doubleProjectionIdSource = new IdSource<ScalarProjection<Double>>() {

        @Override
        public String getId(ScalarProjection<Double> src) {
            return String.valueOf(src.get());
        }
    };

    private static ObjectConverter<Integer, ScalarProjection<Integer>> intConverter = new ObjectConverter<Integer, ScalarProjection<Integer>>() {
        @Override
        public void convert(Integer src, ScalarProjection<Integer> target) {
            target.set(src);
        }
    };

    private static IdSource<Integer> intIdSource = new IdSource<Integer>() {
        @Override
        public String getId(Integer src) {
            return String.valueOf(src);
        }
    };

    private static IdSource<ScalarProjection<Integer>> intProjectionIdSource = new IdSource<ScalarProjection<Integer>>() {
        @Override
        public String getId(ScalarProjection<Integer> src) {
            return String.valueOf(src.get());
        }
    };

    static ObjectConverter<SimpleConnectedObject, SimpleConnectedObjectCO> simpleConnectedObjectConverter = new ObjectConverter<SimpleConnectedObject, SimpleConnectedObjectCO>() {
        @Override
        public void convert(SimpleConnectedObject src, SimpleConnectedObjectCO target) {
            target.setId(src.getId());
            target.setMessage(src.getMessage());
        }
    };

    static IdSource<SimpleConnectedObject> simpleConnectedObjectIdSource = new IdSource<SimpleConnectedObject>() {
        @Override
        public String getId(SimpleConnectedObject src) {
            return src.getId();
        }
    };


    static IdSource<SimpleConnectedObjectCO> simpleConnectedObjectCOIdSource = new IdSource<SimpleConnectedObjectCO>() {
        @Override
        public String getId(SimpleConnectedObjectCO src) {
            return src.getId();
        }
    };

    static ObjectConverter<LessComplexObject, LessComplexObjectCO> lessComplexObjectConverter = new ObjectConverter<LessComplexObject, LessComplexObjectCO>() {
        @Override
        public void convert(LessComplexObject src, LessComplexObjectCO target) {
            target.setId(src.getId());
            target.setSimpleType(src.getSimpleType());
            updateList(src.getList(), target.getList());
            updateList(src.getSet(), target.getSet());
            updateMap(src.getMap(), target.getMap());
        }
    };

    static IdSource<LessComplexObject> lessComplexObjectIdSource = new IdSource<LessComplexObject>() {
        @Override
        public String getId(LessComplexObject src) {
            return src.getId();
        }
    };

    static IdSource<LessComplexObjectCO> lessComplexObjectProjectionIdSource = new IdSource<LessComplexObjectCO>() {
        @Override
        public String getId(LessComplexObjectCO src) {
            return src.getId();
        }
    };

    @SuppressWarnings(value = "unchecked")
    static <T, C> void updateList(Iterable<T> sourceList, HListComplex<? extends C> targetList, ObjectConverter<T, C> converter, IdSource<T> srcIdSource, IdSource<C> targetIdSource) {
        // a list can have duplicate values, including the id field
        Map<String, List<C>> idMap = new HashMap<String, List<C>>();
        for (C obj : targetList) {
            List<C> valueList = idMap.get(targetIdSource.getId(obj));
            if (valueList == null) {
                valueList = new ArrayList<C>();
                idMap.put(targetIdSource.getId(obj), valueList);
            }
            valueList.add(obj);
        }
        if (sourceList != null) {
            for (T obj : sourceList) {
                List<C> toUpdateList = idMap.remove(srcIdSource.getId(obj));
                if (toUpdateList == null) {
                    toUpdateList = new ArrayList<C>();
                }
                if (toUpdateList.isEmpty()) {
                    toUpdateList.add(targetList.addLast());
                }
                for (C toUpdate : toUpdateList) {
                    converter.convert(obj, toUpdate);
                }
            }

        }
        for (List<C> toDeleteList : idMap.values()) {
            for (C toDelete : toDeleteList) {
                ((HListComplex) targetList).remove(toDelete);
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    static <T> void updateList(Iterable<T> sourceList, HListScalar<T> targetList) {
        // a list can have duplicate values, including the id field, alas this won't work too well for scalar lists..
        Map<String, List<T>> idMap = new HashMap<String, List<T>>();
        for (T obj : targetList) {
            List<T> valueList = idMap.get(String.valueOf(obj));
            if (valueList == null) {
                valueList = new ArrayList<T>();
                idMap.put(String.valueOf(obj), valueList);
            }
            valueList.add(obj);
        }
        if (sourceList != null) {
            for (T obj : sourceList) {
                List<T> toUpdateList = idMap.remove(String.valueOf(obj));
                if (toUpdateList == null) {
                    toUpdateList = new ArrayList<T>();
                }
                if (toUpdateList.isEmpty()) {
                    targetList.addLast(obj);
                }
            }

        }
        for (List<T> toDeleteList : idMap.values()) {
            for (T toDelete : toDeleteList) {
                ((HListScalar) targetList).remove(toDelete);
            }
        }
    }

    static <T> void updateMap(Map<String, T> srcMap, HMapScalar<T> targetMap) {
        Map<String, T> currentTargetMap = new HashMap<String, T>();
        for (String s : targetMap.keySet()) {
            currentTargetMap.put(s, targetMap.get(s));
        }
        if (srcMap != null) {
            for (String s : srcMap.keySet()) {
                T src = srcMap.get(s);
                T target = currentTargetMap.remove(s);
                if (target == null) {
                    targetMap.put(s, src);
                }
            }
        }
        for (String s : currentTargetMap.keySet()) {
            targetMap.remove(s);
        }
    }

    static <T, C> void updateMap(Map<String, T> srcMap, HMapComplex<? extends C> targetMap, ObjectConverter<T, C> converter) {
        Map<String, C> currentTargetMap = new HashMap<String, C>();
        for (String s : targetMap.keySet()) {
            currentTargetMap.put(s, targetMap.get(s));
        }
        if (srcMap != null) {
            for (String s : srcMap.keySet()) {
                T src = srcMap.get(s);
                C target = currentTargetMap.remove(s);
                if (target == null) {
                    target = targetMap.put(s);
                }
                converter.convert(src, target);
            }
        }
        for (String s : currentTargetMap.keySet()) {
            targetMap.remove(s);
        }
    }

    private static ArrayList<SimpleConnectedObject> getSimpleConnectedList(HListComplex<SimpleConnectedObjectClientCO> simpleConnectedList) {
        ArrayList<SimpleConnectedObject> existingData = new ArrayList<SimpleConnectedObject>();

        for (SimpleConnectedObjectClientCO c : simpleConnectedList) {
            existingData.add( createSimpleConnectedObject( c.getId(), c.getMessage()) );
        }

        return existingData;
    }

    private static VeryComplexObject getVeryComplexObject(VeryComplexObjectCO complexCO) {
        VeryComplexObject existingObject = new VeryComplexObject();

        if (complexCO.getMap() != null) {
            existingObject.setMap(new HashMap<String, LessComplexObject>());

            for(String mapKey :  complexCO.getMap().keySet())
            {
                LessComplexObject tempLessComplexObject = new LessComplexObject();

                tempLessComplexObject.setId(complexCO.getMap().get(mapKey).getId());
                tempLessComplexObject.setSimpleType(complexCO.getMap().get(mapKey).getSimpleType());

                existingObject.getMap().put(mapKey, tempLessComplexObject);
            }
        }

        if (complexCO.getSet() != null) {
            existingObject.setSet(new HashSet<LessComplexObject>());

            for (LessComplexObjectCO curObject : complexCO.getSet())
            {
                LessComplexObject tempLessComplexObject = new LessComplexObject();

                tempLessComplexObject.setId(curObject.getId());
                tempLessComplexObject.setSimpleType(curObject.getSimpleType());
                existingObject.getSet().add(tempLessComplexObject);
            }
        }

        if (complexCO.getList() != null) {
            existingObject.setList(new ArrayList<LessComplexObject>());

            for (int i = 0; i < getSize(complexCO.getList()); i++) {
                LessComplexObject tempLessComplexObject = new LessComplexObject();

                tempLessComplexObject.setId(complexCO.getList().get(i).getId());
                tempLessComplexObject.setSimpleType(complexCO.getList().get(i).getSimpleType());

                existingObject.getList().add(tempLessComplexObject);
            }
        }

        return existingObject;
    }

    //read from log file and check that the subscriptions are logged
    private static boolean isLogged(String file, String subscriptionId, String connectedObjName, String action, Date pre, boolean socketProtocol) {
        if (!socketProtocol) {
            return true;
        }

        try {
            int i=0;
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));


            String strLine;
            ArrayList<String> logList = new ArrayList<String>();
            //Read File Line By Line, get last written log output
            while ((strLine = br.readLine()) != null) {
                logList.add(strLine);
            }

            strLine = logList.get(logList.size()-1);

            // Print the content on the console
            System.out.println (strLine);

            String[] split = strLine.split(",");

            //check each value in the logged output
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date timestamp = (Date)formatter.parse(split[0]);
            Date now = Calendar.getInstance().getTime();

            if (timestamp.after(now) || timestamp.before(pre)) {
                return false;
            }

            if (!split[1].trim().equals(subscriptionId)) {
                return false;
            }

            if (!split[2].trim().equals(connectedObjName)) {
                return false;
            }

            if (!split[3].trim().equals(action)) {
                return false;
            }

            //Close the input stream
            in.close();
        }
        catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            return false;
        }

        return true;
    }

    private static String getSubscriptionId(Subscription sub, boolean socketProtocol) {
        if (socketProtocol) {
            if (sub instanceof ClientSubscription) {
                return ((ClientSubscription) sub).getSubscriptionId();
            }
        }
        return "";
    }

}
