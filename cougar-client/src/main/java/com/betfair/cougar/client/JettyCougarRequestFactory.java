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
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import java.util.List;

/**
 * {@code JettyCougarRequestFactory} is an implementation of {@link CougarRequestFactory} for asynchronous
 * http client.
 * See {@link AsyncHttpExecutable}
 */
class JettyCougarRequestFactory extends CougarRequestFactory<Request> {

    private AsyncHttpExecutable executable;

    public JettyCougarRequestFactory(ContextEmitter<Request,List<Header>> emission) {
        super(emission);
    }

    void setExecutable(AsyncHttpExecutable executable) {
        this.executable = executable;
    }

    @Override
    protected void addHeaders(Request request, List<Header> headers) {
        for (Header header : headers) {
            if (header.getValue() != null) {
                request.header(header.getName(), header.getValue());
            }
        }
    }

    @Override
    protected void addPostEntity(Request request, String postEntity, String contentType) {
        request.header(HttpHeader.CONTENT_TYPE, contentType +  "; charset=utf-8");
        request.content(new StringContentProvider(postEntity, UTF8));
    }

    @Override
    protected Request createRequest(String httpMethod, String uri) {
        if (!"GET".equals(httpMethod) && !("POST".equals(httpMethod))) {
            throw new UnsupportedOperationException("don't know how to handle method:" + httpMethod);
        }

        return executable.getClient().newRequest(uri).method(httpMethod);
    }

}

