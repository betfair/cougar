/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for @See ServletResponseFileStreamerTest
 */
public class ServletResponseFileStreamerTest {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ServletOutputStream capturingOutputStream = new ServletOutputStream() {
        @Override
        public boolean isReady() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void write(int b) throws IOException {
            baos.write(b);
        }
    };


    private InputStream pageInputStream;
    private HttpServletResponse response;

    @Before
    public void init() throws IOException {
        pageInputStream = getClass().getResourceAsStream("/errorpages/file.html");
        response = Mockito.mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(capturingOutputStream);
        baos.reset();
    }


    @Test
    public void testBytesAreStreamed() throws IOException {
        int bytesWritten = ServletResponseFileStreamer.getInstance().streamFileToResponse(pageInputStream, response, HttpServletResponse.SC_OK, null, null);
        ArgumentCaptor<Integer> statusCapture = ArgumentCaptor.forClass(Integer.class);
        verify(response).setStatus(statusCapture.capture());
        assertTrue(bytesWritten >= 3);
        assertEquals("Bytes written should equal bytes written to servlet", baos.size(), bytesWritten);
        assertEquals("Incorrect response code set", 200, (int)statusCapture.getValue());
    }

    @Test
    public void testHeaderParamsAreSet() throws IOException{
        ServletResponseFileStreamer.getInstance().streamFileToResponse(pageInputStream, response, HttpServletResponse.SC_OK, null, new String[][] {{ "foo", "bar"}});
        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerParamCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(headerNameCaptor.capture(), headerParamCaptor.capture());
        assertEquals("incorrect header value set", "foo", headerNameCaptor.getValue());
        assertEquals("incorrect header value set", "bar", headerParamCaptor.getValue());
    }

    @Test
    public void testContentTypeIsSet() throws IOException {
        int bytesWritten = ServletResponseFileStreamer.getInstance().streamFileToResponse(pageInputStream, response, HttpServletResponse.SC_OK, "text/html", null);
        ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setContentType(contentTypeCaptor.capture());
        assertTrue(bytesWritten >= 3);
        assertEquals(contentTypeCaptor.getValue(), "text/html");
    }

    @Test
    public void testErrorPageStreamer() throws IOException {
        int bytesWritten = ServletResponseFileStreamer.getInstance().stream404ToResponse(response);
        ArgumentCaptor<Integer> statusCapture = ArgumentCaptor.forClass(Integer.class);

        verify(response).setStatus(statusCapture.capture());
        assertTrue(bytesWritten >= 10);
        assertEquals("Incorrect response code set", 404, (int)statusCapture.getValue());

    }

}

