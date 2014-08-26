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

package com.betfair.cougar.transport.jetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.KeyStoreManagement;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ConnectorStatistics;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.core.io.Resource;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class encapsulate configuration and lifecycle of a Jetty server and its connectors
 */
public class JettyServerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServerWrapper.class);

    private Server jettyServer;

    private MBeanServer mbeanServer;

    private QueuedThreadPool threadPool;

    private int httpPort;
    private boolean httpReuseAddress;
    private int httpMaxIdle;
    private int httpAcceptors;
    private int httpAcceptQueueSize;
    private int httpSelectors;

    private boolean httpsAllowRenegotiate;

    private boolean httpForwarded;
    private boolean httpsForwarded;

    private int requestHeaderSize;
    private int responseHeaderSize;
    private int responseBufferSize;

    private int httpsPort;
    private boolean httpsReuseAddress;
    private int httpsMaxIdle;
    private int httpsAcceptors;
    private int httpsAcceptQueueSize;
    private int httpsSelectors;

    private boolean httpsWantClientAuth;
    private boolean httpsNeedClientAuth;
    private Resource httpsKeystore;
    private String httpsKeystoreType;
    private String httpsKeyPassword;
    private String httpsCertAlias;
    private Resource httpsTruststore;
    private String httpsTruststoreType;
    private String httpsTrustPassword;

    private int minThreads;
    private int maxThreads;

    private ServerConnector httpConnector;
    private HttpConfiguration httpConfiguration;
    private ServerConnector httpsConnector;
    private HttpConfiguration httpsConfiguration;

    private int maxFormContentSize;

    private int lowResourcesIdleTime;
    private int lowResourcesMaxTime;
    private int lowResourcesPeriod;
    private boolean lowResourcesMonitorThreads;
    private int lowResourcesMaxConnections;
    private long lowResourcesMaxMemory;

    public void initialiseConnectors() throws Exception {
        threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxThreads);
        threadPool.setMinThreads(minThreads);
        threadPool.setName("JettyThread");
        jettyServer = new Server(threadPool);

        jettyServer.setStopAtShutdown(true);

        MBeanContainer container = new MBeanContainer(mbeanServer);
        jettyServer.addBean(container);

        LowResourceMonitor lowResourcesMonitor = new LowResourceMonitor(jettyServer);
        lowResourcesMonitor.setPeriod(lowResourcesPeriod);
        lowResourcesMonitor.setLowResourcesIdleTimeout(lowResourcesIdleTime);
        lowResourcesMonitor.setMonitorThreads(lowResourcesMonitorThreads);
        lowResourcesMonitor.setMaxConnections(lowResourcesMaxConnections);
        lowResourcesMonitor.setMaxMemory(lowResourcesMaxMemory);
        lowResourcesMonitor.setMaxLowResourcesTime(lowResourcesMaxTime);
        jettyServer.addBean(lowResourcesMonitor);

        // US24803 - Needed for preventing Hashtable key collision DoS CVE-2012-2739
        jettyServer.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxFormContentSize);

        List<Connector> connectors = new ArrayList<Connector>();

        if (httpPort != -1) {
            httpConfiguration = createHttpConfiguration();
            setBufferSizes(httpConfiguration);
            if (httpForwarded) {
                httpConfiguration.addCustomizer(new ForwardedRequestCustomizer());
            }
            httpConnector = createHttpConnector(jettyServer, httpConfiguration, httpAcceptors, httpSelectors);
            httpConnector.setPort(httpPort);
            httpConnector.setReuseAddress(httpReuseAddress);
            httpConnector.setIdleTimeout(httpMaxIdle);
            httpConnector.setAcceptQueueSize(httpAcceptQueueSize);
            httpConnector.addBean(new ConnectorStatistics());

            connectors.add(httpConnector);
        }

        if (httpsPort != -1) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(httpsKeystore.getFile().getCanonicalPath());
            sslContextFactory.setKeyStoreType(httpsKeystoreType);
            sslContextFactory.setKeyStorePassword(httpsKeyPassword);
            if (StringUtils.isNotBlank(httpsCertAlias)) {
                sslContextFactory.setCertAlias(httpsCertAlias);
            }
            sslContextFactory.setKeyManagerPassword(httpsKeyPassword);
            // if you need it then you defo want it
            sslContextFactory.setWantClientAuth(httpsNeedClientAuth || httpsWantClientAuth);
            sslContextFactory.setNeedClientAuth(httpsNeedClientAuth);
            sslContextFactory.setRenegotiationAllowed(httpsAllowRenegotiate);

            httpsConfiguration = createHttpConfiguration();
            setBufferSizes(httpsConfiguration);
            if (httpsForwarded) {
                httpsConfiguration.addCustomizer(new ForwardedRequestCustomizer());
            }

            httpsConnector = createHttpsConnector(jettyServer, httpsConfiguration, httpsAcceptors, httpsSelectors, sslContextFactory);
            httpsConnector.setPort(httpsPort);
            httpsConnector.setReuseAddress(httpsReuseAddress);
            httpsConnector.setIdleTimeout(httpsMaxIdle);
            httpsConnector.setAcceptQueueSize(httpsAcceptQueueSize);
            httpsConnector.addBean(new ConnectorStatistics());

            mbeanServer.registerMBean(getKeystoreCertificateChains(), new ObjectName("CoUGAR.https:name=keyStore"));
            // truststore is not required if we don't want client auth
            if (httpsWantClientAuth) {
                sslContextFactory.setTrustStorePath(httpsTruststore.getFile().getCanonicalPath());
                sslContextFactory.setTrustStoreType(httpsTruststoreType);
                sslContextFactory.setTrustStorePassword(httpsTrustPassword);
                mbeanServer.registerMBean(getTruststoreCertificateChains(), new ObjectName("CoUGAR.https:name=trustStore"));
            }
            connectors.add(httpsConnector);
        }

        if (connectors.size() == 0) {
            throw new IllegalStateException("HTTP transport requires at least one port enabled to function correctly.");
        }

        jettyServer.setConnectors(connectors.toArray(new Connector[connectors.size()]));
    }

    public void start() throws Exception {
        jettyServer.start();
    }

    public void startThreadPool() throws Exception {
        threadPool.start();
    }

    public void stop() throws Exception {
        jettyServer.stop();
    }

    public boolean isRunning() {
        if (jettyServer == null) {
            return false;
        }
        return jettyServer.isRunning();
    }

    private HttpConfiguration createHttpConfiguration() {
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSendServerVersion(true);
        httpConfiguration.setSendDateHeader(true);
        return httpConfiguration;
    }

    /**
     * Sets the request and header buffer sizex if they are not zero
     */
    private void setBufferSizes(HttpConfiguration buffers) {
        if (requestHeaderSize > 0) {
            LOGGER.info("Request header size set to {} for {}", requestHeaderSize, buffers.getClass().getCanonicalName());
            buffers.setRequestHeaderSize(requestHeaderSize);
        }

        if (responseBufferSize > 0) {
            LOGGER.info("Response buffer size set to {} for {}", responseBufferSize, buffers.getClass().getCanonicalName());
            buffers.setOutputBufferSize(responseBufferSize);
        }
        if (responseHeaderSize > 0) {
            LOGGER.info("Response header size set to {} for {}", responseHeaderSize, buffers.getClass().getCanonicalName());
            buffers.setResponseHeaderSize(responseHeaderSize);
        }
    }

    protected ServerConnector createHttpConnector(Server server, HttpConfiguration httpConfiguration, int httpAcceptors, int httpSelectors) {
        return new ServerConnector(server, null, null, null, httpAcceptors, httpSelectors, new HttpConnectionFactory(httpConfiguration));
    }

    protected ServerConnector createHttpsConnector(Server server, HttpConfiguration httpConfiguration, int httpsAcceptors, int httpsSelectors, SslContextFactory sslContextFactory) {
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());
        return new ServerConnector(server, null, null, null, httpsAcceptors, httpsSelectors, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpConfiguration));
    }

    private KeyStoreManagement getKeystoreCertificateChains() throws Exception {
        return KeyStoreManagement.getKeyStoreManagement(httpsKeystoreType, httpsKeystore, httpsKeyPassword);
    }

    private KeyStoreManagement getTruststoreCertificateChains() throws Exception {
        return KeyStoreManagement.getKeyStoreManagement(httpsTruststoreType, httpsTruststore, httpsTrustPassword);
    }

    public boolean addBean(Object o) {
        return jettyServer.addBean(o);
    }

    public void setHandler(Handler handler) {
        jettyServer.setHandler(handler);
    }

    public boolean isHttpEnabled() {
        return httpPort != -1;
    }

    public boolean isHttpsEnabled() {
        return httpsPort != -1;
    }

    public int getRequestHeaderSize() {
        return requestHeaderSize;
    }

    public int getResponseBufferSize() {
        return responseBufferSize;
    }

    public int getResponseHeaderSize() {
        return responseHeaderSize;
    }

    public int getHttpAcceptors() {
        return httpAcceptors;
    }

    public int getHttpsAcceptors() {
        return httpsAcceptors;
    }

    public boolean isHttpForwarded() {
        return httpForwarded;
    }

    public void setMbeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpReuseAddress(boolean httpReuseAddress) {
        this.httpReuseAddress = httpReuseAddress;
    }

    public void setHttpMaxIdle(int httpMaxIdle) {
        this.httpMaxIdle = httpMaxIdle;
    }

    public void setHttpAcceptors(int httpAcceptors) {
        this.httpAcceptors = httpAcceptors;
    }

    public void setHttpSelectors(int httpSelectors) {
        this.httpSelectors = httpSelectors;
    }

    public int getHttpSelectors() {
        return httpSelectors;
    }

    public void setHttpForwarded(boolean httpForwarded) {
        this.httpForwarded = httpForwarded;
    }

    public boolean isHttpsForwarded() {
        return httpsForwarded;
    }

    public void setRequestHeaderSize(int requestHeaderSize) {
        this.requestHeaderSize = requestHeaderSize;
    }

    public void setResponseHeaderSize(int responseHeaderSize) {
        this.responseHeaderSize = responseHeaderSize;
    }

    public void setResponseBufferSize(int responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsReuseAddress(boolean httpsReuseAddress) {
        this.httpsReuseAddress = httpsReuseAddress;
    }

    public void setHttpsMaxIdle(int httpsMaxIdle) {
        this.httpsMaxIdle = httpsMaxIdle;
    }

    public void setHttpsAcceptors(int httpsAcceptors) {
        this.httpsAcceptors = httpsAcceptors;
    }

    public void setHttpsSelectors(int httpsSelectors) {
        this.httpsSelectors = httpsSelectors;
    }

    public int getHttpsSelectors() {
        return httpsSelectors;
    }

    public void setHttpsWantClientAuth(boolean httpsWantClientAuth) {
        this.httpsWantClientAuth = httpsWantClientAuth;
    }

    public void setHttpsNeedClientAuth(boolean httpsNeedClientAuth) {
        this.httpsNeedClientAuth = httpsNeedClientAuth;
    }

    public void setHttpsKeystore(Resource httpsKeystore) {
        this.httpsKeystore = httpsKeystore;
    }

    public void setHttpsKeystoreType(String httpsKeystoreType) {
        this.httpsKeystoreType = httpsKeystoreType;
    }

    public void setHttpsKeyPassword(String httpsKeyPassword) {
        this.httpsKeyPassword = httpsKeyPassword;
    }

    public void setHttpsTruststore(Resource httpsTruststore) {
        this.httpsTruststore = httpsTruststore;
    }

    public void setHttpsTruststoreType(String httpsTruststoreType) {
        this.httpsTruststoreType = httpsTruststoreType;
    }

    public void setHttpsTrustPassword(String httpsTrustPassword) {
        this.httpsTrustPassword = httpsTrustPassword;
    }

    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public boolean isHttpsAllowRenegotiate() {
        return httpsAllowRenegotiate;
    }

    public void setHttpsAllowRenegotiate(boolean httpsAllowRenegotiate) {
        this.httpsAllowRenegotiate = httpsAllowRenegotiate;
    }

    public Server getJettyServer() {
        return jettyServer;
    }

    /* Some testing helpers */
    public boolean isHttpConnectorCreated() {
        return httpConnector != null;
    }

    public boolean isHttpsConnectorCreated() {
        return httpsConnector != null;
    }

    public void setThreadPoolName(String s) {
        threadPool.setName(s);
    }

    public int getMaxFormContentSize() {
        return maxFormContentSize;
    }

    /**
     * Used to set the Jetty Server attribute "org.eclipse.jetty.server.Request.maxFormContentSize".
     * @param maxFormContentSize
     */
    public void setMaxFormContentSize(int maxFormContentSize) {
        this.maxFormContentSize = maxFormContentSize;
    }

    public void setHttpsForwarded(boolean httpsForwarded) {
        this.httpsForwarded = httpsForwarded;
    }

    public void setLowResourcesIdleTime(int lowResourcesIdleTime) {
        this.lowResourcesIdleTime = lowResourcesIdleTime;
    }

    public void setLowResourcesMaxTime(int lowResourcesMaxTime) {
        this.lowResourcesMaxTime = lowResourcesMaxTime;
    }

    public void setLowResourcesPeriod(int lowResourcesPeriod) {
        this.lowResourcesPeriod = lowResourcesPeriod;
    }

    public void setLowResourcesMaxConnections(int lowResourcesMaxConnections) {
        this.lowResourcesMaxConnections = lowResourcesMaxConnections;
    }

    public void setLowResourcesMaxMemory(long lowResourcesMaxMemory) {
        this.lowResourcesMaxMemory = lowResourcesMaxMemory;
    }

    public int getHttpAcceptQueueSize() {
        return httpAcceptQueueSize;
    }

    public void setHttpAcceptQueueSize(int httpAcceptQueueSize) {
        this.httpAcceptQueueSize = httpAcceptQueueSize;
    }

    public int getHttpsAcceptQueueSize() {
        return httpsAcceptQueueSize;
    }

    public void setHttpsAcceptQueueSize(int httpsAcceptQueueSize) {
        this.httpsAcceptQueueSize = httpsAcceptQueueSize;
    }

    public void setHttpsCertAlias(String httpsCertAlias) {
        this.httpsCertAlias = httpsCertAlias;
    }

    public long getLowResourcesMaxMemory() {
        return lowResourcesMaxMemory;
    }

    public int getLowResourcesMaxConnections() {
        return lowResourcesMaxConnections;
    }

    public int getLowResourcesPeriod() {
        return lowResourcesPeriod;
    }

    public int getLowResourcesMaxTime() {
        return lowResourcesMaxTime;
    }

    public int getLowResourcesIdleTime() {
        return lowResourcesIdleTime;
    }

    public boolean isLowResourcesMonitorThreads() {
        return lowResourcesMonitorThreads;
    }

    public void setLowResourcesMonitorThreads(boolean lowResourcesMonitorThreads) {
        this.lowResourcesMonitorThreads = lowResourcesMonitorThreads;
    }
}
