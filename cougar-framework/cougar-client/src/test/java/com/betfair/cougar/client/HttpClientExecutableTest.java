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

package com.betfair.cougar.client;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.core.api.ev.ClientExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.client.ExceptionFactory;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.betfair.cougar.client.AbstractHttpExecutable.DEFAULT_REQUEST_UUID_PARENTS_HEADER;
import static com.betfair.cougar.client.HttpClientExecutable.DEFAULT_REQUEST_UUID_HEADER;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
/**
 * Unit test for @see HttpClientExecutable class
 */
public class HttpClientExecutableTest extends AbstractHttpExecutableTest<HttpUriRequest> {

    // --------------- Initialisation stuff ----------------
	HttpClient mockHttpClient;
    CougarClientConnManager mockManager;
    private ContextEmitter emitter;

    @Override
    protected AbstractHttpExecutable<HttpUriRequest> makeExecutable(HttpServiceBindingDescriptor tsbd) throws Exception {
        mockHttpClient = mock(HttpClient.class);
        HttpResponse response = mock(HttpResponse.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP",1,1), 200, "OK"));
        emitter = mock(ContextEmitter.class);

        mockManager = mock(CougarClientConnManager.class);

        HttpClientExecutable ret = new HttpClientExecutable(tsbd, emitter, tracer, mockManager);
        ret.setClient(mockHttpClient);
        return ret;
    }

    protected void preInit() throws Exception {
		// Add dependent mocks to cougar client execution venue

        HttpUriRequest request = mock(HttpUriRequest.class);
        when(mockMethodFactory.create(any(String.class), any(String.class), any(Message.class), any(Marshaller.class), any(String.class), any(ClientCallContext.class), any(TimeConstraints.class))).thenReturn(request);

		HttpParams mockParams = mock(HttpParams.class);
		mockGetMethod = mock(HttpGet.class);
		when(mockGetMethod.getParams()).thenReturn(mockParams);

	}

    // --------------- Base class requirements ---------------
    private void mockHttpResponse(final HttpUriRequest request, final String response, final int responseCode) {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final HttpEntity entity = mock(HttpEntity.class);
        final StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1,responseCode,"");
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        final Header cougarHeader=mock(Header.class);
        when(cougarHeader.getValue()).thenReturn("Cougar 2");
        when(httpResponse.containsHeader("Server")).thenReturn(true);
        when(httpResponse.getFirstHeader("Server")).thenReturn(cougarHeader);

        try {
            final byte[] responseBytes = response.getBytes("UTF-8");
            when(entity.getContent()).thenReturn(new ByteArrayInputStream(responseBytes));
            when(entity.getContentLength()).thenReturn((long) responseBytes.length);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try {
			when(mockHttpClient.execute(request)).thenReturn(httpResponse);
		} catch (final Exception e1) {
			fail();
		}
    }

    @Override
    protected void mockAndMakeCall(HttpUriRequest httpUriRequest, int httpCode, String response, int responseSize, AbstractHttpExecutable<HttpUriRequest> client, ExecutionContext ec, OperationKey key, Object[] params, ObservableObserver observer, ExecutionVenue ev, TimeConstraints timeConstraints) throws InterruptedException {
        // mocks before
        mockHttpResponse(httpUriRequest, response, httpCode);
        // executes second
        client.execute(ec, key, params, observer, ev, timeConstraints);
    }

    @Override
    protected int getEVPostSuccessWithMandatoryBodyAndQueryParameterPresent_ResultSize() {
        return 23;
    }

    // --------------- Specific tests ---------------
	@Test
	public void testEVPostSuccessWithMandatoryBodyParameterPresent() throws Exception {
        HttpParams mockParams = mock(HttpParams.class);
        mockPostMethod = mock(HttpPost.class);
        when(mockPostMethod.getParams()).thenReturn(mockParams);

        super.testEVPostSuccessWithMandatoryBodyParameterPresent();
	}

    @Test
	public void testEVPostSuccessWithMandatoryBodyAndQueryParameterPresent() throws Exception {
		HttpParams mockParams = mock(HttpParams.class);
		mockPostMethod = mock(HttpPost.class);
		when(mockPostMethod.getParams()).thenReturn(mockParams);

		super.testEVPostSuccessWithMandatoryBodyAndQueryParameterPresent();
	}

    @Test
    public void testFailingCall() throws IOException {
        generateEV(tsd, null);
        HttpUriRequest mockMethod = mock(HttpUriRequest.class);
        final BasicHttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ""));
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(mockMethodFactory.create(anyString(), anyString(), any(Message.class), any(Marshaller.class), anyString(), any(ClientCallContext.class), any(TimeConstraints.class))).thenReturn(mockMethod);
        when(mockedHttpErrorTransformer.convert(any(InputStream.class), any(ExceptionFactory.class), anyInt())).thenReturn(new CougarClientException(ServerFaultCode.RemoteCougarCommunicationFailure, "bang"));

