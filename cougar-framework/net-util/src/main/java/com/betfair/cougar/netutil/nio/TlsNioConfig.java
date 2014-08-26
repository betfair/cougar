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

package com.betfair.cougar.netutil.nio;

import com.betfair.cougar.netutil.SslContextFactory;
import com.betfair.cougar.netutil.nio.monitoring.SessionWriteQueueMonitoring;
import com.betfair.cougar.util.KeyStoreManagement;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.common.support.BaseIoServiceConfig;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;

@ManagedResource
public class TlsNioConfig extends NioConfig implements BeanNameAware {

    private static Logger logger = LoggerFactory.getLogger(TlsNioConfig.class);

    private boolean supportsTls;
    private boolean requiresTls;

    private boolean needClientAuth;
    private boolean wantClientAuth;

    private Resource keystore;
    private String keystoreType;
    private String keystorePassword;
    private Resource truststore;
    private String truststoreType;
    private String truststorePassword;
    private MBeanServer mbeanServer;

    private KeyStoreManagement keystoreChains;
    private KeyStoreManagement truststoreChains;
    private String allowedCipherSuites;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    private KeyStoreManagement getKeystoreCertificateChains() throws Exception {
        if (keystoreChains == null) {
            keystoreChains = KeyStoreManagement.getKeyStoreManagement(keystoreType, keystore, keystorePassword);
            if (keystoreChains != null) {
                try {
                    mbeanServer.registerMBean(keystoreChains, new ObjectName("CoUGAR.socket.ssl:name=keyStore,nioConfig="+beanName));
                }
                catch (InstanceAlreadyExistsException iaee) {
                    logger.warn("You appear to have loaded the same TlsNioConfig more than once in the same JVM", iaee);
                }
            }
        }
        return keystoreChains;
    }

    private KeyStoreManagement getTruststoreCertificateChains() throws Exception {
        if (truststoreChains == null) {
            truststoreChains = KeyStoreManagement.getKeyStoreManagement(truststoreType, truststore, truststorePassword);
            if (truststoreChains != null) {
                try {
                    mbeanServer.registerMBean(truststoreChains, new ObjectName("CoUGAR.socket.ssl:name=trustStore,nioConfig="+beanName));
                }
                catch (InstanceAlreadyExistsException iaee) {
                    logger.warn("You appear to have loaded the same TlsNioConfig more than once in the same JVM", iaee);
                }
            }
        }
        return truststoreChains;
    }

    protected void configureProtocol(BaseIoServiceConfig config, boolean isServer) throws IOException {
        try {
            ByteBuffer.setUseDirectBuffers(isUseDirectBuffersInMina());

            SslContextFactory factory = new SslContextFactory();
            if (isServer) {
                if (supportsTls) {
                    KeyStoreManagement keystoreMgmt = getKeystoreCertificateChains();
                    if (keystoreMgmt == null) {
                        throw new IllegalStateException("This configuration ostensibly supports TLS, yet doesn't provide valid keystore configuration");
                    }
                    factory.setKeyManagerFactoryKeyStore(keystoreMgmt.getKeyStore());
                    factory.setKeyManagerFactoryKeyStorePassword(keystorePassword);
                    if (wantClientAuth) {
                        KeyStoreManagement truststoreMgmt = getTruststoreCertificateChains();
                        if (truststoreMgmt == null) {
                            throw new IllegalStateException("This configuration ostensibly supports client auth, yet doesn't provide valid truststore configuration");
                        }
                        factory.setTrustManagerFactoryKeyStore(truststoreMgmt.getKeyStore());
                    }
                }
            }
            else {
                if (supportsTls) {
                    KeyStoreManagement truststoreMgmt = getTruststoreCertificateChains();
                    if (truststoreMgmt == null) {
                        throw new IllegalStateException("This configuration ostensibly supports TLS, yet doesn't provide valid truststore configuration");
                    }
                    factory.setTrustManagerFactoryKeyStore(truststoreMgmt.getKeyStore());
                    if (wantClientAuth) {
                        KeyStoreManagement keystoreMgmt = getKeystoreCertificateChains();
                        if (keystoreMgmt == null) {
                            throw new IllegalStateException("This configuration ostensibly supports client auth, yet doesn't provide valid keystore configuration");
                        }
                        factory.setKeyManagerFactoryKeyStore(keystoreMgmt.getKeyStore());
                        factory.setKeyManagerFactoryKeyStorePassword(keystorePassword);
                    }
                }
            }
            SSLFilter sslFilter = null;
            if (supportsTls) {
                sslFilter = new SSLFilter(factory.newInstance());
                sslFilter.setWantClientAuth(wantClientAuth);
                sslFilter.setNeedClientAuth(needClientAuth);
                sslFilter.setUseClientMode(!isServer);
                String[] cipherSuites = allowedCipherSuites == null || "".equals(allowedCipherSuites.trim()) ? null : allowedCipherSuites.split(",");
                if (cipherSuites != null) {
                    sslFilter.setEnabledCipherSuites(cipherSuites);
                }
            }

            CougarProtocol protocol;
            if (isServer) {
                protocol = CougarProtocol.getServerInstance(getNioLogger(), getKeepAliveInterval(), getKeepAliveTimeout(), sslFilter, supportsTls, requiresTls);
            }
            else {
                protocol = CougarProtocol.getClientInstance(getNioLogger(), getKeepAliveInterval(), getKeepAliveTimeout(), sslFilter, supportsTls, requiresTls, getRpcTimeoutMillis());
            }

            config.getFilterChain().addLast("slowHandling", new SessionWriteQueueMonitoring(getNioLogger(), getMaxWriteQueueSize()));
            config.getFilterChain().addLast("codec",
                    new ProtocolCodecFilter(new CougarProtocolEncoder(getNioLogger()), new CougarProtocolDecoder(getNioLogger())));
            config.getFilterChain().addLast("protocol", protocol);

            config.setThreadModel(ThreadModel.MANUAL);
        }
        catch (Exception e) {
            throw new IOException("Unable to initialise MINA", e);
        }
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    @ManagedAttribute
    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

    @ManagedAttribute
    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    public void setKeystore(Resource keystore) {
        this.keystore = keystore;
    }

    @ManagedAttribute
    public Resource getKeystore() {
        return keystore;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    @ManagedAttribute
    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @ManagedAttribute
    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setTruststore(Resource truststore) {
        this.truststore = truststore;
    }

    @ManagedAttribute
    public Resource getTruststore() {
        return truststore;
    }

    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    @ManagedAttribute
    public String getTruststoreType() {
        return truststoreType;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    @ManagedAttribute
    public String getTruststorePassword() {
        return truststorePassword;
    }

    @ManagedAttribute
    public boolean isSupportsTls() {
        return supportsTls;
    }

    public void setSupportsTls(boolean supportsTls) {
        this.supportsTls = supportsTls;
    }

    @ManagedAttribute
    public boolean isRequiresTls() {
        return requiresTls;
    }

    public void setRequiresTls(boolean requiresTls) {
        this.requiresTls = requiresTls;
    }

    public void setMbeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    @ManagedAttribute
    public String getAllowedCipherSuites() {
        return allowedCipherSuites;
    }

    public void setAllowedCipherSuites(String allowedCipherSuites) {
        this.allowedCipherSuites = allowedCipherSuites;
    }
}
