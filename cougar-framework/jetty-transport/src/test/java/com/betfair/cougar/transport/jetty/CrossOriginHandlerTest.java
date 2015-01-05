/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

import com.betfair.cougar.CougarVersion;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link com.betfair.cougar.transport.jetty.CrossOriginHandler}
 */
public class CrossOriginHandlerTest {


    @Test
    public void testHandlerSetsServerHeaderInTheResponse() throws Exception {
        final CrossOriginHandler victim = new CrossOriginHandler("betfair.com", "GET,POST,HEAD", "X-Requested-With,Content-Type,Accept,Origin", "1800", "true", "");
        final MockJettyRequest req = mock(MockJettyRequest.class);
        final MockJettyResponse res = mock(MockJettyResponse.class);

        victim.handle("/", req, req, res);

        verify(res, times(1)).setHeader(eq("Server"), eq("Cougar 2 - " + CougarVersion.getVersion()));
    }

    @Test
    public void testHandlerMarksRequestAsHandledByDefault() throws Exception {
        final CrossOriginHandler victim = new CrossOriginHandler("betfair.com", "GET,POST,HEAD", "X-Requested-With,Content-Type,Accept,Origin", "1800", "true", "");
        final MockJettyRequest req = mock(MockJettyRequest.class);
        final MockJettyResponse res = mock(MockJettyResponse.class);

        victim.handle("/", req, req, res);

        verify(req, times(1)).setHandled(eq(true));
        verify(req, times(1)).setHandled(eq(false));
    }

    @Test
    public void testHandlerUnmarksRequestAsHandledIfFilterContinuesTheChainExplicitDomain() throws Exception {
        testHandlesCrossOriginRequest("betfair.com", true);
    }

    @Test
    public void testHandlerUnmarksRequestAsHandledIfFilterContinuesTheChainAllDomains() throws Exception {
        testHandlesCrossOriginRequest("*", true);
    }

    @Test
    public void testHandlerUnmarksRequestAsHandledIfFilterContinuesTheChainNoDomains() throws Exception {
        testHandlesCrossOriginRequest("", false);
    }

    private void testHandlesCrossOriginRequest(String domains, boolean wantHandled) throws Exception {
        final CrossOriginHandler victim = new CrossOriginHandler(domains, "GET,POST,HEAD", "X-Requested-With,Content-Type,Accept,Origin", "1800", "true", "");
        final MockJettyRequest req = mock(MockJettyRequest.class);
        final MockJettyResponse res = mock(MockJettyResponse.class);

        when(req.getMethod()).thenReturn("OPTIONS");
        when(req.getHeader("Origin")).thenReturn("betfair.com");
        when(req.getHeader(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER)).thenReturn("PUT");
        when(req.getHeaders("Connection")).thenReturn(Collections.<String>emptyEnumeration());

        victim.handle("/", req, req, res);

        // this is always called
        verify(req, times(1)).setHandled(eq(true));
        if (wantHandled) {
            verify(req, never()).setHandled(eq(false));
        }
        else {
            verify(req, times(1)).setHandled(eq(false));
        }
    }

}
