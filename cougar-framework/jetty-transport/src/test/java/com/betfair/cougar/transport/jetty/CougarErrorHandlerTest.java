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

package com.betfair.cougar.transport.jetty;

import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import com.betfair.cougar.test.CougarTestCase;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CougarErrorHandlerTest  {

    @Test
    public void testErrorPage() throws Exception {
        CougarErrorHandler err = new CougarErrorHandler();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        Writer writer = new StringWriter();

        err.writeErrorPage(request, writer, 100, null, false);

        assertEquals("<html><head><title>foo</title></head><body>bar</body></html>", writer.toString());
    }

    @Test
    public void testErrorPageNotFound() throws Exception {
        CougarErrorHandler err = new CougarErrorHandler();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        Writer writer = new StringWriter();

        err.writeErrorPage(request, writer, 1, null, false);

        assertEquals("", writer.toString());
    }
}
