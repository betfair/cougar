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

package com.betfair.cougar.client;

import com.betfair.cougar.client.api.ContextEmitter;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * {@code HttpClientCougarRequestFactory} is an implementation of {@link CougarRequestFactory} for synchronous
 * http client.
 * See {@link HttpClientExecutable}
 */
public class HttpClientCougarRequestFactory extends CougarRequestFactory<HttpUriRequest> {

    public HttpClientCougarRequestFactory(ContextEmitter<HttpUriRequest,List<Header>> emission) {
        super(emission);
    }

    @Override
    protected void addHeaders(HttpUriRequest httpRequest, List<Header> headers) {
        for (Header header : headers) {
            if (header.getValue() != null) {
                httpRequest.addHeader(header);
            }
        }
    }

    @Override
    protected void addPostEntity(HttpUriRequest httpRequest, String postEntity, String contentType) {
        try {
            if (httpRequest instanceof HttpPost) {
                ((HttpPost) httpRequest).setEntity(new StringEntity(postEntity, contentType, UTF8));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected HttpUriRequest createRequest(String method, String uri) {
        if ("GET".equals(method)) {
            return new HttpGet(uri);
        } else if ("POST".equals(method)) {
            return new HttpPost(uri);
        } else {
            throw new UnsupportedOperationException("don't know how to handle method:" + method);
        }
    }
}
