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

// Originally from UpdatedComponentTests/Geolocate/SOAP/SOAP_Geolocate_ByIPAddress_InvalidIP.xls;
package com.betfair.cougar.tests.updatedcomponenttests.geolocate.soap;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.AccessLogRequirement;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Ensure that when a SOAP request is received by Cougar, passing an invalid ipaddress (in this case, multiple Comma Seperated Addresses), the local IP address is used as default
 */
public class SOAPGeolocateByIPAddressInvalidIPTest {
    String localIPAddress = "127.0.0.1";
    String invalidIPAddress = "NOT AN IP Address";
    String validUKIPAddress = "87.248.113.14";

    @Test
    public void doTest_Single_IP() throws Exception {
        doTest(invalidIPAddress, localIPAddress);
    }

    @Test
    public void doTest_FirstItem_IPList() throws Exception {
        String testIPAddress = invalidIPAddress + "," + validUKIPAddress + ",1.2.3.4,5.6.7.8";
        String expectedIPAddress = validUKIPAddress + ";1.2.3.4;5.6.7.8";

        doTest(testIPAddress, expectedIPAddress);
    }

    @Test
    public void doTest_LastItem_IPList() throws Exception {
        String testIPAddress = validUKIPAddress + ",1.2.3.4,5.6.7.8," + invalidIPAddress;
        String expectedIPAddress = "87.248.113.14;1.2.3.4;5.6.7.8";

        doTest(testIPAddress, expectedIPAddress);
    }

    @Test
    public void doTest_AllItemInvalid_IPList() throws Exception {
        String testIPAddress =  "Another Random One" +  "," + invalidIPAddress + "," + "And Another Random Bit" ;

        doTest(testIPAddress, localIPAddress);
    }

    @Test
    public void doTest_SingleValidItem_IPList() throws Exception {
        String testIPAddress =  "Another Random One" +  "," + invalidIPAddress + "," + "And Another Random Bit" + "," + validUKIPAddress + "," + "And More Random Bit" ;

        doTest(testIPAddress, validUKIPAddress);
    }

    private void doTest(String testIPAddress, String expectedIPAddress) throws Exception{
        String expectedIPLocation = "";
        if(expectedIPAddress == localIPAddress)
        {
           expectedIPLocation = "--";
        }
        if(expectedIPAddress.contains(validUKIPAddress))
        {
            expectedIPLocation = "GB";
        }
        // Create the request as an XML document
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<TestSimpleGetRequest><message>foo</message></TestSimpleGetRequest>");
        // Set up the Http Call Bean to make the request
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean(testIPAddress);

        getNewHttpCallBean2.setServiceName("Baseline");
        
        getNewHttpCallBean2.setVersion("v2");
        
        getNewHttpCallBean2.setPostObjectForRequestType(createAsDocument1, "SOAP");
        // Get current time for getting log entries later

        Timestamp getTimeAsTimeStamp8 = new Timestamp(System.currentTimeMillis());
        // Make the SOAP call to the operation
        cougarManager2.makeSoapCougarHTTPCalls(getNewHttpCallBean2);
        // Create the expected response as an XML document
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument10 = xMLHelpers4.getXMLObjectFromString("<response><message>foo</message></response>");
        // Convert the expected response to a SOAP document for comparison with actual response
        Map<String, Object> convertResponseToSOAP11 = cougarManager2.convertResponseToSOAP(createAsDocument10, getNewHttpCallBean2);
        // Check the response is as expected
        HttpResponseBean response5 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(convertResponseToSOAP11.get("SOAP"), response5.getResponseObject());
        
        // Check the log entries are as expected
        cougarManager2.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp8, new RequestLogRequirement("2.8", "testSimpleGet"));
        // In particular check the IPLocation field of the Access log to check that it defaulted to use the local host address
        cougarManager2.verifyAccessLogEntriesAfterDate(getTimeAsTimeStamp8, new AccessLogRequirement(expectedIPAddress, expectedIPLocation, "/BaselineService/v2", "Ok"));
    }

}
