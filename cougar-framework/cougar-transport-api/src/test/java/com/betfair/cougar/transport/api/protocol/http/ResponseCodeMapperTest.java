/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.transport.api.protocol.http;

import static org.junit.Assert.assertEquals;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.Fault;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

public class ResponseCodeMapperTest{

    @Test
    public void testStandardMapping() {
        CougarFault fault = new Fault(FaultCode.Server, "DSC-0015");
        ResponseCode responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_FORBIDDEN, fault);
        assertEquals(ResponseCode.Forbidden, responseCode);
    }

    @Test
    public void testBusinessExceptionMapping() {
        CougarFault fault = new Fault(FaultCode.Server, "DSC-0001");
        ResponseCode responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, fault);
        assertEquals(ResponseCode.InternalError, responseCode);

        fault = new Fault(FaultCode.Client, "Some Business Exception");
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, fault);
        assertEquals(ResponseCode.BusinessException, responseCode);
    }

    @Test
    public void testIdentityExceptionMappings() {
        CougarFault fault = new Fault(FaultCode.Client, "DSC-0033"); // UnidentifiedCaller
        ResponseCode responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_BAD_REQUEST, fault);
        assertEquals(ResponseCode.BadRequest, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0034"); // UnknownCaller
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_BAD_REQUEST, fault);
        assertEquals(ResponseCode.BadRequest, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0035"); // UnrecognisedCredentials
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_BAD_REQUEST, fault);
        assertEquals(ResponseCode.BadRequest, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0036"); // InvalidCredentials
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_BAD_REQUEST, fault);
        assertEquals(ResponseCode.BadRequest, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0037"); // SubscriptionRequired
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_FORBIDDEN, fault);
        assertEquals(ResponseCode.Forbidden, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0038"); // OperationForbidden
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_FORBIDDEN, fault);
        assertEquals(ResponseCode.Forbidden, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0039"); // NoLocationSupplied
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_BAD_REQUEST, fault);
        assertEquals(ResponseCode.BadRequest, responseCode);

        fault = new Fault(FaultCode.Client, "DSC-0040"); // BannedLocation
        responseCode = ResponseCodeMapper.getResponseCodeFromHttpCode(HttpServletResponse.SC_FORBIDDEN, fault);
        assertEquals(ResponseCode.Forbidden, responseCode);
    }
}
