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

package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.impl.protocol.http.DefaultGeoLocationDeserializer;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;


public class StaticContentServiceHandlerTest {

	private static final String CONTENT_TYPE = "text/xml";

	private StaticContentServiceHandler handler;
	private HttpServletResponse httpServletResponse;
	private org.eclipse.jetty.server.Request request;
    private RequestLogger requestLogger;

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ServletOutputStream sos = new ServletOutputStream() {
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

    private GeoIPLocator locator = new GeoIPLocator() {
        @Override
        public GeoLocationDetails getGeoLocation(String remoteIP, List<String> resolvedIPs, String inferredCountry) {
            return new GeoLocationDetails() {
                @Override
                public String getRemoteAddr() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public List<String> getResolvedAddresses() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getCountry() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public boolean isLowConfidenceGeoLocation() {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getLocation() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getInferredCountry() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            };
        }
    };

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }


	@Before
	public void setUp() throws Exception {
        requestLogger = mock(RequestLogger.class);
		handler = new StaticContentServiceHandler( "/wsdl", "/wsdl/[fo]+\\.wsdl", CONTENT_TYPE, "X-UUID", "X-UUID-Parents", new DefaultGeoLocationDeserializer(), locator, requestLogger, true);
        handler.setServer(mock(Server.class));
        handler.start();

		// Set up the Continuations mock.
		request = new MockJettyRequest();

		httpServletResponse = mock(HttpServletResponse.class);
	}

    private void logCheck() {
        verify(requestLogger, times(1)).logAccess(any(HttpCommand.class),
                any(ExecutionContext.class),
                anyLong(),
                anyLong(),
                any(MediaType.class),
                any(MediaType.class),
                any(ResponseCode.class));
    }

	@Test
	public void testNonWSDLRequest() throws IOException, ServletException {
        when(httpServletResponse.getOutputStream()).thenReturn(sos);

		handler.doHandle("/foo.wsdl", request, request, httpServletResponse);
		assertTrue(request.isHandled());

        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        logCheck();
	}

	@Test
	public void testWSDLRequestNoWSDL() throws IOException, ServletException {
        when(httpServletResponse.getOutputStream()).thenReturn(sos);

		handler.handle("/wsdl/foooooooo.wsdl", request, request, httpServletResponse);
		assertTrue(request.isHandled());

        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        logCheck();
	}

	@Test
	public void testWSDLRequestNonMatchingRegex() throws IOException, ServletException {
        when(httpServletResponse.getOutputStream()).thenReturn(sos);

		handler.handle("/wsdl/not-there.wsdl", request, request, httpServletResponse);
		assertTrue(request.isHandled());

        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        logCheck();
	}

	@Test
	public void testWSDLRequestOK() throws IOException, ServletException {
        when(httpServletResponse.getOutputStream()).thenReturn(sos);

		handler.handle("/wsdl/foo.wsdl", request, request, httpServletResponse);
		assertTrue(request.isHandled());
		String wsdl = baos.toString();
		assertEquals("I AM A WSDL", wsdl);

        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse).setContentType(CONTENT_TYPE);
        verify(httpServletResponse).flushBuffer();
        verify(httpServletResponse).addHeader("Cache-Control", "private, max-age=2592000");
        logCheck();
	}

}
