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
import com.betfair.cougar.api.ExecutionContextImpl;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.client.exception.HTTPErrorToCougarExceptionTransformer;
import com.betfair.cougar.client.query.QueryStringGenerator;
import com.betfair.cougar.client.query.QueryStringGeneratorFactory;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptBody;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Date: 28/01/2013
 * Time: 14:48
 */
public abstract class AbstractHttpExecutableTest<HttpRequest> {
    protected AbstractHttpExecutable<HttpRequest> client;
    protected static final String SERVER_URI = "http://localhost:9001/";
    protected static final String TEST_TEXT = "hello";


    protected TestServiceDefinition tsd;

    protected QueryStringGeneratorFactory qsgf;
    protected QueryStringGenerator queryStringGenerator;

    protected  Marshaller mockedMarshaller;
    protected UnMarshaller mockedUnMarshaller;
    protected FaultMarshaller mockedFaultMarshaller;
    protected HTTPErrorToCougarExceptionTransformer mockedHttpErrorTransformer;

    protected CougarRequestFactory<HttpRequest> mockMethodFactory;
    protected ExecutionVenue ev;

    protected HttpRequest mockGetMethod;
    protected HttpRequest mockPostMethod;
    protected PassFailExecutionObserver observer;
    protected Tracer tracer;

    @BeforeClass
    public static void suppressLogs() {
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }

    private Answer<HttpRequest> httpMethodFactoryCreatePostAnswer = new Answer<HttpRequest>() {

        public HttpRequest answer(InvocationOnMock invocation) throws Throwable {
            Object[] args = invocation.getArguments();

            String uri = (String)args[0];
            String httpMethodString = (String)args[1];
            Message message = (Message)args[2];
            Marshaller marshaller = (Marshaller)args[3];
            String contentType = (String)args[4];

            assertEquals(SERVER_URI + "test/v1/post", uri);
            assertEquals("POST", httpMethodString);
            assertEquals(0, message.getHeaderMap().size());
            assertEquals(0, message.getQueryParmMap().size());
            assertEquals(1, message.getRequestBodyMap().size());
            assertTrue(message.getRequestBodyMap().containsKey("messagePOST"));
            assertNotNull(marshaller);
            assertEquals("application/json", contentType);

            return mockPostMethod;
        }
    };

    private Answer<HttpRequest> httpMethodFactoryCreateMixedAnswer = new Answer<HttpRequest>() {

        public HttpRequest answer(InvocationOnMock invocation) throws Throwable {
            Object[] args = invocation.getArguments();

            String uri = (String)args[0];
            String httpMethodString = (String)args[1];
            Message message = (Message)args[2];
            Marshaller marshaller = (Marshaller)args[3];
            String contentType = (String)args[4];

            assertEquals(SERVER_URI + "test/v1/mixed?messageMixedQUERY=hello", uri);
            assertEquals("POST", httpMethodString);
            assertEquals(0, message.getHeaderMap().size());
            assertEquals(1, message.getQueryParmMap().size());
            assertTrue(message.getQueryParmMap().containsKey("messageMixedQUERY"));
            assertEquals(1, message.getRequestBodyMap().size());
            assertTrue(message.getRequestBodyMap().containsKey("messageMixedPOST"));
            assertNotNull(marshaller);
            assertEquals("application/json", contentType);

            return mockPostMethod;
        }
    };

    @Before
    public void setup() throws Exception {
        // Add dependent mocks to cougar client execution venue
        tsd = new TestServiceDefinition();
        TestServiceBindingDescriptor tsbd = new TestServiceBindingDescriptor();
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());

        tracer = mock(Tracer.class);
        client = makeExecutable(tsbd);

        mockedMarshaller = mock(Marshaller.class);
        mockedUnMarshaller = mock(UnMarshaller.class);
        mockedFaultMarshaller = mock(FaultMarshaller.class);
        mockedHttpErrorTransformer = mock(HTTPErrorToCougarExceptionTransformer.class);

        mockMethodFactory = mock(CougarRequestFactory.class);
        when(mockMethodFactory.create(any(String.class), any(String.class), any(Message.class), any(Marshaller.class),
                any(String.class), any(ClientCallContext.class), any(TimeConstraints.class))).thenReturn(mockGetMethod);
        qsgf = mock(QueryStringGeneratorFactory.class);
        queryStringGenerator = mock(QueryStringGenerator.class);
        when(qsgf.getQueryStringGenerator()).thenReturn(queryStringGenerator);

