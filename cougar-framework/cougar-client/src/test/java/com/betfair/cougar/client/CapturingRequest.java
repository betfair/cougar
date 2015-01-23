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

import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.util.Fields;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
*
*/
class CapturingRequest implements Request {

    private AtomicReference<Response.CompleteListener> completeListener = new AtomicReference<>();
    private CountDownLatch sendLatch = new CountDownLatch(1);

    @Override
    public long getConversationID() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public Request onComplete(Response.CompleteListener listener) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getScheme() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request scheme(String scheme) {
        return this;
    }

    @Override
    public String getHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMethod() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request method(HttpMethod method) {
        return this;
    }

    @Override
    public Request method(String method) {
        return this;
    }

    @Override
    public String getPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request path(String path) {
        return this;
    }

    @Override
    public String getQuery() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URI getURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HttpVersion getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request version(HttpVersion version) {
        return this;
    }

    @Override
    public Fields getParams() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request param(String name, String value) {
        return this;
    }

    @Override
    public HttpFields getHeaders() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request header(String name, String value) {
        return this;
    }

    @Override
    public Request header(HttpHeader header, String value) {
        return this;
    }

    @Override
    public Request attribute(String name, Object value) {
        return this;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ContentProvider getContent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request content(ContentProvider content) {
        return this;
    }

    @Override
    public Request content(ContentProvider content, String contentType) {
        return this;
    }

    @Override
    public Request file(Path file) throws IOException {
        return this;
    }

    @Override
    public Request file(Path file, String contentType) throws IOException {
        return this;
    }

    @Override
    public String getAgent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request agent(String agent) {
        return this;
    }

    @Override
    public long getIdleTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request idleTimeout(long timeout, TimeUnit unit) {
        return this;
    }

    @Override
    public long getTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request timeout(long timeout, TimeUnit unit) {
        return this;
    }

    @Override
    public boolean isFollowRedirects() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request followRedirects(boolean follow) {
        return this;
    }

    @Override
    public <T extends RequestListener> List<T> getRequestListeners(Class<T> listenerClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Request listener(Listener listener) {
        return this;
    }

    @Override
    public Request onRequestQueued(QueuedListener listener) {
        return this;
    }

    @Override
    public Request onRequestBegin(BeginListener listener) {
        return this;
    }

    @Override
    public Request onRequestHeaders(HeadersListener listener) {
        return this;
    }

    @Override
    public Request onRequestCommit(CommitListener listener) {
        return this;
    }

    @Override
    public Request onRequestContent(ContentListener listener) {
        return this;
    }

    @Override
    public Request onRequestSuccess(SuccessListener listener) {
        return this;
    }

    @Override
    public Request onRequestFailure(FailureListener listener) {
        return this;
    }

    @Override
    public Request onResponseBegin(Response.BeginListener listener) {
        return this;
    }

    @Override
    public Request onResponseHeader(Response.HeaderListener listener) {
        return this;
    }

    @Override
    public Request onResponseHeaders(Response.HeadersListener listener) {
        return this;
    }

    @Override
    public Request onResponseContent(Response.ContentListener listener) {
        return this;
    }

    @Override
    public Request onResponseSuccess(Response.SuccessListener listener) {
        return this;
    }

    @Override
    public Request onResponseFailure(Response.FailureListener listener) {
        return this;
    }

    @Override
    public ContentResponse send() throws InterruptedException, TimeoutException, ExecutionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void send(Response.CompleteListener listener) {
        completeListener.set(listener);
        sendLatch.countDown();

    }

    public Response.CompleteListener awaitSend(long time, TimeUnit unit) throws InterruptedException {
        if (sendLatch.await(time, unit)) {
            return completeListener.get();
        }
        return null;
    }

    @Override
    public boolean abort(Throwable cause) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Throwable getAbortCause() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
