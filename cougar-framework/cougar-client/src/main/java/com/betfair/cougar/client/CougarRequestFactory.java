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

import com.betfair.cougar.CougarVersion;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpHeaders.*;

/**
 * {@code CougarRequestFactory} describes contract and base behaviour for creating http requests on client side.
 * Particular implementations are to be used within each actual transport.
 */
public abstract class CougarRequestFactory<HR> {
    protected static final String UTF8 = "utf-8";

    // todo: base this on app name as well?
    static final String USER_AGENT_HEADER = "Cougar Client " + CougarVersion.getVersion();

    private final ContextEmitter<HR,List<Header>> contextEmission;

    private volatile boolean gzipCompressionEnabled;

    public CougarRequestFactory(ContextEmitter<HR,List<Header>> contextEmission) {
        this.contextEmission = contextEmission;
    }

    public HR create(final String uri, final String httpMethod, final Message message,
                              final Marshaller marshaller, final String contentType, final ClientCallContext ctx, final TimeConstraints timeConstraints) {

        final HR httpRequest = createRequest(httpMethod, uri);

        if ("POST".equalsIgnoreCase(httpMethod)) {
            addPostEntity(httpRequest, createPostEntity(message, marshaller), contentType);
        }

        List<Header> coreHeaders = constructRequestHeaders(message, contentType, timeConstraints);
        contextEmission.emit(ctx, httpRequest, coreHeaders);

        addHeaders(httpRequest, coreHeaders);
        return httpRequest;
    }

    /**
     * Add the specified headers to the request.
     *
     * @param httpRequest the request
     * @param headers the headers to add.
     */
    protected abstract void addHeaders(HR httpRequest, List<Header> headers);

    /**
     * Adds the specified entity to the request as a POST body, with the specified content-type.
     *
     * @param httpRequest the request
     * @param postEntity the entity
     * @param contentType the content type
     */
    protected abstract void addPostEntity(HR httpRequest, String postEntity, String contentType);


    /**
     * Make a HTTP request object suitable for connecting to the specified URL with the given method.
     *
     * @param httpMethod the method
     * @param uri the uri
     * @return a HTTP request object
     * @throws UnsupportedOperationException if the method isn't supported
     */
    protected abstract HR createRequest(String httpMethod, String uri);


    private String createPostEntity(final Message message, final Marshaller marshaller) {
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshall(os, message.getRequestBodyMap(), UTF8, true);
            return os.toString(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Header> constructRequestHeaders(final Message message, final String contentType, TimeConstraints timeConstraints) {

        final List<Header> result = new ArrayList<Header>();

        result.add(new BasicHeader(ACCEPT, contentType));
        if (gzipCompressionEnabled) {
            result.add(new BasicHeader(ACCEPT_ENCODING, "gzip"));
        }
        result.add(new BasicHeader(USER_AGENT, USER_AGENT_HEADER));

        if (timeConstraints.getTimeRemaining() != null) {
            result.add(new BasicHeader("X-RequestTimeout", String.valueOf(timeConstraints.getTimeRemaining())));
        }

        for (Map.Entry<String, Object> entry : message.getHeaderMap().entrySet()) {
            result.add(new BasicHeader(entry.getKey(), entry.getValue().toString()));
        }

        return result;
    }

    public void setGzipCompressionEnabled(boolean gzipCompressionEnabled) {
        this.gzipCompressionEnabled = gzipCompressionEnabled;
    }

}
