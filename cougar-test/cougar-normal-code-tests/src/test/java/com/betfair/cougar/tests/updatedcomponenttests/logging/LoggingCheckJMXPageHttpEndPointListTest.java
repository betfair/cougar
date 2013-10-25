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

// Originally from UpdatedComponentTests/Logging/Logging_CheckJMXPageHttpEndPointList.xls;
package com.betfair.cougar.tests.updatedcomponenttests.logging;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpPageBean;
import com.betfair.testing.utils.cougar.manager.HttpPageManager;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Connects to the JMX admin page. Generates the list of End Points and checks that the correct entries exist for testSimpleGet operation on multiple services
 */
public class LoggingCheckJMXPageHttpEndPointListTest {
    @Test
    public void doTest() throws Exception {
        // Set the parts of the URL to load (setting the protocol to the https)
        HttpPageBean httpPageBean1 = new HttpPageBean();
        httpPageBean1.setURLParts("https", "localhost", (int) 9999, "/");
        HttpPageBean Bean = httpPageBean1;
        // Set the auth details needed to load it
        Bean.setAuthusername("jmxadmin");
        
        Bean.setAuthpassword("password");
        // Attempt to load the page and check an OK status is returned
        HttpPageManager httpPageManager2 = new HttpPageManager();
        int status = httpPageManager2.getPage(Bean);
        HttpPageManager pageManager = httpPageManager2;
        AssertionUtils.multiAssertEquals((int) 200, status);
        // Check the page was loaded
        HttpPageBean response3 = Bean.getMe();
        AssertionUtils.multiAssertEquals(true, response3.getPageLoaded());
        // Click on the Cougar end points link
        boolean result = pageManager.clickOnLink(Bean, "CoUGAR", "EndPoints");
        AssertionUtils.multiAssertEquals(true, result);
        // Check the clicked link is loaded
        HttpPageBean response4 = Bean.getMe();
        AssertionUtils.multiAssertEquals(true, response4.getPageLoaded());
        // Set the bean to perform the list http end points action
        Bean.setLink("/InvokeAction//CoUGAR%3Aname%3DEndPoints/action=listEndPoints?action=listEndPoints");
        // Perform the action (get the http end points page)
        status = pageManager.getPage(Bean);
        AssertionUtils.multiAssertEquals((int) 200, status);
        // Check the page was loaded
        HttpPageBean response5 = Bean.getMe();
        AssertionUtils.multiAssertEquals(true, response5.getPageLoaded());
        // Parse the html page text to produce a list of the endpoints listed on the page
        List<String> endPointList = pageManager.parseJmxEndpointPage(Bean);
        // Check the various versions of testSimpleGet endpoints are listed on the page (using regex as can't hardcode the IP address that Cougar will be running on)
        result = pageManager.endPointExists(endPointList, "SOAP : Baseline/v1\\.0/testSimpleGet => http://.*/BaselineService/v1");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "SOAP : Baseline/v2\\.8/testSimpleGet => http://.*/BaselineService/v2");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "JSON_RPC : Baseline/v1\\.0/testSimpleGet => http://.*/json-rpc/");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "JSON_RPC : Baseline/v2\\.8/testSimpleGet => http://.*/json-rpc/");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "RESCRIPT : Baseline/v1\\.0/testSimpleGet => http://.*/cougarBaseline/v1/simple");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "RESCRIPT : Baseline/v1\\.0/testSimpleGet => http://.*/www/cougarBaseline/v1/simple");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "RESCRIPT : Baseline/v2\\.8/testSimpleGet => http://.*/cougarBaseline/v2/simple");
        AssertionUtils.multiAssertEquals(true, result);
        
        result = pageManager.endPointExists(endPointList, "RESCRIPT : Baseline/v2\\.8/testSimpleGet => http://.*/www/cougarBaseline/v2/simple");
        AssertionUtils.multiAssertEquals(true, result);
    }

}
