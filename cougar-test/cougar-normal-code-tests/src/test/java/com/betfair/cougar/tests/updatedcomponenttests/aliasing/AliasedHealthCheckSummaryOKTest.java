/*
 * Copyright 2013, Simon Matić Langford
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

// Originally from UpdatedComponentTests/HealthCheck/Rest/Rest_HealthCheck_Summary_OK.xls;
package com.betfair.cougar.tests.updatedcomponenttests.aliasing;

import com.betfair.cougar.tests.updatedcomponenttests.healthcheck.rest.RestHealthCheckSummaryOKTest;
import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Checks that the healthcheck works when accessed on an aliased path.
 */
public class AliasedHealthCheckSummaryOKTest extends RestHealthCheckSummaryOKTest {

    public AliasedHealthCheckSummaryOKTest() {
        overridePath("/webping");
    }

}
