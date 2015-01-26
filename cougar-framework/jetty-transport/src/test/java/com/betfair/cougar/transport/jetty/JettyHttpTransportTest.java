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

package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.impl.transports.TransportRegistryImpl;
import com.betfair.cougar.transport.api.TransportCommandProcessorFactory;
import com.betfair.cougar.transport.api.protocol.ProtocolBinding;
import com.betfair.cougar.transport.api.protocol.ProtocolBindingRegistry;
import com.betfair.cougar.transport.api.protocol.http.HttpCommandProcessor;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.jsonrpc.JsonRpcOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor;
import com.betfair.cougar.transport.impl.protocol.http.DefaultGeoLocationDeserializer;
import com.betfair.cougar.transport.jetty.jmx.JettyEndpoints;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StopWatch;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JettyHttpTransportTest {

    private static final String OPERATION_URI = "/myOperationUri";

    private static final String RESCRIPT_SERVICE_CONTEXT_PATH = "/RESCRIPT_SERVICE_CONTEXT_PATH/";
    private static final String JSONRPC_SERVICE_CONTEXT_PATH = "/JSON_RPC_SERVICE_CONTEXT_PATH";
    public static final String SEP = System.getProperty("file.separator");

    private MBeanServer mbeanServer;

    private HttpServiceBindingDescriptor rescriptServiceBindingDescriptor;
    private HttpServiceBindingDescriptor jsonRpcBindingDescriptor;
    private HttpCommandProcessor commandProc;
    private TransportCommandProcessorFactory<HttpCommandProcessor> factory;

    private static final int TEST_HTTP_PORT = 19478;

    @BeforeClass
    public static void logQuash() {
        //Jetty uses log4j internally, so we need to shut that up
        org.apache.log4j.Logger.getRootLogger().addAppender(new org.apache.log4j.varia.NullAppender());
    }

    private JettyHttpTransport populateTransport() throws Exception {
        JettyHttpTransport transport = new JettyHttpTransport();
        populateTransport(transport);
        return transport;
    }

    @Before
    public void init() {
        rescriptServiceBindingDescriptor = Mockito.mock(HttpServiceBindingDescriptor.class);
        when(rescriptServiceBindingDescriptor.getServiceContextPath()).thenReturn(RESCRIPT_SERVICE_CONTEXT_PATH);
        when(rescriptServiceBindingDescriptor.getServiceProtocol()).thenReturn(Protocol.RESCRIPT);
        when(rescriptServiceBindingDescriptor.getServiceVersion()).thenReturn(new ServiceVersion("v3.0"));

        when(rescriptServiceBindingDescriptor.getOperationBindings()).thenReturn(new OperationBindingDescriptor[]{
                new RescriptOperationBindingDescriptor(null, OPERATION_URI, null, Collections.<RescriptParamBindingDescriptor>emptyList())
        });

        jsonRpcBindingDescriptor = Mockito.mock(HttpServiceBindingDescriptor.class);
        when(jsonRpcBindingDescriptor.getServiceContextPath()).thenReturn(JSONRPC_SERVICE_CONTEXT_PATH);
        when(jsonRpcBindingDescriptor.getServiceProtocol()).thenReturn(Protocol.JSON_RPC);
        when(jsonRpcBindingDescriptor.getServiceVersion()).thenReturn(new ServiceVersion("v3.0"));
        when(jsonRpcBindingDescriptor.getOperationBindings()).thenReturn(new OperationBindingDescriptor[]{
                new JsonRpcOperationBindingDescriptor(null)
        });

        commandProc = Mockito.mock(HttpCommandProcessor.class);
        factory = Mockito.mock(TransportCommandProcessorFactory.class);
        when(factory.getCommandProcessor(Protocol.RESCRIPT)).thenReturn(commandProc);
        when(factory.getCommandProcessor(Protocol.JSON_RPC)).thenReturn(commandProc);
    }

    private void populateTransport(JettyHttpTransport transport) throws Exception {
        mbeanServer = mock(MBeanServer.class);
        // mocking this method because otherwise Jetty gets it's knickers in a twist (and I'm not even trying to test
        // this, hence why i don't care how often this is called, and hence why i'm not checking other calls to mbeanServer)
        ObjectInstance mockedInstance = mock(ObjectInstance.class);
        when(mbeanServer.registerMBean(any(Object.class), any(ObjectName.class))).thenReturn(mockedInstance);
        transport.getServerWrapper().setMbeanServer(mbeanServer);

        transport.setTransportRegistry(new TransportRegistryImpl());

        transport.setHtmlContextPath("/wsdl");
        transport.setHtmlRegex("/wsdl/[^/]+\\\\.wsdl");
        transport.setHtmlMediaType("text/xml");

        transport.setWsdlContextPath("/static-html");
        transport.setWsdlRegex("/static-html/.+\\\\.html");
        transport.setWsdlMediaType("text/html");

        transport.setGeoLocationDeserializer(new DefaultGeoLocationDeserializer());
        transport.setUuidHeader("X-UUid");

        transport.getServerWrapper().setMinThreads(5);
        // jetty now frustratingly checks we have enough max threads on startup
        transport.getServerWrapper().setMaxThreads(33);

        // default is 9001, but going to use something odd so we don't clash if people have something running
        transport.getServerWrapper().setHttpPort(TEST_HTTP_PORT);
        transport.getServerWrapper().setHttpReuseAddress(false);
        transport.getServerWrapper().setHttpMaxIdle(30000);

        transport.getServerWrapper().setHttpsPort(-1);
        transport.getServerWrapper().setHttpsReuseAddress(false);
        transport.getServerWrapper().setHttpsMaxIdle(30000);
        transport.getServerWrapper().setHttpsWantClientAuth(false);
        transport.getServerWrapper().setHttpsNeedClientAuth(false);
        transport.getServerWrapper().setHttpsKeystore(new FileSystemResource("MUST_BE_OVERRIDDEN"));
        transport.getServerWrapper().setHttpsKeystoreType("MUST_BE_OVERRIDDEN");
        transport.getServerWrapper().setHttpsKeyPassword("MUST_BE_OVERRIDDEN");
        transport.getServerWrapper().setHttpsTruststore(new FileSystemResource("MUST_BE_OVERRIDDEN"));
        transport.getServerWrapper().setHttpsTruststoreType("MUST_BE_OVERRIDDEN");
        transport.getServerWrapper().setHttpsTrustPassword("MUST_BE_OVERRIDDEN");
    }

    private void populateTransportWithCORSEnabled(JettyHttpTransport transport) throws Exception {
        populateTransport(transport);
        transport.setCorsEnabled(true);
    }

    @Test
    public void testNotify() {
        //We need to test that for a particular service we end up with
        //1. An appropriately populated handlerSpecificationMap
        //2. the appropriate command processor is informed of the serviceDefinition

        ProtocolBinding pb = new ProtocolBinding(null, null, Protocol.RESCRIPT);
        Set<ProtocolBinding> bindingSet = new HashSet<ProtocolBinding>();
        bindingSet.add(pb);
        ProtocolBindingRegistry bindingReg = Mockito.mock(ProtocolBindingRegistry.class);
        when(bindingReg.getProtocolBindings()).thenReturn(bindingSet);


        JettyHttpTransport transport = new JettyHttpTransport();
        transport.setProtocolBindingRegistry(bindingReg);
        transport.setCommandProcessorFactory(factory);

        assertTrue("Should be no entry in the handlerSpecMap", transport.getHandlerSpecificationMap().isEmpty());
        transport.registerHandler(rescriptServiceBindingDescriptor);
        assertTrue("should be one entry in the handlerSpecMap", transport.getHandlerSpecificationMap().size() == 1);

        //verify that the commandproc was notified with the appropriate binding descriptor
        verify(commandProc).bind(eq(rescriptServiceBindingDescriptor));
    }

    @Test
    public void testNotifyWithIdentityTokenResolvers() {
        IdentityTokenResolver resolver = Mockito.mock(IdentityTokenResolver.class);

        ProtocolBinding pb = new ProtocolBinding(null, resolver, Protocol.RESCRIPT);
        Set<ProtocolBinding> bindingSet = new HashSet<ProtocolBinding>();
        bindingSet.add(pb);
        ProtocolBindingRegistry bindingReg = Mockito.mock(ProtocolBindingRegistry.class);
        when(bindingReg.getProtocolBindings()).thenReturn(bindingSet);


        JettyHttpTransport transport = new JettyHttpTransport();
        transport.setProtocolBindingRegistry(bindingReg);
        transport.setCommandProcessorFactory(factory);

        transport.registerHandler(rescriptServiceBindingDescriptor);
        JettyHandlerSpecification spec = transport.getHandlerSpecificationMap().values().iterator().next();
        assertNotNull("Jetty handler spec should not be null", spec);
        assertTrue("There should be one identityTokenResolver plugged in here", spec.getVersionToIdentityTokenResolverMap().size() == 1);
    }

    @Test
    public void testCORSEnabledAddNewHandlerToContextHandlerCollection() throws Exception {
        ProtocolBinding pb = new ProtocolBinding("/", null, Protocol.RESCRIPT);
        ProtocolBinding pb2 = new ProtocolBinding("/api", null, Protocol.RESCRIPT);
        ProtocolBinding pb3 = new ProtocolBinding("/", null, Protocol.JSON_RPC);
        Set<ProtocolBinding> bindingSet = new HashSet<ProtocolBinding>();
        bindingSet.add(pb);
        bindingSet.add(pb2);
        bindingSet.add(pb3);
        ProtocolBindingRegistry bindingReg = Mockito.mock(ProtocolBindingRegistry.class);
        when(bindingReg.getProtocolBindings()).thenReturn(bindingSet);


        JettyHttpTransport transport = new JettyHttpTransport();
        populateTransport(transport);
        transport.setProtocolBindingRegistry(bindingReg);
        transport.setCommandProcessorFactory(factory);

        JettyEndpoints endpoints = Mockito.mock(JettyEndpoints.class);
        transport.setJettyEndPoints(endpoints);

        transport.notify(rescriptServiceBindingDescriptor);
        transport.notify(jsonRpcBindingDescriptor);

        transport.initialiseStaticJettyConfig();
        populateTransportWithCORSEnabled(transport);

        transport.onCougarStart();

        assertEquals(3, transport.getHandlerCollection().getChildHandlersByClass(CrossOriginHandler.class).length);

        // Instruct Jetty not to wait for Handlers to terminate
        transport.getServerWrapper().getJettyServer().setStopTimeout(0);
        transport.stop();
    }

    @Test
    public void testCORSDisabledDoesNotAddHandlersToContextHandlerCollection() throws Exception {
        ProtocolBinding pb = new ProtocolBinding("/", null, Protocol.RESCRIPT);
        ProtocolBinding pb2 = new ProtocolBinding("/api", null, Protocol.RESCRIPT);
        ProtocolBinding pb3 = new ProtocolBinding("/", null, Protocol.JSON_RPC);
        Set<ProtocolBinding> bindingSet = new HashSet<ProtocolBinding>();
        bindingSet.add(pb);
        bindingSet.add(pb2);
        bindingSet.add(pb3);
        ProtocolBindingRegistry bindingReg = Mockito.mock(ProtocolBindingRegistry.class);
        when(bindingReg.getProtocolBindings()).thenReturn(bindingSet);


        JettyHttpTransport transport = new JettyHttpTransport();
        populateTransport(transport);
        transport.setProtocolBindingRegistry(bindingReg);
        transport.setCommandProcessorFactory(factory);

        JettyEndpoints endpoints = Mockito.mock(JettyEndpoints.class);
        transport.setJettyEndPoints(endpoints);

        transport.notify(rescriptServiceBindingDescriptor);
        transport.notify(jsonRpcBindingDescriptor);

        transport.initialiseStaticJettyConfig();

        populateTransport(transport);
        transport.onCougarStart();

        assertEquals(0, transport.getHandlerCollection().getChildHandlersByClass(CrossOriginHandler.class).length);

        // Instruct Jetty not to wait for Handlers to terminate
        transport.getServerWrapper().getJettyServer().setStopTimeout(0);
        transport.stop();
    }

    @Test
    public void testEndpointListConstruction() throws Exception {
        ProtocolBinding pb = new ProtocolBinding("/", null, Protocol.RESCRIPT);
        ProtocolBinding pb2 = new ProtocolBinding("/api", null, Protocol.RESCRIPT);
        ProtocolBinding pb3 = new ProtocolBinding("/", null, Protocol.JSON_RPC);
        Set<ProtocolBinding> bindingSet = new HashSet<ProtocolBinding>();
        bindingSet.add(pb);
        bindingSet.add(pb2);
        bindingSet.add(pb3);
        ProtocolBindingRegistry bindingReg = Mockito.mock(ProtocolBindingRegistry.class);
        when(bindingReg.getProtocolBindings()).thenReturn(bindingSet);


        JettyHttpTransport transport = new JettyHttpTransport();
        populateTransport(transport);
        transport.setProtocolBindingRegistry(bindingReg);
        transport.setCommandProcessorFactory(factory);

        JettyEndpoints endpoints = Mockito.mock(JettyEndpoints.class);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        transport.setJettyEndPoints(endpoints);

        transport.notify(rescriptServiceBindingDescriptor);
        transport.notify(jsonRpcBindingDescriptor);

        transport.initialiseStaticJettyConfig();

        transport.onCougarStart();

        verify(endpoints).setEndPoints(captor.capture());

        List<String> endpointValues = captor.getValue();
        assertEquals("endpoints length incorrect", 3, endpointValues.size());

        HttpServiceBindingDescriptor bindingDescriptor;
        for (ProtocolBinding b : bindingSet) {

            if (b.getProtocol() == Protocol.RESCRIPT) {
                bindingDescriptor = rescriptServiceBindingDescriptor;
            } else {
                bindingDescriptor = jsonRpcBindingDescriptor;
            }

            String endpoint = b.getContextRoot() + bindingDescriptor.getServiceContextPath();

            if (b.getProtocol() != Protocol.JSON_RPC) {
                endpoint = endpoint + "v" + bindingDescriptor.getServiceVersion().getMajor() + OPERATION_URI;
            } else {
                endpoint = endpoint + "/";
            }

            boolean found = false;
            for (int i = 0; i < endpointValues.size() && !found; i++) {
                found = endpointValues.get(i).endsWith(endpoint);
            }
            assertTrue("endpoint was not found in list", found);
        }
    }

    @Test
    public void testSimpleStartStop() throws Exception {
        // don't set any overrides
        JettyHttpTransport transport = populateTransport();
        // initialise
        transport.start();
        transport.stop();
    }

    @Test
    public void testCantStartWithNoConnectors() throws Exception {
        try {
            JettyHttpTransport transport = populateTransport();
            transport.getServerWrapper().setHttpPort(-1);
            transport.getServerWrapper().setHttpsPort(-1);
            transport.initialiseStaticJettyConfig();
            fail("Transport shouldn't be able to start without connectors");
        } catch (IllegalStateException ise) {
            // expected
        }
    }

    private String getServerKeystorePath() throws IOException {
        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        final String subDir = SEP + "cougar-framework" + SEP + "jetty-transport";
        if (userDir.endsWith(subDir)) {
            userDir = userDir.substring(0, userDir.indexOf(subDir));
        }
        final String cert = SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "cougar_server_cert.jks";
        return new File(userDir, subDir + cert).getCanonicalPath();
    }

    private String getServerTruststorePath() throws IOException {
        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        final String subDir = SEP + "cougar-framework" + SEP + "jetty-transport";
        if (userDir.endsWith(subDir)) {
            userDir = userDir.substring(0, userDir.indexOf(subDir));
        }
        final String cert = SEP + "src" + SEP + "test" + SEP + "resources" + SEP + "cougar_client_ca.jks";
        return new File(userDir, subDir + cert).getCanonicalPath();
    }

    @Test
    public void testOnlyHttpsConnectorCreated() throws Exception {
        DummyTransport transport = new DummyTransport();
        populateTransport(transport);
        transport.getServerWrapper().setHttpPort(-1);
        transport.getServerWrapper().setHttpsPort(9443);
        transport.getServerWrapper().setHttpsKeystore(new FileSystemResource(getServerKeystorePath()));
        transport.getServerWrapper().setHttpsKeyPassword("password");
        transport.getServerWrapper().setHttpsKeystoreType("JKS");
        transport.initialiseStaticJettyConfig();
        assertFalse("Http transport should NOT have been created", transport.isHttpConnectorCreated());
        assertTrue("Https transport should have been created", transport.isHttpsConnectorCreated());
    }

    @Test
    public void testHttpsConnectorWithClientAuth() throws Exception {
        DummyTransport transport = new DummyTransport();
        populateTransport(transport);
        transport.getServerWrapper().setHttpPort(-1);
        transport.getServerWrapper().setHttpsPort(9443);
        transport.getServerWrapper().setHttpsKeystore(new FileSystemResource(getServerKeystorePath()));
        transport.getServerWrapper().setHttpsKeyPassword("password");
        transport.getServerWrapper().setHttpsKeystoreType("JKS");
        transport.getServerWrapper().setHttpsTruststore(new FileSystemResource(getServerTruststorePath()));
        transport.getServerWrapper().setHttpsTrustPassword("password");
        transport.getServerWrapper().setHttpsTruststoreType("JKS");
        transport.getServerWrapper().setHttpsNeedClientAuth(true);
        transport.initialiseStaticJettyConfig();
        assertFalse("Http transport should NOT have been created", transport.isHttpConnectorCreated());
        assertTrue("Https transport should have been created", transport.isHttpsConnectorCreated());
    }

    @Test
    public void testBothConnectorsCreated() throws Exception {
        DummyTransport transport = new DummyTransport();
        populateTransport(transport);
        transport.getServerWrapper().setHttpPort(TEST_HTTP_PORT);
        transport.getServerWrapper().setHttpsPort(9443);
        transport.getServerWrapper().setHttpsKeystore(new FileSystemResource(getServerKeystorePath()));
        transport.getServerWrapper().setHttpsKeyPassword("password");
        transport.getServerWrapper().setHttpsKeystoreType("JKS");
        transport.getServerWrapper().setHttpsTruststore(new FileSystemResource(getServerTruststorePath()));
        transport.getServerWrapper().setHttpsTrustPassword("password");
        transport.getServerWrapper().setHttpsTruststoreType("JKS");
        transport.initialiseStaticJettyConfig();
        assertTrue("Http transport should have been created", transport.isHttpConnectorCreated());
        assertTrue("Https transport should have been created", transport.isHttpsConnectorCreated());
    }

    @Test
    public void testThreadCounts() throws Exception {
        JettyHttpTransport transport = populateTransport();
        ProtocolBindingRegistry bindingReg = Mockito.mock(ProtocolBindingRegistry.class);
        when(bindingReg.getProtocolBindings()).thenReturn(new HashSet<ProtocolBinding>());
        transport.setProtocolBindingRegistry(bindingReg);
        JettyEndpoints endpoints = Mockito.mock(JettyEndpoints.class);
        transport.setJettyEndPoints(endpoints);
        transport.getServerWrapper().setHttpAcceptors(2);
        transport.getServerWrapper().setHttpSelectors(4);
        transport.getServerWrapper().setMaxThreads(8);
        transport.getServerWrapper().setMinThreads(8);

        transport.initialiseStaticJettyConfig();
        transport.getServerWrapper().setThreadPoolName("testThreadCounts");
        transport.onCougarStart();

        long startTime = System.currentTimeMillis();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        try {
            while (true) {
                try {
                    int acceptors = 0;
                    int selectors = 0;
                    int poolThreads = 0;

                    for (long l : threadMXBean.getAllThreadIds()) {
                        String threadName = threadMXBean.getThreadInfo(l).getThreadName();
                        if (threadName.matches("^testThreadCounts.*")) poolThreads++;
                        if (threadName.matches("^testThreadCounts.*acceptor.*")) acceptors++;
                        if (threadName.matches("^testThreadCounts.*selector.*")) selectors++;
                    }

                    assertEquals("Total thread count is not correct", 8, poolThreads);
                    assertEquals("Acceptor thread count is not correct", 2, acceptors);
                    // Jetty 9 threads don't become selectors automatically
//                    assertEquals("Selector thread count is not correct", 4, selectors);
                    break;
                } catch (AssertionError e) {
                    if (System.currentTimeMillis() - startTime > 10 * 1000) {
                        throw e;
                    } else {
                        // give threads some time to take runnables
                        Thread.sleep(1000);
                    }
                }
            }
        } finally {
            transport.getServerWrapper().stop();
        }
    }

    private class DummyTransport extends JettyHttpTransport {

        public boolean isHttpConnectorCreated() {
            return getServerWrapper().isHttpConnectorCreated();
        }

        public boolean isHttpsConnectorCreated() {
            return getServerWrapper().isHttpsConnectorCreated();
        }
    }
}
