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

import com.betfair.cougar.netutil.nio.monitoring.SessionWriteQueueMonitoring;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.support.BaseIoConnectorConfig;
import org.apache.mina.common.support.BaseIoServiceConfig;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.FileSystemResource;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class TlsNioConfigTest {

    BaseIoServiceConfig minaConfig;
    DefaultIoFilterChainBuilder filterChainBuilder;
    NioLogger logger;
    MBeanServer mbeanServer;

    @Before
    public void before() {
        minaConfig = mock(BaseIoServiceConfig.class);
        filterChainBuilder = mock(DefaultIoFilterChainBuilder.class);
        when(minaConfig.getFilterChain()).thenReturn(filterChainBuilder);
        mbeanServer = mock(MBeanServer.class);

        logger = new NioLogger("ALL");
    }

    private List<Tuple<String, IoFilter>> getAddedFilters() {
        List<Tuple<String, IoFilter>> ret = new ArrayList<Tuple<String, IoFilter>>();

        ArgumentCaptor<String> filterName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<IoFilter> filter = ArgumentCaptor.forClass(IoFilter.class);
        verify(filterChainBuilder, atLeast(0)).addLast(filterName.capture(), filter.capture());

        List<String> names = filterName.getAllValues();
        List<IoFilter> filters = filter.getAllValues();
        for (int i=0; i<names.size(); i++) {
            ret.add(new Tuple<String, IoFilter>(names.get(i), filters.get(i)));
        }

        return ret;
    }

    private void assertInstanceOf(Class c, Object toCheck) {
        assertTrue(toCheck.getClass().getName() + " is not an instanceof " + c.getName(), toCheck.getClass().isAssignableFrom(c));
    }

    private static String getPath(String filename) throws IOException {
        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        if (userDir.endsWith("/cougar-framework/net-util")) {
            userDir = userDir.substring(0, userDir.indexOf("/cougar-framework/net-util"));
        }
        if (userDir.endsWith("\\cougar-framework\\net-util")) {
            userDir = userDir.substring(0, userDir.indexOf("\\cougar-framework\\net-util"));
        }

        return new File(userDir, "cougar-framework/net-util/src/test/resources/"+filename).getCanonicalPath();
    }

    public static String getClientKeystorePath() throws IOException {
        return getPath("cougar_client_cert.jks");
    }

    public static String getClientTruststorePath() throws IOException {
        return getPath("cougar_server_ca.jks");
    }

    public static String getServerKeystorePath() throws IOException {
        return getPath("cougar_server_cert.jks");
    }

    public static String getServerTruststorePath() throws IOException {
        return getPath("cougar_client_ca.jks");
    }

    @Test
    public void insecureClient() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());
        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertNull(cp.getSslFilter());
    }

    @Test
    public void secureClientSupportsTlsNoClientAuthTruststoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getClientTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setWantClientAuth(false);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertFalse(sslFilter.isWantClientAuth());
        assertTrue(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test
    public void secureClientSupportsTlsNoClientAuthTruststoreProvidedSpecifiedCiphers() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getClientTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setWantClientAuth(false);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.setAllowedCipherSuites("DES,AES");
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertFalse(sslFilter.isWantClientAuth());
        assertTrue(sslFilter.isUseClientMode());
        assertNotNull(sslFilter.getEnabledCipherSuites());
        assertEquals(2, sslFilter.getEnabledCipherSuites().length);
    }

    @Test(expected = IllegalStateException.class)
    public void secureClientSupportsTlsNoClientAuthTruststoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setWantClientAuth(false);
            config.setNeedClientAuth(false);
            config.setRequiresTls(false);
            config.setSupportsTls(true);
            config.setTruststoreType("JKS");
            config.configureProtocol(minaConfig, false);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void secureClientSupportsTlsWantsClientAuthTruststoreProvidedKeystoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getClientTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setKeystore(new FileSystemResource(getClientKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(true);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertTrue(sslFilter.isWantClientAuth());
        assertTrue(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test(expected = IllegalStateException.class)
    public void secureClientSupportsTlsWantsClientAuthTruststoreProvidedKeystoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setTruststore(new FileSystemResource(getClientTruststorePath()));
            config.setTruststorePassword("password");
            config.setTruststoreType("JKS");
            config.setKeystoreType("JKS");
            config.setWantClientAuth(true);
            config.setNeedClientAuth(false);
            config.setRequiresTls(false);
            config.setSupportsTls(true);
            config.setTruststoreType("JKS");
            config.configureProtocol(minaConfig, false);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void secureClientSupportsTlsNeedsClientAuthTruststoreProvidedKeystoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getClientTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setKeystore(new FileSystemResource(getClientKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(true);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertTrue(sslFilter.isWantClientAuth());
        assertTrue(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test(expected = IllegalStateException.class)
    public void secureClientSupportsTlsNeedsClientAuthTruststoreProvidedKeystoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setTruststore(new FileSystemResource(getClientTruststorePath()));
            config.setTruststorePassword("password");
            config.setTruststoreType("JKS");
            config.setKeystoreType("JKS");
            config.setWantClientAuth(true);
            config.setNeedClientAuth(false);
            config.setRequiresTls(false);
            config.setSupportsTls(true);
            config.setTruststoreType("JKS");
            config.configureProtocol(minaConfig, false);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void secureClientRequiresTlsNoClientAuthTruststoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getClientTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setWantClientAuth(false);
        config.setNeedClientAuth(false);
        config.setRequiresTls(true);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertTrue(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertFalse(sslFilter.isWantClientAuth());
        assertTrue(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test(expected = IllegalStateException.class)
    public void secureClientRequiresTlsNoClientAuthTruststoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setWantClientAuth(false);
            config.setNeedClientAuth(false);
            config.setRequiresTls(true);
            config.setSupportsTls(true);
            config.setTruststoreType("JKS");
            config.configureProtocol(minaConfig, false);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void insecureServer() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.configureProtocol(minaConfig, false);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());
        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertNull(cp.getSslFilter());
    }

    @Test
    public void secureServerSupportsTlsNoClientAuthKeystoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setKeystore(new FileSystemResource(getServerKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(false);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, true);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertFalse(sslFilter.isWantClientAuth());
        assertFalse(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test
    public void secureServerSupportsTlsNoClientAuthKeystoreProvidedSpecifiedCiphers() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setKeystore(new FileSystemResource(getServerKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(false);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.setAllowedCipherSuites("DES,AES");
        config.configureProtocol(minaConfig, true);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertFalse(sslFilter.isWantClientAuth());
        assertFalse(sslFilter.isUseClientMode());
        assertNotNull(sslFilter.getEnabledCipherSuites());
        assertEquals(2, sslFilter.getEnabledCipherSuites().length);
    }

    @Test(expected = IllegalStateException.class)
    public void secureServerSupportsTlsNoClientAuthKeystoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setWantClientAuth(false);
            config.setNeedClientAuth(false);
            config.setRequiresTls(false);
            config.setSupportsTls(true);
            config.setKeystoreType("JKS");
            config.configureProtocol(minaConfig, true);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void secureServerSupportsTlsWantsClientAuthKeystoreProvidedTruststoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getServerTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setKeystore(new FileSystemResource(getServerKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(true);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, true);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertTrue(sslFilter.isWantClientAuth());
        assertFalse(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test(expected = IllegalStateException.class)
    public void secureServerSupportsTlsWantsClientAuthKeystoreProvidedTruststoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setTruststoreType("JKS");
            config.setKeystore(new FileSystemResource(getServerKeystorePath()));
            config.setKeystorePassword("password");
            config.setKeystoreType("JKS");
            config.setWantClientAuth(true);
            config.setNeedClientAuth(false);
            config.setRequiresTls(false);
            config.setSupportsTls(true);
            config.setTruststoreType("JKS");
            config.configureProtocol(minaConfig, true);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void secureServerSupportsTlsNeedsClientAuthKeystoreProvidedTruststoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setTruststore(new FileSystemResource(getServerTruststorePath()));
        config.setTruststorePassword("password");
        config.setTruststoreType("JKS");
        config.setKeystore(new FileSystemResource(getServerKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(true);
        config.setNeedClientAuth(false);
        config.setRequiresTls(false);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, true);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertFalse(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertTrue(sslFilter.isWantClientAuth());
        assertFalse(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test(expected = IllegalStateException.class)
    public void secureServerSupportsTlsNeedsClientAuthKeystoreProvidedTruststoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setTruststoreType("JKS");
            config.setKeystore(new FileSystemResource(getServerKeystorePath()));
            config.setKeystorePassword("password");
            config.setKeystoreType("JKS");
            config.setWantClientAuth(true);
            config.setNeedClientAuth(false);
            config.setRequiresTls(false);
            config.setSupportsTls(true);
            config.setTruststoreType("JKS");
            config.configureProtocol(minaConfig, true);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    @Test
    public void secureServerRequiresTlsNoClientAuthKeystoreProvided() throws IOException {
        TlsNioConfig config = new TlsNioConfig();
        config.setNioLogger(logger);
        config.setMbeanServer(mbeanServer);
        config.setKeystore(new FileSystemResource(getServerKeystorePath()));
        config.setKeystorePassword("password");
        config.setKeystoreType("JKS");
        config.setWantClientAuth(false);
        config.setNeedClientAuth(false);
        config.setRequiresTls(true);
        config.setSupportsTls(true);
        config.configureProtocol(minaConfig, true);

        List<Tuple<String, IoFilter>> addedFilters = getAddedFilters();
        assertEquals("slowHandling", addedFilters.get(0).getFirst());
        assertInstanceOf(SessionWriteQueueMonitoring.class, addedFilters.get(0).getSecond());
        assertEquals("codec", addedFilters.get(1).getFirst());
        assertInstanceOf(ProtocolCodecFilter.class, addedFilters.get(1).getSecond());
        assertEquals("protocol", addedFilters.get(2).getFirst());
        assertInstanceOf(CougarProtocol.class, addedFilters.get(2).getSecond());

        CougarProtocol cp = (CougarProtocol) addedFilters.get(2).getSecond();
        assertTrue(cp.isRequiresTls());
        assertTrue(cp.isSupportsTls());
        assertNotNull(cp.getSslFilter());
        SSLFilter sslFilter = cp.getSslFilter();
        assertFalse(sslFilter.isNeedClientAuth());
        assertFalse(sslFilter.isWantClientAuth());
        assertFalse(sslFilter.isUseClientMode());
        assertNull(sslFilter.getEnabledCipherSuites());
    }

    @Test(expected = IllegalStateException.class)
    public void secureServerRequiresTlsNoClientAuthKeystoreNotProvided() throws Throwable {
        try {
            TlsNioConfig config = new TlsNioConfig();
            config.setNioLogger(logger);
            config.setMbeanServer(mbeanServer);
            config.setWantClientAuth(false);
            config.setNeedClientAuth(false);
            config.setRequiresTls(true);
            config.setSupportsTls(true);
            config.setKeystoreType("JKS");
            config.configureProtocol(minaConfig, true);
            fail("Expected an exception");
        }
        catch (IOException ioe) {
            throw ioe.getCause();
        }
    }

    private class Tuple<A, B> {
        private A first;
        private B second;

        private Tuple(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple tuple = (Tuple) o;

            if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
            if (second != null ? !second.equals(tuple.second) : tuple.second != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }
    }
}