        HttpParams mockParams = mock(HttpParams.class);
        when(mockMethod.getParams()).thenReturn(mockParams);


        ExecutionObserver mockedObserver = mock(ExecutionObserver.class);
        client.execute(createEC(null, null, false), TestServiceDefinition.TEST_MIXED, new Object[] {TEST_TEXT, TEST_TEXT }, mockedObserver, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> resultCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(mockedObserver).onResult(resultCaptor.capture());
        ExecutionResult actual = resultCaptor.getValue();
        assertEquals(ExecutionResult.ResultType.Fault, actual.getResultType());
        assertNotNull(actual.getFault());
        assertNull(actual.getResult());
        assertNull(actual.getSubscription());
        assertEquals(0, ((ClientExecutionResult)actual).getResultSize());
    }

    @Test
    public void testNullResponseFromServer() throws IOException {
        generateEV(tsd, null);
        HttpUriRequest mockMethod = mock(HttpUriRequest.class);

        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getEntity()).thenReturn(null);

        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(mockMethodFactory.create(anyString(), anyString(), any(Message.class), any(Marshaller.class), anyString(), any(ClientCallContext.class), any(TimeConstraints.class))).thenReturn(mockMethod);
        when(mockedHttpErrorTransformer.convert(any(InputStream.class),  any(ExceptionFactory.class), anyInt())).thenReturn(new CougarClientException(ServerFaultCode.RemoteCougarCommunicationFailure, "bang"));

        HttpParams mockParams = mock(HttpParams.class);
        when(mockMethod.getParams()).thenReturn(mockParams);

        final StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1,HttpServletResponse.SC_OK,"");

        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        ExecutionObserver mockedObserver = mock(ExecutionObserver.class);
        client.execute(createEC(null, null, false), TestServiceDefinition.TEST_MIXED, new Object[] {TEST_TEXT, TEST_TEXT }, mockedObserver, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> resultCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(mockedObserver).onResult(resultCaptor.capture());
        ExecutionResult actual = resultCaptor.getValue();
        assertEquals(actual.getFault().getServerFaultCode(), ServerFaultCode.RemoteCougarCommunicationFailure);
        assertEquals(actual.getFault().getResponseCode(), ResponseCode.ServiceUnavailable);
        assertEquals(0, ((ClientExecutionResult)actual).getResultSize());
    }

    @Test
    public void shouldReturnTransportMetrics() throws Exception {

        when(mockManager.getConnectionsInPool()).thenReturn(4);
        when(mockManager.getFreeConnections()).thenReturn(2);
        when(mockManager.getDefaultMaxPerRoute()).thenReturn(10);

        assertEquals(4, client.getTransportMetrics().getOpenConnections());
        assertEquals(2, client.getTransportMetrics().getFreeConnections());
        assertEquals(10, client.getTransportMetrics().getMaximumConnections());
        assertEquals(20, client.getTransportMetrics().getCurrentLoad());
    }


}
