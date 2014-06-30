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

// Originally from UpdatedComponentTests/StandardTesting/REST/Rest_Get_RequestTypes_Parameters_ListOfStrings_1BlankEntry.xls;
package com.betfair.cougar.tests.updatedcomponenttests.standardtesting.rest;

import com.betfair.testing.utils.cougar.CougarBaseline2_8TestingInvoker;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensure that when the 4 supported Rest XML/JSON Gets are performed, Cougar can handle an empty List of Strings being passed in the Header and Query parameters
 */
public class RestGetRequestTypesParametersListOfStrings1BlankEntryTest {

    @Test
    public void doTest() {
        String expectedXmlResponse = "<StringListOperationResponse><NonMandatoryParamsOperationResponseObject><headerParameter/><queryParameter/></NonMandatoryParamsOperationResponseObject></StringListOperationResponse>";
        String expectedJsonResponse = "{queryParameter:\"\",headerParameter:\"\"}";
        CougarBaseline2_8TestingInvoker.create()
                .setOperation("stringListOperation")
                .addHeaderParam("HeaderParam","")
                .addQueryParam("queryParam","")
                .setExpectedResponse(CougarMessageContentTypeEnum.XML, expectedXmlResponse)
                .setExpectedResponse(CougarMessageContentTypeEnum.JSON, expectedJsonResponse)
                .setExpectedHttpResponse(200, "OK")
                .makeMatrixCalls(CougarMessageContentTypeEnum.JSON, CougarMessageContentTypeEnum.XML)
                .verify();

    }

}