        client.setRequestFactory(mockMethodFactory);
        client.setQueryStringGeneratorFactory(qsgf);

        DataBindingFactory mockedBindingFactory = mock(DataBindingFactory.class);
        when(mockedBindingFactory.getMarshaller()).thenReturn(mockedMarshaller);
        when(mockedBindingFactory.getUnMarshaller()).thenReturn(mockedUnMarshaller);
        when(mockedBindingFactory.getFaultMarshaller()).thenReturn(mockedFaultMarshaller);
        client.setDataBindingFactory(mockedBindingFactory);

        client.setRemoteAddress(SERVER_URI);

        preInit();

        client.init();
    }

    protected void preInit() throws Exception {

    }

    protected abstract AbstractHttpExecutable<HttpRequest> makeExecutable(HttpServiceBindingDescriptor tsbd)
            throws Exception;

    protected void generateEV(TestServiceDefinition tsd, String namespace) {
        ev = mock(ExecutionVenue.class);

        OperationDefinition[] opDefs = tsd.getOperationDefinitions();
        final Map<OperationKey, OperationDefinition> opMap = new HashMap<OperationKey, OperationDefinition>();
        for (OperationDefinition od : opDefs) {
            if (namespace == null) {
                opMap.put(od.getOperationKey(), od);
            } else {
                opMap.put(new OperationKey(od.getOperationKey(), namespace), od);
            }
        }
        when(ev.getOperationKeys()).thenReturn(opMap.keySet());
        when(ev.getOperationDefinition(any(OperationKey.class))).thenAnswer(new Answer<OperationDefinition>() {
            @Override
            public OperationDefinition answer(InvocationOnMock invocation) throws Throwable {
                OperationKey opKey = (OperationKey) invocation.getArguments()[0];
                return opMap.get(opKey);
            }
        });

    }

    public void makeSimpleCall(ExecutionContext ec) throws Exception {
        String response = "{\"result\":\"" + TEST_TEXT + "\"}";

        when(mockMethodFactory.create(anyString(), anyString(), any(Message.class), any(Marshaller.class),
                anyString(), any(ClientCallContext.class), any(TimeConstraints.class))).thenReturn(mockGetMethod);

        observer = new PassFailExecutionObserver(true, false);
        OperationKey key = TestServiceDefinition.TEST_GET;
        TestResponse tr = new TestResponse();
        tr.setResult(TEST_TEXT);
        when(mockedUnMarshaller.unmarshall(any(InputStream.class),
                any(ParameterType.class), anyString(), eq(true))).thenReturn(tr);


        mockAndMakeCall(mockGetMethod, HttpServletResponse.SC_OK, response, 18, client, ec, key, new Object[]{TEST_TEXT}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        TestResponse resp = (TestResponse)observer.getResult().getResult();
        assertEquals(TEST_TEXT, resp.getResult());
        assertEquals(ExecutionResult.ResultType.Success, observer.getResult().getResultType());
        assertEquals(18, ((ClientExecutionResult)observer.getResult()).getResultSize());
    }

    @Test
    public void testEVGetSuccess() throws Exception {
        generateEV(tsd, null);
        makeSimpleCall(createEC(null, null, false));
    }

    @Test
    public void testIdentityTokenWriting() throws Exception {
        IdentityResolver resolver = mock((IdentityResolver.class));
        IdentityTokenResolver tokenResolver = mock((IdentityTokenResolver.class));
        client.setIdentityResolver(resolver);
        client.setIdentityTokenResolver(tokenResolver);
        when(tokenResolver.isRewriteSupported()).thenReturn(true);
        final List<IdentityToken> tokens = new ArrayList<IdentityToken>();
        when(resolver.tokenise(any(IdentityChain.class))).thenReturn(tokens);

        generateEV(tsd, null);
        makeSimpleCall(createEC(null, null, true));
        verify(resolver).tokenise(any(IdentityChain.class));
        verify(tokenResolver).rewrite(eq(tokens), any(HttpUriRequest.class));
    }


    @Test
    public void testIdentityTokenWritingRewriteOff() throws Exception {
        IdentityResolver resolver = mock((IdentityResolver.class));
        IdentityTokenResolver tokenResolver = mock((IdentityTokenResolver.class));
        client.setIdentityResolver(resolver);
        client.setIdentityTokenResolver(tokenResolver);
        when(tokenResolver.isRewriteSupported()).thenReturn(false);
        final List<IdentityToken> tokens = new ArrayList<IdentityToken>();
        when(resolver.tokenise(any(IdentityChain.class))).thenReturn(tokens);

        generateEV(tsd, null);
        makeSimpleCall(createEC(null, null, true));
        verify(resolver, never()).tokenise(any(IdentityChain.class));
        verify(tokenResolver, never()).rewrite(eq(tokens), any(HttpUriRequest.class));
    }

    @Test
    public void testEVGetSuccessWithNamespaceMatch() throws Exception {
        generateEV(tsd, "MyNamespace");
        String response = "{\"result\":\"" + TEST_TEXT + "\"}";

        when(mockMethodFactory.create(anyString(), anyString(), any(Message.class), any(Marshaller.class),
                anyString(), any(ClientCallContext.class), any(TimeConstraints.class))).thenReturn(mockGetMethod);

        TestResponse testResponse = new TestResponse();
        testResponse.setResult(TEST_TEXT);

        OperationKey key = new OperationKey(TestServiceDefinition.TEST_GET, "MyNamespace");
        observer = new PassFailExecutionObserver(true, false);

        TestResponse tr = new TestResponse();
        tr.setResult(TEST_TEXT);
        when(mockedUnMarshaller.unmarshall(any(InputStream.class),
                any(ParameterType.class), anyString(), eq(true))).thenReturn(tr);

        mockAndMakeCall(mockGetMethod, HttpServletResponse.SC_OK, response, 18, client, createEC(null, null, false), key, new Object[]{TEST_TEXT}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        TestResponse resp = (TestResponse)observer.getResult().getResult();
        assertEquals(TEST_TEXT, resp.getResult());
        assertEquals(ExecutionResult.ResultType.Success, observer.getResult().getResultType());
        assertEquals(18, ((ClientExecutionResult)observer.getResult()).getResultSize());
    }

    @Test
    public void testEVFailWrongNamespace() {
        generateEV(tsd, "MyNamespace");
        OperationKey key = new OperationKey(TestServiceDefinition.TEST_GET, "WrongNamespace");
        observer = new PassFailExecutionObserver(true, false);

        try {
            client.execute(createEC(null, null, false), key, new Object[] {TEST_TEXT }, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
        } catch (NullPointerException e) {
            // correct - the provided key was not right
            assert true;
        }
    }

    @Test
    public void testEVFailNoNamespaceProvided() {
        generateEV(tsd, "MyNamespace");
        OperationKey key = TestServiceDefinition.TEST_GET;
        observer = new PassFailExecutionObserver(true, false);

        try {
            client.execute(createEC(null, null, false), key, new Object[] {TEST_TEXT }, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
        } catch (NullPointerException e) {
            // correct - the provided key was not right
        }
    }


    @Test
    public void changeRemoteAddress() throws InterruptedException {
        generateEV(tsd, null);

        final int maxThreads = 5;
        Thread[] threads = new Thread[maxThreads];

        final OperationKey key = new OperationKey(TestServiceDefinition.TEST_GET, null);

        final AtomicInteger failureCount = new AtomicInteger(0);

        for (int threadCount = 0; threadCount < maxThreads; threadCount++) {
            final Integer threadId = new Integer(threadCount);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    int iterations = 100;
                    ExecutionContext ec = createEC(null, null, false);
                    do {
                        observer = new PassFailExecutionObserver(true, true) {
                            @Override
                            public void onResult(ExecutionResult executionResult) {

                            }
                        };

                        try {
                            client.execute(ec, key, new Object[] {TEST_TEXT }, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
                            if (iterations % 5 == 0 && threadId == 0) {
                                client.setRemoteAddress("http://localhost:" + iterations + "/");
                            }
                        } catch (Exception ex) {
                            failureCount.incrementAndGet();
                        }
                    } while (--iterations > 0);
                }
            });
            threads[threadCount] = t;
            t.start();

        }

        for (int i = 0; i < maxThreads; i++) {
            threads[i].join();
        }

        if (failureCount.get() > 0) {
            fail("An exception occurred during multithreaded test of remote address modification");
        }

    }

    @Test
    public void testEVFailNoNamespaceRegistered() {
        generateEV(tsd, null);
        OperationKey key = new OperationKey(TestServiceDefinition.TEST_GET, "MyNamespace");
        observer = new PassFailExecutionObserver(true, false);

        try {
            client.execute(createEC(null, null, false), key, new Object[] {TEST_TEXT }, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
        } catch (NullPointerException e) {
            // correct - the provided key was not right
        }
    }

    @Test
    public void testEVPostSuccessWithMandatoryBodyParameterPresent() throws Exception {
        generateEV(tsd, null);
        when(queryStringGenerator.generate(any(Map.class))).thenReturn("");

        when(mockMethodFactory.create(anyString(), anyString(), any(Message.class), any(Marshaller.class),
                anyString(), any(ClientCallContext.class), any(TimeConstraints.class))).thenAnswer(httpMethodFactoryCreatePostAnswer);
        String response = "{\"result\":\"" + TEST_TEXT + "\"}";

        TestResponse tr = new TestResponse();
        tr.setResult(TEST_TEXT);
        when(mockedUnMarshaller.unmarshall(any(InputStream.class), any(ParameterType.class),
                anyString(), eq(true))).thenReturn(tr);

        TestResponse testResponse = new TestResponse();
        testResponse.setResult(TEST_TEXT);

        OperationKey key = TestServiceDefinition.TEST_POST;
        observer = new PassFailExecutionObserver(true, false);

        mockAndMakeCall(mockPostMethod, HttpServletResponse.SC_OK, response, 18, client, createEC(null, null, false), key, new Object[]{TEST_TEXT}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        TestResponse resp = (TestResponse)observer.getResult().getResult();
        assertEquals(TEST_TEXT, resp.getResult());
        assertEquals(ExecutionResult.ResultType.Success, observer.getResult().getResultType());
        assertEquals(18, ((ClientExecutionResult)observer.getResult()).getResultSize());
    }

    @Test
    public void testEVPostSuccessWithMandatoryBodyAndQueryParameterPresent() throws Exception {
        generateEV(tsd, null);
        when(queryStringGenerator.generate(any(Map.class))).thenReturn("?messageMixedQUERY=hello");
        when(mockMethodFactory.create(anyString(), anyString(), any(Message.class), any(Marshaller.class),
                anyString(), any(ClientCallContext.class), any(TimeConstraints.class))).thenAnswer(httpMethodFactoryCreateMixedAnswer);
        String response = "{\"result\":\"" + TEST_TEXT + TEST_TEXT + "\"}";


        TestResponse testResponse = new TestResponse();
        testResponse.setResult(TEST_TEXT + TEST_TEXT);

        OperationKey key = TestServiceDefinition.TEST_MIXED;
        observer = new PassFailExecutionObserver(true, false);

        when(mockedUnMarshaller.unmarshall(any(InputStream.class), any(ParameterType.class), anyString(), eq(true)))
                .thenReturn(testResponse);

        mockAndMakeCall(mockPostMethod, HttpServletResponse.SC_OK, response, 18, client, createEC(null, null, false), key, new Object[]{TEST_TEXT, TEST_TEXT}, observer, ev, DefaultTimeConstraints.NO_CONSTRAINTS);

        TestResponse resp = (TestResponse)observer.getResult().getResult();
        assertEquals(TEST_TEXT + TEST_TEXT, resp.getResult());
        assertEquals(ExecutionResult.ResultType.Success, observer.getResult().getResultType());
        assertEquals(getEVPostSuccessWithMandatoryBodyAndQueryParameterPresent_ResultSize(), ((ClientExecutionResult)observer.getResult()).getResultSize());
    }

    protected abstract int getEVPostSuccessWithMandatoryBodyAndQueryParameterPresent_ResultSize();


    @Test(expected = IllegalArgumentException.class)
    public void testExtractPortFromBadAddresses() throws IOException {
        HttpClientExecutable executable = new HttpClientExecutable(null, new HttpContextEmitter(new DefaultGeoLocationSerializer(),"X-REQUEST-UUID","X-REQUEST-UUID-PARENTS"),tracer);
        executable.setRemoteAddress("NOT ASSIGNED");
        executable.extractPortFromAddress();
    }

    @Test
    public void testExtractPortFromAddress() throws IOException {
        HttpClientExecutable executable = new HttpClientExecutable(null, new HttpContextEmitter(new DefaultGeoLocationSerializer(),"X-REQUEST-UUID","X-REQUEST-UUID-PARENTS"),tracer);
        executable.setRemoteAddress("http://wibble.com:3939/www");
        int actual = executable.extractPortFromAddress();
        assertEquals(3939, actual);

        executable.setRemoteAddress("http://wibble.com:25000");
        actual = executable.extractPortFromAddress();
        assertEquals(25000, actual);
    }

    protected ExecutionContext createEC(final String remoteIp, final String customerIp, boolean trace) {
        ExecutionContextImpl eci = new ExecutionContextImpl();
        eci.setTraceLoggingEnabled(trace);
        if (remoteIp != null || customerIp != null) {
            GeoLocationDetails gld = new GeoLocationDetails() {
                @Override
                public String getRemoteAddr() {
                    return remoteIp;
                }

                @Override
                public List<String> getResolvedAddresses() {
                    return Collections.singletonList(customerIp);
                }

                @Override
                public String getCountry() {
                    return null;
                }

                @Override
                public boolean isLowConfidenceGeoLocation() {
                    return false;
                }

                @Override
                public String getLocation() {
                    return null;
                }

                @Override
                public String getInferredCountry() {
                    return null;
                }
            };
            eci.setGeoLocationDetails(gld);
        }
        return eci;
    }


    protected static class TestServiceDefinition extends ServiceDefinition {
        private static final String SERVICE_NAME = "test";
        private static final ServiceVersion SERVICE_VERSION = new ServiceVersion("v1.0");

        public static final OperationKey TEST_GET = new OperationKey(SERVICE_VERSION, SERVICE_NAME,
                "TEST_GET",  OperationKey.Type.Request);
        public static final OperationKey TEST_POST = new OperationKey(SERVICE_VERSION, SERVICE_NAME,
                "TEST_POST", OperationKey.Type.Request);
        public static final OperationKey TEST_MIXED = new OperationKey(SERVICE_VERSION, SERVICE_NAME,
                "TEST_MIXED", OperationKey.Type.Request);

        private OperationDefinition[] operationDefinitions = new OperationDefinition[] {
                new SimpleOperationDefinition(TEST_GET, new Parameter[] {
                        new Parameter("message", new ParameterType(String.class, null), true) },
                        new ParameterType(TestResponse.class, null)),

                new SimpleOperationDefinition(TEST_POST, new Parameter [] {
                        new Parameter("messagePOST", new ParameterType(String.class, null), true) },
                        new ParameterType(TestResponse.class, null)),

                new SimpleOperationDefinition(TEST_MIXED, new Parameter [] {
                        new Parameter("messageMixedPOST", new ParameterType(String.class, null), true),
                        new Parameter("messageMixedQUERY", new ParameterType(String.class, null), true) },
                        new ParameterType(TestResponse.class, null))
        };

        public TestServiceDefinition() {
            super.init();
        }
        @Override
        public String getServiceName() {
            return SERVICE_NAME;
        }

        @Override
        public ServiceVersion getServiceVersion() {
            return SERVICE_VERSION;
        }

        @Override
        public OperationDefinition[] getOperationDefinitions() {
            return operationDefinitions;
        }
    }

    protected static class PassFailExecutionObserver implements ExecutionObserver, ObservableObserver {

        private boolean passOnResult;
        private boolean passOnException;
        private ExecutionResult result;
        private CountDownLatch latch = new CountDownLatch(1);

        protected PassFailExecutionObserver(boolean passOnResult, boolean passOnException) {
            this.passOnResult = passOnResult;
            this.passOnException = passOnException;

        }
        @Override
        public void onResult(ExecutionResult result) {
            this.result = result;
            latch.countDown();

            switch (result.getResultType()) {
                case Success:
                    if (!passOnResult) {
                        fail("Did not expect a successful ExecutionResult");
                    }
                    break;
                case Fault:
                    if (!passOnException) {
                        fail("Did not expect a fault ExecutionResult");
                    }
                    break;
                default:
                    fail("Incorrect ExecutionResult type received: " + result.getResultType());
            }
        }

        public ExecutionResult getResult() {
            return result;
        }

        public CountDownLatch getLatch() {
            return latch;
        }
    }

    protected static class TestServiceBindingDescriptor implements HttpServiceBindingDescriptor {

        private final ServiceVersion serviceVersion = new ServiceVersion("v1.0");
        private final String serviceName = "test";

        public TestServiceBindingDescriptor() {
            List<RescriptParamBindingDescriptor> testGetParamBindings =
                    new ArrayList<RescriptParamBindingDescriptor>();
            testGetParamBindings.add(new RescriptParamBindingDescriptor("message",
                    RescriptParamBindingDescriptor.ParamSource.QUERY));
            testGetDescriptor = new RescriptOperationBindingDescriptor(TestServiceDefinition.TEST_GET,
                    "/get", "GET", testGetParamBindings, TestResponse.class);

            List<RescriptParamBindingDescriptor> testPostParamBindings =
                    new ArrayList<RescriptParamBindingDescriptor>();
            testPostParamBindings.add(new RescriptParamBindingDescriptor("messagePOST",
                    RescriptParamBindingDescriptor.ParamSource.BODY));
            testPostDescriptor = new RescriptOperationBindingDescriptor(TestServiceDefinition.TEST_POST,
                    "/post", "POST", testPostParamBindings, TestResponse.class, TestBody.class);

            List<RescriptParamBindingDescriptor> testMixedParamBindings =
                    new ArrayList<RescriptParamBindingDescriptor>();
            testMixedParamBindings.add(new RescriptParamBindingDescriptor("messageMixedPOST",
                    RescriptParamBindingDescriptor.ParamSource.BODY));
            testMixedParamBindings.add(new RescriptParamBindingDescriptor("messageMixedQUERY",
                    RescriptParamBindingDescriptor.ParamSource.QUERY));
            testMixedDescriptor = new RescriptOperationBindingDescriptor(TestServiceDefinition.TEST_MIXED,
                    "/mixed", "POST", testMixedParamBindings, TestResponse.class, TestBody.class);

            operations = new RescriptOperationBindingDescriptor[] {
                    testGetDescriptor,
                    testPostDescriptor,
                    testMixedDescriptor
            };
        }

        @Override
        public Protocol getServiceProtocol() {
            return Protocol.RESCRIPT;
        }

        @Override
        public String getServiceContextPath() {
            return "/test/";
        }

        @Override
        public RescriptOperationBindingDescriptor[] getOperationBindings() {
            return operations;
        }

        @Override
        public String getServiceName() {
            return serviceName;
        }

        @Override
        public ServiceVersion getServiceVersion() {
            return serviceVersion;
        }

        private final RescriptOperationBindingDescriptor[] operations;

        private final RescriptOperationBindingDescriptor testGetDescriptor;
        private final RescriptOperationBindingDescriptor testPostDescriptor;
        private final RescriptOperationBindingDescriptor testMixedDescriptor;


    }
    private static class TestBody implements RescriptBody {

        private Map<String, Object> parameters = new HashMap<String, Object>();

        @Override
        public Object getValue(String name) {
            return parameters.get(name);
        }

        private String message;

        @XmlElement(name = "message")
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
            parameters.put("message", message);
        }
    }

    protected static class TestResponse implements RescriptResponse {

        private Object result;

        @Override
        public Object getResult() {
            return result;
        }

        @Override
        public void setResult(Object result) {
            this.result = result;

        }
    }

    protected static interface ObservableObserver extends ExecutionObserver {
        CountDownLatch getLatch();
    }

    protected abstract void mockAndMakeCall(HttpRequest request, int httpCode, String response, int responseSize,
                                            AbstractHttpExecutable<HttpRequest> client, ExecutionContext ec, OperationKey key,
                                            Object[] params, ObservableObserver observer, ExecutionVenue ev, TimeConstraints timeConstraints) throws InterruptedException;
}
