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

package com.betfair.cougar.tests.updatedcomponenttests.property;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class VerifyOverrideCougarPropertyTest {
    @Test
    public void
    doTest() throws Exception {
        verifyPropertyValue("some.random.property.1", "overrides");
        verifyPropertyValue("some.random.property.2", "application");
        verifyPropertyValue("some.random.property.3", "default");
    }

    public void verifyPropertyValue(String propertyname, String propertyvalue) throws Exception {
        CougarManager cougarManager = CougarManager.getInstance();

        HttpCallBean getNewHttpCallBean = cougarManager.getNewHttpCallBean("87.248.113.14");
        getNewHttpCallBean.setOperationName("echoCougarPropertyValue", "propertyEcho");
        getNewHttpCallBean.setServiceName("baseline", "cougarBaseline");
        getNewHttpCallBean.setVersion("v2");
        Map map = new HashMap();
        map.put("propertyName", propertyname);

        getNewHttpCallBean.setQueryParams(map);
// Make the 4 REST calls to the operation
        cougarManager.makeRestCougarHTTPCalls(getNewHttpCallBean);
// Create the expected response as an XML document
        XMLHelpers xMLHelpers = new XMLHelpers();
        Document createAsDocument = xMLHelpers.createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<String>" + propertyvalue + "</String>").getBytes())));
        Map<CougarMessageProtocolRequestTypeEnum, Object> convertResponseToRestTypes = cougarManager.convertResponseToRestTypes(createAsDocument, getNewHttpCallBean);
// Check the 4 responses are as expected
        HttpResponseBean response = getNewHttpCallBean.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTXMLXML);
        AssertionUtils.multiAssertEquals(convertResponseToRestTypes.get(CougarMessageProtocolRequestTypeEnum.RESTXML), response.getResponseObject());
    }
}
