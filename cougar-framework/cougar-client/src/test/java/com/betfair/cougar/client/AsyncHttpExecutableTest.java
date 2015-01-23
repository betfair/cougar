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
import com.betfair.cougar.core.api.ev.ClientExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.client.ExceptionFactory;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import org.eclipse.jetty.client.*;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.After;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Date: 28/01/2013
 * Time: 15:50
 */
public class AsyncHttpExecutableTest extends AbstractHttpExecutableTest<Request> {

    // --------------- Initialisation stuff ----------------
    private CapturingRequest mockRequest;
    private HttpClient mockClient;
    private HttpContextEmitter contextEmitter;

    @Override
    protected AbstractHttpExecutable<Request> makeExecutable(HttpServiceBindingDescriptor tsbd)
            throws Exception {

        mockRequest = new CapturingRequest();

        mockPostMethod = mockRequest;
        mockGetMethod = mockRequest;

        contextEmitter = new HttpContextEmitter(new DefaultGeoLocationSerializer(),"X-REQUEST-UUID","X-REQUEST-UUID-PARENTS");

        AsyncHttpExecutable executable = new AsyncHttpExecutable(tsbd,contextEmitter, tracer, Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        mockClient = mock(HttpClient.class);
        when(mockClient.newRequest(anyString())).thenReturn(mockRequest);
        executable.setClient(mockClient);

        return executable;
    }

    @After
    public void tearDown() throws Exception {
        ((AsyncHttpExecutable) client).shutdown();
    }

    // --------------- Base class requirements ---------------
    private void fireResponse(CapturingRequest request, int errorCode, String responseText, int resultSize, ObservableObserver observer, boolean successfulResponse) throws InterruptedException {
        Response.CompleteListener listener = request.awaitSend(1000, TimeUnit.MILLISECONDS);
        assertNotNull(listener);
        InputStreamResponseListener responseListener = (InputStreamResponseListener) listener;

        Result result = mock(Result.class);
        Response response = mock(Response.class);
        when(result.getResponse()).thenReturn(response);
        when(result.isSucceeded()).thenReturn(successfulResponse);
        when(result.isFailed()).thenReturn(!successfulResponse);
        HttpFields headers = mock(HttpFields.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(HttpHeader.CONTENT_LENGTH)).thenReturn(String.valueOf(resultSize));
        when(response.getStatus()).thenReturn(errorCode);
        when(response.getVersion()).thenReturn(HttpVersion.HTTP_1_1);

        // fire that event
        responseListener.onHeaders(response);
        responseListener.onContent(response, ByteBuffer.allocate(0));
        responseListener.onComplete(result);

        assertTrue(observer.getLatch().await(1000, TimeUnit.MILLISECONDS));
    }

    protected void mockAndMakeCall(Request request, int httpCode, String response, int responseSize,
                                   final AbstractHttpExecutable<Request> client, final ExecutionContext ec, final OperationKey key,
                                   final Object[] params, final ObservableObserver observer, final ExecutionVenue ev, TimeConstraints timeConstraints) throws InterruptedException {
        mockAndMakeCall(request, httpCode, response, responseSize, false, client, ec, key, params, observer, ev, timeConstraints);
    }

    private void mockAndMakeCall(Request request, int httpCode, String response, int responseSize, boolean ioException,
                                 final AbstractHttpExecutable<Request> client, final ExecutionContext ec, final OperationKey key,
                                 final Object[] params, final ObservableObserver observer, final ExecutionVenue ev, final TimeConstraints timeConstraints) throws InterruptedException {
        // calls first (but in new thread)
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.execute(ec, key, params, observer, ev, timeConstraints);
            }
        }).start();

        // mocks after
        fireResponse((CapturingRequest)request, httpCode, response, responseSize, observer, !ioException);
    }

    @Override
    protected int getEVPostSuccessWithMandatoryBodyAndQueryParameterPresent_ResultSize() {
        return 18;
    }

    // --------------- Specific tests ---------------
    @Test
    public void testEmptyResponseFromServer() throws IOException, InterruptedException {
        generateEV(tsd, null);

        when(mockedHttpErrorTransformer.convert(any(InputStream.class),  any(ExceptionFactory.class),
                anyInt())).thenReturn(new CougarClientException(ServerFaultCode.RemoteCougarCommunicationFailure, "bang"));

        final PassFailExecutionObserver observer = new PassFailExecutionObserver(true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.execute(createEC(null, null, false), TestServiceDefinition.TEST_MIXED,
                        new Object[] {TEST_TEXT, TEST_TEXT }, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
            }
        }).start();

        fireResponse(mockRequest, 200, null, 34, observer, true);

        ExecutionResult actual = observer.getResult();
        assertFalse(actual.isFault());
        assertNull(actual.getResult());
        assertEquals(34, ((ClientExecutionResult)actual).getResultSize());
    }

    @Test
    public void testFailingCall() throws IOException, InterruptedException {
        generateEV(tsd, null);

        when(mockedHttpErrorTransformer.convert(any(InputStream.class),  any(ExceptionFactory.class),
                anyInt())).thenReturn(new CougarClientException(ServerFaultCode.RemoteCougarCommunicationFailure, "bang"));

        final PassFailExecutionObserver observer = new PassFailExecutionObserver(false, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.execute(createEC(null, null, false), TestServiceDefinition.TEST_MIXED,
                        new Object[] {TEST_TEXT, TEST_TEXT }, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
            }
        }).start();

        fireResponse(mockRequest, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, 34, observer, true);

        ExecutionResult actual = observer.getResult();
        assertEquals(ExecutionResult.ResultType.Fault, actual.getResultType());
        assertNotNull(actual.getFault());
        assertNull(actual.getResult());
        assertNull(actual.getSubscription());
        assertEquals(34, ((ClientExecutionResult)actual).getResultSize());
    }

    @Test
    public void shouldStartupAndShutdown() throws Exception {
        final AsyncHttpExecutable executable = new AsyncHttpExecutable(new TestServiceBindingDescriptor(),contextEmitter, tracer, Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        executable.setRemoteAddress("http://localhost");
        executable.init();

        executable.shutdown();
    }

    @Test
    public void shouldStartupAndShutdownWithTimeout() throws Exception {
        final AsyncHttpExecutable executable = new AsyncHttpExecutable(new TestServiceBindingDescriptor(),contextEmitter, tracer, Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        executable.setRemoteAddress("http://localhost");
        executable.setConnectTimeout(5000);
        executable.setIdleTimeout(5000);
        executable.init();

        assertEquals(5000, executable.getClient().getConnectTimeout());
        assertEquals(5000, executable.getClient().getIdleTimeout());

        executable.shutdown();
    }

    @Test
    public void shouldThrowExceptionOnIOException() throws Exception {
        generateEV(tsd, null);
        final PassFailExecutionObserver mockObserver = new PassFailExecutionObserver(false, true);

        mockAndMakeCall(mockRequest, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, 34, true, client, createEC(null, null, false), TestServiceDefinition.TEST_GET, new Object[]{TEST_TEXT}, mockObserver, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
    }

    @Test
    public void shouldReturnTransportMetrics() throws Exception {
        MyHttpDestination dest = mock(MyHttpDestination.class);

        when(mockClient.getDestination(anyString(), anyString(), anyInt())).thenReturn(dest);

        ConnectionPool pool = mock(ConnectionPool.class);
        when(dest.getConnectionPool()).thenReturn(pool);
        when(pool.getIdleConnections()).thenReturn(queued(2));
        when(pool.getActiveConnections()).thenReturn(queued(4));
        ((AsyncHttpExecutable) client).setMaxConnectionsPerDestination(10);

        assertEquals(4, client.getTransportMetrics().getOpenConnections());
        assertEquals(2, client.getTransportMetrics().getFreeConnections());
        assertEquals(10, client.getTransportMetrics().getMaximumConnections());
        assertEquals(20, client.getTransportMetrics().getCurrentLoad());
    }

    private BlockingQueue<Connection> queued(int inside) {
        BlockingQueue<Connection> ret = new LinkedBlockingDeque<>();
        for (int i=0; i<inside; i++) {
            ret.add(new Connection() {
                @Override
                public void send(Request request, Response.CompleteListener listener) {}
                @Override
                public void close() {}
            });
        }
        return ret;
    }

    private class MyHttpDestination extends PoolingHttpDestination {

        private MyHttpDestination(HttpClient client, String scheme, String host, int port) {
            super(client, new Origin(scheme, host, port));
        }

        @Override
        protected void send() {
        }

        @Override
        protected void send(Connection connection, HttpExchange exchange) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void succeeded(Object result) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
