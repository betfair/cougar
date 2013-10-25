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

package com.betfair.cougar.tests.updatedcomponenttests.logging;

import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.ServiceLogRequirement;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;

import static com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.RESTJSONJSON;


/**
 * Ensure that jetty does not produce warnings in suppressed classes.
 * These classes marked in jetty-transport-defaults.properties
 */
public class LoggingCheckJettySuppressTest {

    @Test
    public void SuppressInvalidCookieHeader() throws Exception {
        CougarManager cougarManager = CougarManager.getInstance();
        HttpCallBean callBean = cougarManager.getNewHttpCallBean();

        Timestamp startTime = new Timestamp(System.currentTimeMillis());

        callBean.setServiceName("baseline", "cougarBaseline");
        callBean.setOperationName("stringSimpleTypeEcho", "stringEcho");
        callBean.setVersion("v2");
        callBean.setQueryParams(Collections.singletonMap("msg", "foo"));
        callBean.setAlternativeURL("/cookie");
        callBean.setHeaderParams(Collections.singletonMap("Cookie", "Invalid {[Cookie]-Name}=SomeValue"));

        cougarManager.makeRestCougarHTTPCall(callBean, CougarMessageProtocolRequestTypeEnum.RESTJSON,
                CougarMessageContentTypeEnum.JSON);

        Map<CougarMessageProtocolResponseTypeEnum, HttpResponseBean> responses = callBean.getResponses();
        AssertionUtils.multiAssertEquals("\"foo\"", responses.get(RESTJSONJSON).getResponseObject());

        expectNoWarningsAfter(cougarManager, startTime);
    }

    @Test
    public void SuppressEofWarning() throws Exception {
        CougarManager cougarManager = CougarManager.getInstance();
        HttpCallBean callBean = cougarManager.getNewHttpCallBean();

        Timestamp startTime = new Timestamp(System.currentTimeMillis());

        Socket socket = new Socket(callBean.getHost(), Integer.parseInt(callBean.getPort()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println("GET /cougarBaseline/v2.8/simple/sleep?sleep=500 HTTP/1.1");
        out.println("Host: " + callBean.getHost());
        out.println("Accept-Encoding: gzip");
        out.println();

        socket.setSoLinger(true, 0); // RST
        socket.close();

        expectNoWarningsAfter(cougarManager, startTime);
    }

    private void expectNoWarningsAfter(CougarManager cougarManager, Timestamp startTime) throws IOException, InterruptedException {
        cougarManager.verifyNoServiceLogEntriesAfterDate(startTime, 2000, new ServiceLogRequirement("WARN", true));
    }

}
