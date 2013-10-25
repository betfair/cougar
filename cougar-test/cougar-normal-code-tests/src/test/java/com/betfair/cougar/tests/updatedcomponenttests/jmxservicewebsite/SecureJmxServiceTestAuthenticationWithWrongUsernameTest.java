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

// Originally from UpdatedComponentTests/JMXServiceWebsite/Secure_JmxServiceTest_AuthenticationWithWrongUsername.xls;
package com.betfair.cougar.tests.updatedcomponenttests.jmxservicewebsite;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpPageBean;
import com.betfair.testing.utils.cougar.manager.HttpPageManager;
import org.testng.annotations.Test;

/**
 * Tries to connect to the JMX admin page with the wrong login name and checks the page wasn't loaded.  
 */
public class SecureJmxServiceTestAuthenticationWithWrongUsernameTest {
    @Test
    public void doTest() throws Exception {
        // Set the parts of the URL to load
        HttpPageBean httpPageBean1 = new HttpPageBean();
        httpPageBean1.setURLParts("https", "localhost", (int) 9999, "/");
        HttpPageBean BeanyBaby = httpPageBean1;
        // Set the auth details needed to load it (but with the wrong username)
        BeanyBaby.setAuthusername("wrongusername");
        
        BeanyBaby.setAuthpassword("password");
        // Attempt to load the page and check an authorisation error is returned
        HttpPageManager httpPageManager2 = new HttpPageManager();
        int status = httpPageManager2.getPage(BeanyBaby);
        AssertionUtils.multiAssertEquals((int) 401, status);
        // Check the page wasn't loaded as the username was incorrect
        HttpPageBean response3 = BeanyBaby.getMe();
        AssertionUtils.multiAssertEquals(false, response3.getPageLoaded());
    }

}
