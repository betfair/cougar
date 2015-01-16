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

import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.core.api.client.TransportMetrics;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.util.KeyStoreManagement;
import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of client executable using synchronous implementation of HTTP ReScript protocol.
 */
@ManagedResource
public class HttpClientExecutable extends AbstractHttpExecutable<HttpUriRequest> implements BeanNameAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientExecutable.class);

    private HttpClient client;
    private HttpRequestRetryHandler retryHandler;
    private CougarClientConnManager clientConnectionManager;
    private String beanName;
    private JMXControl jmxControl;
    private UserTokenHandler userTokenHandler;

    private HttpClientTransportMetrics metrics;

    public HttpClientExecutable(final HttpServiceBindingDescriptor bindingDescriptor,
                                final ContextEmitter emission,
                                final Tracer tracer) {
        this(bindingDescriptor, emission, tracer, new CougarClientConnManager());
    }


    public HttpClientExecutable(final HttpServiceBindingDescriptor bindingDescriptor, ContextEmitter emission, Tracer tracer, CougarClientConnManager clientConnectionManager) {
        super(bindingDescriptor, new HttpClientCougarRequestFactory(emission), tracer);
        this.clientConnectionManager = clientConnectionManager;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void init() throws Exception {
        super.init();

        // create client if not been set externally (e.g for testing)
        if (client == null) {
            client = new DefaultHttpClient(clientConnectionManager);
            ((DefaultHttpClient)client).setUserTokenHandler(userTokenHandler);
        }

        // configure retryhandler if set
        if (retryHandler != null) {
            ((AbstractHttpClient) client).setHttpRequestRetryHandler(retryHandler);
        }

        // configure timeout if set
        if (connectTimeout != -1) {
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, connectTimeout);
            HttpConnectionParams.setSoTimeout(params, connectTimeout);
        }

        //Configure SSL - if relevant
        if (transportSSLEnabled) {
            KeyStoreManagement keyStore = KeyStoreManagement.getKeyStoreManagement(httpsKeystoreType, httpsKeystore, httpsKeyPassword);
            if (jmxControl != null && keyStore != null) {
                jmxControl.registerMBean("CoUGAR:name=HttpClientKeyStore,beanName="+beanName, keyStore);
            }
            KeyStoreManagement trustStore = KeyStoreManagement.getKeyStoreManagement(httpsTruststoreType, httpsTruststore, httpsTrustPassword);
            if (jmxControl != null) {
                jmxControl.registerMBean("CoUGAR:name=HttpClientTrustStore,beanName="+beanName, trustStore);
            }
            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore != null ? keyStore.getKeyStore() : null, keyStore != null ? httpsKeyPassword : null, trustStore.getKeyStore());
            if (hostnameVerificationDisabled) {
                socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                LOGGER.warn("CRITICAL SECURITY CHECKS ARE DISABLED: server SSL certificate hostname " +
                        "verification is turned off.");
            }
            Scheme sch = new Scheme("https", extractPortFromAddress(), socketFactory);
            client.getConnectionManager().getSchemeRegistry().register(sch);
        }

        metrics = new HttpClientTransportMetrics();

        if (jmxControl != null) {
            jmxControl.registerMBean("CoUGAR:name=HttpClientExecutable,beanName="+beanName, this);
        }
    }


    @Override
    protected void sendRequest(HttpUriRequest httpMethod, ExecutionObserver obs,
                               OperationDefinition operationDefinition) {
        try {
            final HttpResponse response = client.execute(httpMethod);
            if (LOGGER.isDebugEnabled()) {
                final int statusCode = response.getStatusLine().getStatusCode();
                LOGGER.debug("Received http response code of " + statusCode +
                        " in reply to request to " + httpMethod.getURI());
            }
            processResponse(new CougarHttpResponse(response), obs, operationDefinition);
        } catch (Exception e) {
            processException(obs, e, httpMethod.getURI().toString());
        }


    }

    public void setRetryHandler(final HttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    public void setClient(final HttpClient client) {
        this.client = client;
    }

    public void setJmxControl(JMXControl jmxControl) {
        this.jmxControl = jmxControl;
    }

    public void setUserTokenHandler(UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
    }

    private static final class CougarHttpResponse implements AbstractHttpExecutable.CougarHttpResponse {
        private final HttpResponse delegate;

        private CougarHttpResponse(HttpResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        public InputStream getEntity() throws IOException {
            return delegate.getEntity() == null ? null : delegate.getEntity().getContent();
        }

        @Override
        public List<String> getContentEncoding() {
            List<String> codecList = new LinkedList<String>();
            Header ceheader = delegate.getEntity().getContentEncoding();
            if (ceheader != null) {
                HeaderElement[] codecs = ceheader.getElements();
                for (HeaderElement codec : codecs) {
                    codecList.add(codec.getName().toLowerCase(Locale.US));
                }
            }
            return codecList;
        }

        @Override
        public int getResponseStatus() {
            return delegate.getStatusLine().getStatusCode();
        }

        @Override
        public String getServerIdentity() {
            if (delegate.containsHeader("Server")) {
                return "" + delegate.getFirstHeader("Server").getValue();
            }
            return null;
        }

        @Override
        public long getResponseSize() {
            if (delegate.getEntity() != null) {
                return delegate.getEntity().getContentLength();
            }
            return 0;
        }
    }

    @Override
    public TransportMetrics getTransportMetrics() {
        return metrics;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        clientConnectionManager.setMaxTotal(maxTotalConnections);
    }

    public void setMaxPerRouteConnections(int maxPerRouteConnections) {
        clientConnectionManager.setDefaultMaxPerRoute(maxPerRouteConnections);
    }


    final class HttpClientTransportMetrics implements TransportMetrics {

        @Override
        public int getOpenConnections() {
            return clientConnectionManager.getConnectionsInPool();
        }

        @Override
        public int getMaximumConnections() {
            return clientConnectionManager.getDefaultMaxPerRoute();
        }

        @Override
        public int getFreeConnections() {
            return clientConnectionManager.getFreeConnections();
        }

        @Override
        public int getCurrentLoad() {
            return (getOpenConnections() - getFreeConnections()) * 100 / getMaximumConnections();
        }
    }

}
