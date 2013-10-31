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

// Originally from UpdatedComponentTests/JMXServiceWebsite/Secure_JmxServiceTest_RunRawQuery.xls;
package com.betfair.cougar.tests.updatedcomponenttests.jmxservicewebsite;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpPageBean;
import com.betfair.testing.utils.cougar.manager.HttpPageManager;
import org.testng.annotations.Test;

/**
 * Runs a raw query to check if the HtmlAdaptor is active. Check that the query result is correctly displayed
 */
public class SecureJmxServiceTestRunRawQueryTest {
    @Test
    public void doTest() throws Exception {
        // Set the parts of the URL to load (setting the link to be the query to run)
        HttpPageBean httpPageBean1 = new HttpPageBean();
        httpPageBean1.setURLParts("https", "localhost", (int) 9999, "/administration/batchquery.jsp?on=SYSTEM&an=java.specification.name");
        HttpPageBean BeanyBaby = httpPageBean1;
        // Set the auth details needed to load it
        BeanyBaby.setAuthusername("jmxadmin");
        
        BeanyBaby.setAuthpassword("password");
        // Attempt to load the page and check an OK status is returned
        HttpPageManager httpPageManager2 = new HttpPageManager();
        int status = httpPageManager2.getPage(BeanyBaby);
        AssertionUtils.multiAssertEquals((int) 200, status);
        // Check the page was loaded and that the query result is displayed correctly
        HttpPageBean response3 = BeanyBaby.getMe();
        AssertionUtils.multiAssertEquals(true, response3.getPageLoaded());
        AssertionUtils.multiAssertEquals("System Properties~java.specification.name~Java Platform API Specification", response3.getPageText());
    }

}
