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

// Originally from UpdatedComponentTests/Headers/StaticHTMLPageServed_Complex_Check_Headers.xls;
package com.betfair.cougar.tests.updatedcomponenttests.headers;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.misc.DocumentHelpers;
import com.betfair.testing.utils.cougar.misc.HttpService;
import com.betfair.testing.utils.cougar.misc.HttptestPageBean;
import com.betfair.testing.utils.cougar.misc.InputStreamHelpers;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * Ensure cougar correctly sets the cache headers and monitors for static content requests for a complex HTML resource
 */
public class StaticHTMLPageServedComplexCheckHeadersTest {
    @Test
    public void doTest() throws Exception {
        // Create the HttpCallBean
        CougarManager cougarManager1 = CougarManager.getInstance();
        HttpCallBean httpCallBeanBaseline = cougarManager1.getNewHttpCallBean();
        CougarManager cougarManagerBaseline = cougarManager1;
        // Get the cougar logging attribute for getting log entries later
        // Point the created HttpCallBean at the correct service
        httpCallBeanBaseline.setServiceName("baseline", "cougarBaseline");
        
        httpCallBeanBaseline.setVersion("v2");
        // Get Expected HTML Response as Input Stream from the given file
        InputStream inputStream = InputStreamHelpers.getInputStreamForResource("static-html/testfile1.html");
        // Transfrom the input stream into a Document (XML) for assertion
        DocumentHelpers documentHelpers3 = new DocumentHelpers();
        Document expectedDocResponse = documentHelpers3.parseInputStreamToDocument(inputStream, false, false, true, "auto");
        // Load the Static Page shipped from Cougar
        HttptestPageBean loadedPage = HttpService.loadPage("http://localhost:8080/static-html/testfile1.html");
        // Get the loaded page as a document
        Document actualDocument = HttpService.getPageDom(loadedPage);
        AssertionUtils.multiAssertEquals(expectedDocResponse, actualDocument);
        // Check the WebResponse content type header
        AssertionUtils.multiAssertEquals("text/html", loadedPage.getWebResponseHeaderField("CONTENT-TYPE"));
        // Check the WebResponse cache control header
        AssertionUtils.multiAssertEquals("private, max-age=2592000", loadedPage.getWebResponseHeaderField("CACHE-CONTROL"));
    }

}
