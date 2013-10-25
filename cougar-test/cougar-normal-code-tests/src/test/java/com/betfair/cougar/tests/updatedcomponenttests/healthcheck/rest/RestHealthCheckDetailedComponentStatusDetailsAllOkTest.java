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

// Originally from UpdatedComponentTests/HealthCheck/Rest/Rest_HealthCheck_Detailed_ComponentStatusDetails_AllOk.xls;
package com.betfair.cougar.tests.updatedcomponenttests.healthcheck.rest;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Ensure that when a Cougar container is running a service, the heathcheck detailed operation returns correct Status Details for each component that has a status set in service. (All OK)
 */
public class RestHealthCheckDetailedComponentStatusDetailsAllOkTest {

    @Test
    public void v3() throws Exception {
        // Set up the Http Call Bean to make the baseline service request
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean1 = cougarManager1.getNewHttpCallBean();
        cougarManager1 = cougarManager1;
        getNewHttpCallBean1.setOperationName("setHealthStatusInfo");

        getNewHttpCallBean1.setServiceName("baseline", "cougarBaseline");

        getNewHttpCallBean1.setVersion("v2");
        // Set the component statuses to be set (All OK)
        getNewHttpCallBean1.setRestPostQueryObjects(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<message><initialiseHealthStatusObject>true</initialiseHealthStatusObject><serviceStatusDetail>OK</serviceStatusDetail><DBConnectionStatusDetail>OK</DBConnectionStatusDetail><cacheAccessStatusDetail>OK</cacheAccessStatusDetail></message>".getBytes())));
        // Make the REST call to the set the health statuses
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean1, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Set up the Http Call Bean to make the healthcheck service request
        HttpCallBean getNewHttpCallBean7 = cougarManager1.getNewHttpCallBean();

        getNewHttpCallBean7.setOperationName("getDetailedHealthStatus", "detailed");

        getNewHttpCallBean7.setServiceName("healthcheck");

        getNewHttpCallBean7.setVersion("v3");

        getNewHttpCallBean7.setNameSpaceServiceName("Health");
        // Make the REST call to the get the health statuses from the health service
        cougarManager1.makeRestCougarHTTPCall(getNewHttpCallBean7, com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum.RESTXML, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum.XML);
        // Get the xml response and grab all the HealthDetail entries
        HttpResponseBean response3 = getNewHttpCallBean7.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        Document xmlResponse = (Document) response3.getResponseObject();
        AssertionUtils.multiAssertEquals((int) 200, response3.getHttpStatusCode());
        AssertionUtils.multiAssertEquals("OK", response3.getHttpStatusText());

        NodeList nodeList = xmlResponse.getElementsByTagName("HealthDetailResponse");
        Node healthDetailResponseNode = nodeList.item(0);
        // Get the HealthDetail entry for the Baseline service version 2.8
        XMLHelpers xmlHelpers = new XMLHelpers();
        // Get the subComponentList from the HealthDetail entry for Baseline
        Node baselineSubComponentList = xmlHelpers.getSpecifiedChildNode(healthDetailResponseNode, "subComponentList");
        // Get the Cache entry from the subComponentList and check the value of the status field is OK
        Node baselineCacheComponent = xmlHelpers.getNodeContainingSpecifiedChildNodeFromParent(baselineSubComponentList, "name", "Cache1");
        String status = xmlHelpers.getTextContentFromChildNode(baselineCacheComponent, "status");
        AssertionUtils.multiAssertEquals("OK", status);
        // Get the Service entry from the subComponentList and check the value of the status field is OK
        Node baselineServiceComponent = xmlHelpers.getNodeContainingSpecifiedChildNodeFromParent(baselineSubComponentList, "name", "Service1");
        status = xmlHelpers.getTextContentFromChildNode(baselineServiceComponent, "status");
        AssertionUtils.multiAssertEquals("OK", status);
        // Get the DB entry from the subComponentList and check the value of the status field is OK
        Node baselineDBComponent = xmlHelpers.getNodeContainingSpecifiedChildNodeFromParent(baselineSubComponentList, "name", "DB1");
        status = xmlHelpers.getTextContentFromChildNode(baselineDBComponent, "status");
        AssertionUtils.multiAssertEquals("OK", status);
        // Get the health entry from the xml response and check the value is OK
        AssertionUtils.multiAssertEquals("OK", xmlHelpers.getTextContentFromChildNode(healthDetailResponseNode, "health"));
    }

}
