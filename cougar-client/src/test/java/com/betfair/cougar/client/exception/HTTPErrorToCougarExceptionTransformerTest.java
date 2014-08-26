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

package com.betfair.cougar.client.exception;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.client.ExceptionFactory;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.logging.CougarLoggingUtils;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for HTTPErrorToCougarExceptionTransformer
 */
public class HTTPErrorToCougarExceptionTransformerTest {

    @BeforeClass
    public static void suppressLogs() {
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }

    @Test
    public void testTransform() throws UnsupportedEncodingException {
        FaultUnMarshaller faultUnMarshaller = mock(FaultUnMarshaller.class);
        ExceptionFactory exceptionFactory = mock(ExceptionFactory.class);
        CougarFault mockCougarFault = mock(CougarFault.class);

        FaultDetail df = mock(FaultDetail.class);
        when(mockCougarFault.getDetail()).thenReturn(df);
        when(df.getFaultMessages()).thenReturn(Collections.<String[]>emptyList());
        when(faultUnMarshaller.unMarshallFault(any(InputStream.class), anyString())).thenReturn(mockCougarFault);
        when(exceptionFactory.parseException(
                any(ResponseCode.class), anyString(), anyString(), any(List.class))).thenReturn(
                new CougarClientException(ServerFaultCode.FrameworkError, "bang"));

        HTTPErrorToCougarExceptionTransformer tx = new HTTPErrorToCougarExceptionTransformer(faultUnMarshaller);

        Exception ex = tx.convert(new ByteArrayInputStream("hello".getBytes("UTF-8")), exceptionFactory, HttpServletResponse.SC_NOT_FOUND);
        assertNotNull(ex);
    }

}
