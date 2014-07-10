/*
 * Copyright 2013, The Sporting Exchange Limited
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
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.client.exception.ExceptionTransformer;
import com.betfair.cougar.client.query.QueryStringGeneratorFactory;
import com.betfair.cougar.core.api.client.ExceptionFactory;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.springframework.core.io.Resource;

/**
 * Factory that creates instances of synchronous http transport
 */
public class SyncHttpTransportFactory {

    private GeoLocationSerializer serializer;
    private String uuidHeader;
    private String uuidParentsHeader;
    private HttpRequestRetryHandler retryHandler;
    private DataBindingFactory dataBindingFactory;
    private QueryStringGeneratorFactory queryStringGeneratorFactory;
    private ExceptionTransformer exceptionTransformer;
    private int httpTimeout = -1;
    private int maxTotalConnections = -1;
    private int maxPerRouteConnections = -1;
    private boolean hardFailEnumDeserialisation;

    public void setGeoLocationSerializer(GeoLocationSerializer serializer) {
        this.serializer = serializer;
    }

    public void setUuidHeader(String uuidHeader) {
        this.uuidHeader = uuidHeader;
    }

    public void setUuidParentsHeader(String uuidParentsHeader) {
        this.uuidParentsHeader = uuidParentsHeader;
    }

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
        return getHttpTransport(remoteServerAddress, bindingDescriptor, exceptionFactory, null, null);
    }

    public HttpClientExecutable getHttpTransport(String remoteServerAddress, HttpServiceBindingDescriptor bindingDescriptor,
                                                 ExceptionFactory exceptionFactory, IdentityTokenResolver identityTokenResolver,
                                                 IdentityResolver identityResolver) {
        return getHttpTransport(remoteServerAddress, bindingDescriptor, exceptionFactory, identityTokenResolver, identityResolver,
                false, null, null, null, null, false);
    }

    public HttpClientExecutable getHttpTransport(String remoteServerAddress, HttpServiceBindingDescriptor bindingDescriptor,
                                                 ExceptionFactory exceptionFactory, IdentityTokenResolver identityTokenResolver,
                                                 IdentityResolver identityResolver, boolean sslEnabled, Resource keyStore,
                                                 String keyPassword, Resource trustStore, String trustPassword,
                                                 boolean hostnameVerificationDisabled) {
        final HttpClientExecutable client = new HttpClientExecutable(bindingDescriptor, serializer, uuidHeader, uuidParentsHeader);
        populateTransportAttributes(client, remoteServerAddress, exceptionFactory, identityTokenResolver, identityResolver,
                sslEnabled, keyStore, keyPassword, trustStore, trustPassword, hostnameVerificationDisabled);
        return client;
    }

    private void populateTransportAttributes(HttpClientExecutable client, String remoteServerAddress,
                                             ExceptionFactory exceptionFactory, IdentityTokenResolver identityTokenResolver,
                                             IdentityResolver identityResolver, boolean sslEnabled, Resource keyStore,
                                             String keyPassword, Resource trustStore, String trustPassword,
                                             boolean hostnameVerificationDisabled) {
        client.setRemoteAddress(remoteServerAddress);
        client.setExceptionFactory(exceptionFactory);
        client.setIdentityTokenResolver(identityTokenResolver);
        client.setIdentityResolver(identityResolver);
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
