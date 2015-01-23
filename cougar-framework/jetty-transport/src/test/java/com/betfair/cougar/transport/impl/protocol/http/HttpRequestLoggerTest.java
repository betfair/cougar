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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.RequestTimer;
import com.betfair.cougar.logging.EventLogDefinition;
import com.betfair.cougar.logging.EventLoggingRegistry;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.HeaderUtils;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HttpRequestLoggerTest {

	protected HttpServletRequest request;
	protected HttpServletResponse response;

    private Logger eventLog;
    private ILoggerFactory loggerFactory;

	private EventLoggingRegistry registry;
    private ILoggerFactory oldLoggerFactory;

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

	@Before
	public void init() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		registry = new EventLoggingRegistry();
		EventLogDefinition eld = new EventLogDefinition();
		eld.setRegistry(registry);
		eld.setLogName("ACCESS-LOG");
		eld.register();

        loggerFactory = mock(ILoggerFactory.class);
        oldLoggerFactory = HttpRequestLogger.setLoggerFactory(loggerFactory);
        eventLog = mock(Logger.class);
	}

    @After
    public void after()
    {
        HttpRequestLogger.setLoggerFactory(oldLoggerFactory);
    }

	@Test
	public void testLogEventNoExtraFields() throws Exception {
        final HttpCommand command = createCommand(null);
        RequestLogger logger = new HttpRequestLogger(registry, true);
		ExecutionContext context = mock(ExecutionContext.class);
		RequestUUIDImpl uuid = new RequestUUIDImpl();
		when(context.getRequestUUID()).thenReturn(uuid);
		when(response.getContentType()).thenReturn(MediaType.APPLICATION_XML);
		GeoLocationDetails geoDetails = mock(GeoLocationDetails.class);
		when(geoDetails.getCountry()).thenReturn("UK");
        when(geoDetails.getRemoteAddr()).thenReturn("2.3.4.5");
		when(geoDetails.getResolvedAddresses()).thenReturn(RemoteAddressUtils.parse("1.2.3.4", "1.2.3.4,5.6.7.8"));
		when(context.getLocation()).thenReturn(geoDetails);
		when(request.getHeader(HeaderUtils.USER_AGENT)).thenReturn("IE");
		long bytesRead = 100;
		long bytesWritten = 1000;
		MediaType requestMediaType = MediaType.APPLICATION_JSON_TYPE;
		MediaType responseMediaType = MediaType.APPLICATION_XML_TYPE;
		ResponseCode responseCode = ResponseCode.Ok;

        command.getTimer().requestComplete();

        when(loggerFactory.getLogger("ACCESS-LOG")).thenReturn(eventLog);

		logger.logAccess(command, context, bytesRead, bytesWritten, requestMediaType, responseMediaType, responseCode);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventLog, times(1)).info(messageCaptor.capture());

        String[] loggedValues = tokenise(messageCaptor.getValue());
        int i=0;
		assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(command.getTimer().getReceivedTime()), loggedValues[i++]);
		assertEquals(uuid.toString(), loggedValues[i++]);
		assertEquals("/test", loggedValues[i++]);
		assertEquals("none", loggedValues[i++]);
        assertEquals("2.3.4.5", loggedValues[i++]);
		assertEquals("1.2.3.4;5.6.7.8", loggedValues[i++]);
		assertEquals("UK", loggedValues[i++]);
		assertEquals("Ok", loggedValues[i++]);
		assertEquals(command.getTimer().getProcessTimeNanos(), Long.parseLong(loggedValues[i++]));
		assertEquals("100", loggedValues[i++]);
		assertEquals("1000", loggedValues[i++]);
		assertEquals("json", loggedValues[i++]);
		assertEquals("xml", loggedValues[i++]);
        assertEquals("[]", loggedValues[i++]);
	}

    @Test
    public void testLogEventWithExtraFields() throws Exception {
        final HttpCommand command = createCommand(null);
        HttpRequestLogger logger = new HttpRequestLogger(registry, true);
        logger.setHeadersToLog("User-Agent");
        logger.addHeaderToLog("eeeep");
        ExecutionContext context = mock(ExecutionContext.class);
        RequestUUIDImpl uuid = new RequestUUIDImpl();
        when(context.getRequestUUID()).thenReturn(uuid);
        when(response.getContentType()).thenReturn(MediaType.APPLICATION_XML);
        GeoLocationDetails geoDetails = mock(GeoLocationDetails.class);
        when(geoDetails.getCountry()).thenReturn("UK");
        when(geoDetails.getRemoteAddr()).thenReturn("2.3.4.5");
        when(geoDetails.getResolvedAddresses()).thenReturn(RemoteAddressUtils.parse("1.2.3.4", "1.2.3.4"));
        when(context.getLocation()).thenReturn(geoDetails);
        when(request.getHeader(HeaderUtils.USER_AGENT)).thenReturn("IE");
        when(request.getHeader("eeeep")).thenReturn("meeeep");
        long bytesRead = 100;
        long bytesWritten = 1000;
        MediaType requestMediaType = MediaType.APPLICATION_JSON_TYPE;
        MediaType responseMediaType = MediaType.APPLICATION_XML_TYPE;
        ResponseCode responseCode = ResponseCode.Ok;


        when(loggerFactory.getLogger("ACCESS-LOG")).thenReturn(eventLog);


        command.getTimer().requestComplete();
        logger.logAccess(command, context, bytesRead, bytesWritten, requestMediaType, responseMediaType, responseCode);


        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventLog, times(1)).info(messageCaptor.capture());

        String[] loggedValues = tokenise(messageCaptor.getValue());
        int i=0;
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(command.getTimer().getReceivedTime()), loggedValues[i++]);
        assertEquals(uuid.toString(), loggedValues[i++]);
        assertEquals("/test", loggedValues[i++]);
        assertEquals("none", loggedValues[i++]);
        assertEquals("2.3.4.5", loggedValues[i++]);
        assertEquals("1.2.3.4", loggedValues[i++]);
        assertEquals("UK", loggedValues[i++]);
        assertEquals("Ok", loggedValues[i++]);
        assertEquals(command.getTimer().getProcessTimeNanos(), Long.parseLong(loggedValues[i++]));
        assertEquals("100", loggedValues[i++]);
        assertEquals("1000", loggedValues[i++]);
        assertEquals("json", loggedValues[i++]);
        assertEquals("xml", loggedValues[i++]);
        assertEquals("[User-Agent=IE|eeeep=meeeep]", loggedValues[i++]);


    }

    private String[] tokenise(String message) {
        // Ruddy java regexes don't support infinite length look behinds
        return message.trim().split("(?<!\\[[^\\]]{1,500}),");
    }

    protected HttpCommand createCommand(IdentityTokenResolver identityTokenResolver) {
        return new TestHttpCommand(identityTokenResolver);
    }

    protected class TestHttpCommand implements HttpCommand {

        private CommandStatus commandStatus = CommandStatus.InProgress;
        private RequestTimer timer = new RequestTimer();
        private IdentityTokenResolver identityTokenResolver;

        public TestHttpCommand(IdentityTokenResolver identityTokenResolver) {
            this.identityTokenResolver = identityTokenResolver;
        }

        @Override
        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public HttpServletResponse getResponse() {
            return response;
        }

        @Override
        public IdentityTokenResolver<?, ?, ?> getIdentityTokenResolver() {
            return this.identityTokenResolver;
        }

        @Override
        public CommandStatus getStatus() {
            return commandStatus;
        }

        @Override
        public void onComplete() {
            commandStatus = CommandStatus.Complete;
        }

        @Override
        public RequestTimer getTimer() {
            return timer;
        }

        @Override
        public String getFullPath() {
            return "/test";
        }


        @Override
        public String getOperationPath() {
            return "/foo"+ getFullPath();
        }
    };

}
