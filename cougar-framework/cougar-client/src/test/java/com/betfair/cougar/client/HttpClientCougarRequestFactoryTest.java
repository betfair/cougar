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
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Date: 30/01/2013
 * Time: 15:14
 */
public class HttpClientCougarRequestFactoryTest {

    @Mock
    private ClientCallContext mockCallContext;
    @Mock
    private Message mockMessage;
    @Mock
    private Marshaller mockMarshaller;
    @Mock
    private GeoLocationDetails mockGeoLocation;
    @Mock
    private TimeConstraints mockTimeConstraints;

    private HttpClientCougarRequestFactory factory = new HttpClientCougarRequestFactory(new HttpContextEmitter<HttpUriRequest>(new DefaultGeoLocationSerializer(), "X-REQUEST-UUID", "X-REQUEST-UUID-PARENTS"));

    private String uri = "http://Some.uri";
    private String contentType = "application/X-my-type";

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        Answer<Void> postAnswer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                ByteArrayOutputStream os = (ByteArrayOutputStream) invocationOnMock.getArguments()[0];

                os.write("some post data".getBytes());
                return null;
            }
        };

        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
        when(mockCallContext.getRequestUUID()).thenReturn(new RequestUUIDImpl());
        when(mockMessage.getHeaderMap()).thenReturn(Collections.<String, Object>emptyMap());
        when(mockMessage.getRequestBodyMap()).thenReturn(Collections.<String, Object>singletonMap("key", "value"));
        doAnswer(postAnswer).when(mockMarshaller).marshall(any(ByteArrayOutputStream.class), anyObject(),
                anyString(), eq(true));
    }

    @Test
    public void shouldCreateGetRequest() {
        HttpUriRequest httpExchange = factory.create(uri, "GET", mockMessage, mockMarshaller, contentType,
                mockCallContext, mockTimeConstraints);

        assertTrue(httpExchange instanceof HttpGet);
        assertEquals("GET", httpExchange.getMethod());
        assertEquals(uri, httpExchange.getURI().toString());
        assertEquals(5, httpExchange.getAllHeaders().length);
    }

    @Test
    public void shouldCreatePostRequest() throws Exception {
        HttpUriRequest httpExchange = factory.create(uri, "POST", mockMessage, mockMarshaller, contentType,
                mockCallContext, mockTimeConstraints);

        assertTrue(httpExchange instanceof HttpPost);
        assertEquals("POST", httpExchange.getMethod());
        assertEquals(uri, httpExchange.getURI().toString());
        assertEquals(5, httpExchange.getAllHeaders().length);
        assertEquals(contentType + "; charset=utf-8",
                ((HttpPost) httpExchange).getEntity().getContentType().getValue());
        assertEquals("some post data", IOUtils.toString(((HttpPost) httpExchange).getEntity().getContent()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotCreateUnknownMethod() {
        factory.create(uri, "TRACE", mockMessage, mockMarshaller, contentType, mockCallContext, mockTimeConstraints);
    }

    @Test
    public void shouldAddNewHeadersWithoutDeleteTheExistingHeaders() {

        HttpUriRequest request = new HttpGet();
        request.setHeader("X-UUID", "1111-111-111-111");

        List<Header> headers = new ArrayList<>(1);
        headers.add(new BasicHeader("NEW-HEADER", "value"));
        factory.addHeaders(request, headers);

        assertEquals(2, request.getAllHeaders().length);
        assertEquals("1111-111-111-111", request.getFirstHeader("X-UUID").getValue());
        assertEquals("value", request.getFirstHeader("NEW-HEADER").getValue());
    }
}


