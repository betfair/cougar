/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.core.api.exception;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static junit.framework.Assert.assertTrue;

public class CougarClientExceptionTest {

    @Test
    public void stackTraceContainsServerWarning() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new CougarClientException(ServerFaultCode.RemoteCougarCommunicationFailure, "Some error", false).printStackTrace(pw);

        assertTrue(sw.toString(), sw.toString().contains("Server not confirmed to be a Cougar"));
    }

}
