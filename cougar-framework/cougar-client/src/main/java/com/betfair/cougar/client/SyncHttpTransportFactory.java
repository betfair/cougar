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

import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.client.exception.ExceptionTransformer;
import com.betfair.cougar.client.query.QueryStringGeneratorFactory;
import com.betfair.cougar.core.api.client.ExceptionFactory;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.springframework.core.io.Resource;

/**
 * Factory that creates instances of synchronous http transport
 */
public class SyncHttpTransportFactory {

    private HttpRequestRetryHandler retryHandler;
    private DataBindingFactory dataBindingFactory;
    private QueryStringGeneratorFactory queryStringGeneratorFactory;
    private ExceptionTransformer exceptionTransformer;
    private ContextEmitter contextEmitter;
    private Tracer tracer;
    private int httpTimeout = -1;
    private int maxTotalConnections = -1;
    private int maxPerRouteConnections = -1;
    private boolean hardFailEnumDeserialisation;

    public void setRetryHandler(HttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    public void setDataBindingFactory(DataBindingFactory dataBindingFactory) {
        this.dataBindingFactory = dataBindingFactory;
    }

    public void setQueryStringGeneratorFactory(QueryStringGeneratorFactory queryStringGeneratorFactory) {
        this.queryStringGeneratorFactory = queryStringGeneratorFactory;
    }

    public void setExceptionTransformer(ExceptionTransformer exceptionTransformer) {
        this.exceptionTransformer = exceptionTransformer;
    }

    public void setContextEmitter(ContextEmitter contextEmitter) {
        this.contextEmitter = contextEmitter;
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void setHttpTimeout(int httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setMaxPerRouteConnections(int maxPerRouteConnections) {
        this.maxPerRouteConnections = maxPerRouteConnections;
    }

    public void setHardFailEnumDeserialisation(boolean hardFailEnumDeserialisation) {
        this.hardFailEnumDeserialisation = hardFailEnumDeserialisation;
    }

    public HttpClientExecutable getHttpTransport(String remoteServerAddress, HttpServiceBindingDescriptor bindingDescriptor,
                                                 ExceptionFactory exceptionFactory) {
        return getHttpTransport(remoteServerAddress, bindingDescriptor, exceptionFactory,
                false, null, null, null, null, false);
    }

    public HttpClientExecutable getHttpTransport(String remoteServerAddress, HttpServiceBindingDescriptor bindingDescriptor,
                                                 ExceptionFactory exceptionFactory, boolean sslEnabled, Resource keyStore,
                                                 String keyPassword, Resource trustStore, String trustPassword,
                                                 boolean hostnameVerificationDisabled) {
        final HttpClientExecutable client = new HttpClientExecutable(bindingDescriptor, contextEmitter, tracer);
        populateTransportAttributes(client, remoteServerAddress, exceptionFactory,
                sslEnabled, keyStore, keyPassword, trustStore, trustPassword, hostnameVerificationDisabled);
        return client;
    }

    private void populateTransportAttributes(HttpClientExecutable client, String remoteServerAddress,
                                             ExceptionFactory exceptionFactory, boolean sslEnabled, Resource keyStore,
                                             String keyPassword, Resource trustStore, String trustPassword,
                                             boolean hostnameVerificationDisabled) {
        client.setRemoteAddress(remoteServerAddress);
        client.setExceptionFactory(exceptionFactory);
        client.setTransportSSLEnabled(sslEnabled);
        client.setHttpsKeystore(keyStore);
        client.setHttpsKeyPassword(keyPassword);
        client.setHttpsTruststore(trustStore);
        client.setHttpsTrustPassword(trustPassword);
        client.setHostnameVerificationDisabled(hostnameVerificationDisabled);

        client.setRetryHandler(retryHandler);
        client.setDataBindingFactory(dataBindingFactory);
        client.setQueryStringGeneratorFactory(queryStringGeneratorFactory);
        client.setExceptionTransformer(exceptionTransformer);
        client.setConnectTimeout(httpTimeout);
        client.setMaxTotalConnections(maxTotalConnections);
        client.setMaxPerRouteConnections(maxPerRouteConnections);
        client.setHardFailEnumDeserialisation(hardFailEnumDeserialisation);
    }
}
